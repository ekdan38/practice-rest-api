package com.example.practicerestapi.events;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Controller
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_VALUE)
@RequiredArgsConstructor
public class EventController {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    private final EventValidator eventValidator;

    @PostMapping
    public ResponseEntity createEvent(@RequestBody @Validated EventDto eventDto, BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            return ResponseEntity.badRequest().body(bindingResult);
        }

        eventValidator.validate(eventDto, bindingResult);
        if(bindingResult.hasErrors()){
            return ResponseEntity.badRequest().body(bindingResult);
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

        return ResponseEntity.created(uri).body(eventModel);
    }

}
