package com.example.practicerestapi.events;

import com.example.practicerestapi.common.ErrorResource;
import com.example.practicerestapi.index.IndexController;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.*;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_VALUE)
@RequiredArgsConstructor
public class EventController {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    private final EventValidator eventValidator;

    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody @Validated EventDto eventDto, Errors errors) {
        if (errors.hasErrors()) {
            return badRequest(errors);

            /**
             *  return ResponseEntity.badRequest().body(errors);
             *  return ResponseEntity.badRequest().body(bindingResult);
             *  //위에서 정의한거 둘중에 아무거나 사용해도 된다. 왜냐면 에러가 생기면 원래 error메시지는 json으로 반환 못한다.
             *  하지만 ErrorsSerializer(에러 직렬화)를 만들어 뒀기에 에러 메시지를 json으로 직렬화 할 수 있기때문이다.
             *  하지만 이렇게 반환하면 _links를 담지 못한다.(에러를 reponse할때 _links를 담는건 자유다. 보통 안담는다고 한다.)
             *  예제에서는 오류 상황에 index페이지의 uri을 보내고 있다.
             *  error(bindingResult 를 응답 바디에 보내고 있다. 그런데 오류가 생긴다면 index로 이동하게해보자.)
             *  이렇게
             */

        }
        eventValidator.validate(eventDto, errors);
        if (errors.hasErrors()) {
            return badRequest(errors);
        }
        Event event = modelMapper.map(eventDto, Event.class);
        event.update();
        Event newEvent = eventRepository.save(event);

        URI uri = linkTo(EventController.class).slash(newEvent.getId()).toUri();

        EntityModel<Event> eventModel = EntityModel.of(event);

//        EventPresentationModel eventPresentationModel = new EventPresentationModel(event); //json변환 과정에서 event 객체로 감싼놈
        eventModel.add(linkTo(EventController.class).withSelfRel());
        eventModel.add(linkTo(EventController.class).withRel("query-events"));
        eventModel.add(linkTo(EventController.class).withRel("update-event"));
        // update의 method는 put이다. 따라서 주소가 같아도 상관없다. (아쉽지만 method는 hateoas로 명시하지 못한다.)
        eventModel.add(Link.of("http://localhost:8080/docs/events-create.html", IanaLinkRelations.PROFILE));
        return ResponseEntity.created(uri).body(eventModel);
    }

    @GetMapping
    public ResponseEntity queryEvents(@RequestParam("page") int page,
                                      @RequestParam("size") int size,
                                      @RequestParam("sort") String sort,
                                      PagedResourcesAssembler<Event> assembler) {
        String[] sortArr = sort.split(",");
        PageRequest pageRequest = PageRequest.of(page, size, getSort(sortArr), sortArr[0]);
        Page<Event> events = eventRepository.findAll(pageRequest);
        var pagedResources = assembler.toModel(events, e -> EntityModel.of(e).add(linkTo(EventController.class).slash(e.getId()).withSelfRel()));
        pagedResources.add(Link.of("http://localhost:8080/docs/events-list.html", IanaLinkRelations.PROFILE));
        return ResponseEntity.ok(pagedResources);

    }

    @GetMapping("/{id}")
    public ResponseEntity getEvent(@PathVariable("id") Integer id) {
        Optional<Event> optionalEvent = eventRepository.findById(id);
        if (optionalEvent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Event event = optionalEvent.get();
        EntityModel<Event> eventEntityModel = EntityModel.of(event);
        eventEntityModel.add(linkTo(EventController.class).withSelfRel());
        eventEntityModel.add(Link.of("http://localhost:8080/docs/events-get.html", IanaLinkRelations.PROFILE));

        return ResponseEntity.ok(eventEntityModel);
    }

    @PutMapping("/{id}")
    public ResponseEntity updateEvent(@PathVariable("id") Integer id,
                                      @RequestBody @Validated EventDto eventDto,
                                      BindingResult bindingResult) {

        // 404 에러(값이 없다.)
        Optional<Event> optionalEvent = eventRepository.findById(id);
        if (optionalEvent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // 바인딩 에러 (타입 불일치)
        if(bindingResult.hasErrors()){
            // BindResult해도 상위 클래스가 Erros이라서 BindingResult도 직렬화 된다.
            // 이제 EntityModel에 Erros(BindingResult)를 담아서 _links로 index로 가는 링크를 담아주자.(오류 나면 안내할 페이지?)
            EntityModel<BindingResult> indexEntityModel =
                    EntityModel.of(bindingResult).add(linkTo(methodOn(IndexController.class).index()).withRel("index"));
            return ResponseEntity.badRequest().body(indexEntityModel);
        }

        // 글로벌 에러(데이터 검증 실패)
        eventValidator.validate(eventDto, bindingResult);
        if(bindingResult.hasErrors()){
            EntityModel<BindingResult> indexEntityModel =
                    EntityModel.of(bindingResult).add(linkTo(methodOn(IndexController.class).index()).withRel("index"));
            return ResponseEntity.badRequest().body(indexEntityModel);
        }

        Event event = optionalEvent.get();
        modelMapper.map(eventDto, event);
        Event savedEvent = eventRepository.save(event);
        EntityModel<Event> eventEntityModel = EntityModel.of(savedEvent);
        eventEntityModel.add(linkTo(EventController.class).withSelfRel());// self description message
        eventEntityModel.add(Link.of("http://localhost:8080/docs/events-update.html", IanaLinkRelations.PROFILE)); //profile

        return ResponseEntity.ok().body(eventEntityModel);
    }

    private Sort.Direction getSort(String[] sortArr) {
        if (sortArr[1].equals("DESC")) return Sort.Direction.DESC;
        return Sort.Direction.ASC;
    }

    private ResponseEntity<EntityModel<Errors>> badRequest(Errors errors) {
        /**
         * Error(bindResult)를 직렬화하기 위해서는 Error에 대한 직렬화 클래스를 따로 작성한다.
         * 이후 1번 방법이나 2번 방법을 사용한다. 둘이 결론은 같다.
         * 근데 1번 방법 쓰려면 오버라이딩 할때 생성자가 protected 되어있어서 public으로 한개 만들어줘야 한다.
         */

        //1번 방법 return type은 ResponseEntity<ErrorResource>
//        return ResponseEntity.badRequest().body(new ErrorResource(errors));

        //2번 방법 return type은 ResponseEntity<EntityModel<Errors>>이다.
        EntityModel<Errors> indexEntityModel =
                EntityModel.of(errors).add(linkTo(methodOn(IndexController.class).index()).withRel("index"));
        return ResponseEntity.badRequest().body(indexEntityModel);
    }


}
