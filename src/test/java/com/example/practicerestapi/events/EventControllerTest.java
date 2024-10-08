package com.example.practicerestapi.events;

import com.example.practicerestapi.common.RestDocsConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;

import java.time.LocalDateTime;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)//restdocs 사용할때 json 이쁘게 출력하기위한 config파일 등록
public class EventControllerTest {

    //api/events
    @Autowired
    MockMvc mockMvc;
    //단위 테스트라고 불리기엔 어렵다. 서버를 띄우는것 보단 빠르지만. 단위 테스트보다는 느리다.

    @Autowired
    ObjectMapper objectMapper; //얘는 스프링에 의해서 기본으로 빈 등록되어있다.

    @Autowired
    EventRepository eventRepository;

    @Autowired
    ModelMapper modelMapper;

    @Test
    @Transactional
    @DisplayName("정상적으로 이벤트를 생성하는 테스트")
    public void createEvent() throws Exception {
        EventDto event = EventDto.builder()
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2018, 11, 23, 14, 21))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 11, 24, 14, 21))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 25, 14, 21))
                .endEventDateTime(LocalDateTime.of(2018, 11, 26, 14, 21))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타텁 팩토리")
                .build();

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, containsString(MediaTypes.HAL_JSON_VALUE)))
                .andExpect(jsonPath("free").value(false))
                .andExpect(jsonPath("offline").value(true))
                .andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.name()))
                .andExpect(jsonPath("_links.self").exists())//여기서 부터
                .andExpect(jsonPath("_links.query-events").exists())
                .andExpect(jsonPath("_links.update-event").exists())
                .andExpect(jsonPath("_links.profile").exists()) //여기까지 없어도 된다. 아래서 documnet에서 검증한다.

                .andDo(document("created-event",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("query-events").description("link to query events"),
                                linkWithRel("update-event").description("link to update a existing"),
                                linkWithRel("profile").description("link to profile a existing")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        requestFields(
                                fieldWithPath("name").description("Name of new event"),
                                fieldWithPath("description").description("Description of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("beginEnrollmentDateTime of new event"),
                                fieldWithPath("closeEnrollmentDateTime").description("closeEnrollmentDateTime of new event"),
                                fieldWithPath("beginEventDateTime").description("beginEventDateTime of new event"),
                                fieldWithPath("endEventDateTime").description("endEventDateTime of new event"),
                                fieldWithPath("location").description("location of new event"),
                                fieldWithPath("basePrice").description("basePrice of new event"),
                                fieldWithPath("maxPrice").description("maxPrice of new event"),
                                fieldWithPath("limitOfEnrollment").description("limitOfEnrollment of new event")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("Response Header of Location"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Response Header of ContentType")
                        ),
                        //ResponseFields 랑 차이점.... ResponseFields사용하면 _links는 왜 검사 안하냐고 오류가 뜬다. 따라서 relaxedResponseFields을 사용한다.
                        //단점은 정확한 문서를 만들지 못한다.
                        responseFields(
                                fieldWithPath("id").description("Id of new event"),
                                fieldWithPath("name").description("Name of new event"),
                                fieldWithPath("description").description("Description of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("beginEnrollmentDateTime of new event"),
                                fieldWithPath("closeEnrollmentDateTime").description("closeEnrollmentDateTime of new event"),
                                fieldWithPath("beginEventDateTime").description("beginEventDateTime of new event"),
                                fieldWithPath("endEventDateTime").description("endEventDateTime of new event"),
                                fieldWithPath("location").description("location of new event"),
                                fieldWithPath("basePrice").description("basePrice of new event"),
                                fieldWithPath("maxPrice").description("maxPrice of new event"),
                                fieldWithPath("limitOfEnrollment").description("limitOfEnrollment of new event"),
                                fieldWithPath("free").description("free of new event"),
                                fieldWithPath("offline").description("offline of new event"),
                                fieldWithPath("eventStatus").description("offline of new event"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.query-events.href").description("link to query event"),
                                fieldWithPath("_links.update-event.href").description("link to update event"),
                                fieldWithPath("_links.profile.href").description("link to profile")
                        )
                ));

    }


    @Test
    @Transactional
    @DisplayName("입력 받을 수 없는ㄷ 값을 사용한 경우에 에러가 발생하는 테스트")
    public void createEvent_Bad_Request() throws Exception {
        Event event = Event.builder()
                .id(100)
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2018, 11, 23, 14, 21))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 11, 24, 14, 21))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 25, 14, 21))
                .endEventDateTime(LocalDateTime.of(2018, 11, 26, 14, 21))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타텁 팩토리")
                .free(true)
                .offline(false)
                .eventStatus(EventStatus.PUBLISHED)
                .build();

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @Transactional
    @DisplayName("입력 값이 비어있는 경우에 에러가 발생하는 테스트")
    public void createdEvent_Bad_Request_Empty_Input() throws Exception {
        EventDto eventDto = new EventDto();

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    @DisplayName("입력 값이 잘못된 경우에 에러가 발생하는 테스트")
    public void createdEvent_Bad_Request_Wrong_Input() throws Exception {
        EventDto eventDto = EventDto.builder()
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2018, 11, 26, 14, 21))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 11, 25, 14, 21))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 24, 14, 21))
                .endEventDateTime(LocalDateTime.of(2018, 11, 23, 14, 21))
                .basePrice(10000)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타텁 팩토리")
                .build();

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    @DisplayName("입력 값이 잘못된 경우에 에러가 발생하는 테스트 + Body에 메시지")
    public void createdEvent_Bad_Request_Wrong_Input_And_MessageBody() throws Exception {
        EventDto eventDto = EventDto.builder()
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2018, 11, 26, 14, 21))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 11, 25, 14, 21))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 24, 14, 21))
                .endEventDateTime(LocalDateTime.of(2018, 11, 23, 14, 21))
                .basePrice(10000)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타텁 팩토리")
                .build();

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].objectName").exists())
                .andExpect(jsonPath("errors[0].defaultMessage").exists())
                .andExpect(jsonPath("errors[0].code").exists());


    }

    @Test
    @Transactional
    @DisplayName("30개의 이벤트를 10개씩 두번째 페이지 조회하기.")
    public void getEvents() throws Exception {
        // Given
        IntStream.range(0, 30).forEach(i -> {
            generateEvent(i);
        });
        // When
        mockMvc.perform(get("/api/events")
                        .queryParam("page", "1")
                        .queryParam("size", "10")
                        .queryParam("sort", "name,DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("query-events"));


        // Then
    }

    @Test
    @Transactional
    @DisplayName("기존의 이벤트 단건 조회하기")
    public void getEvent() throws Exception {
        //given
        Event event = generateEvent(100);
        //when
        //@Pathvariable 사용
        mockMvc.perform(get("/api/events/{id}", event.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").exists())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("_links").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("get-an-event"));

        //then
    }

    @Test
    @Transactional
    @DisplayName("없는 이벤트 조회했을때 404 응답받기")
    public void getEvent404() throws Exception {
        //given
        //when
        //@Pathvariable 사용
        mockMvc.perform(get("/api/events/123213"))
                .andExpect(status().isNotFound());


        //then
    }
    //수정하려는 이벤트가 없는 경우 404 NOT_FOUND
    @Test
    @Rollback
    @DisplayName("수정하려는 이벤트가 없는 경우 404 NOT_FOUND")
    public void updateEvent404() throws Exception {
        //given
        Event event = generateEvent(100);
        EventDto eventDto = modelMapper.map(event, EventDto.class);
        //when
        ResultActions resultActions =
                mockMvc.perform(put("/api/events/{id}", 10000)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                        .andDo(print());
        //then
        resultActions
                .andExpect(status().isNotFound());
    }

    //입력 데이터 (데이터 바인딩)가 이상한 경우에 400 BAD_REQUEST
    @Test
    @Transactional
    @Rollback
    @DisplayName("입력 데이터 (데이터 바인딩)가 이상한 경우에 400 BAD_REQUEST")
    public void updateEvent400_fieldErrors() throws Exception{
        //given
        Event event = generateEvent(100);

        EventDto eventDto = EventDto.builder()
                .build();

        //when
        ResultActions resultActions = mockMvc.perform(put("/api/events/{id}",event.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(eventDto)))

                .andDo(print());

        //then
        resultActions.andExpect(status().isBadRequest());
    }
    
    //도메인 로직으로 데이터 검증 실패하면 400 BAD_REQUEST
    @Test
    @Transactional
    @DisplayName("도메인 로직으로 데이터 검증 실패하면 400 BAD_REQUEST")
    public void updateEvent400_globalErrors() throws Exception {
        //given
        Event event = generateEvent(100);

        EventDto eventDto = modelMapper.map(event, EventDto.class);
        eventDto.setMaxPrice(1000);
        eventDto.setBasePrice(1000000);

        //when
        ResultActions resultActions = mockMvc.perform(put("/api/events/{id}",event.getId()))
                .andDo(print());

        //then
        resultActions.andExpect(status().isBadRequest());
    }


    //정상적으로 수정한 경우에 이벤트 리소스 응답
    //200 OK
    //링크
    //수정한 이벤트 데이터
    @Test
    @Transactional
    @DisplayName("이벤트를 정상적으로 수정하기")
    public void updateEvent200() throws Exception{
        //given
        Event event = generateEvent(100);
        String eventName = "Updated Event";
        EventDto eventDto = modelMapper.map(event, EventDto.class);
        eventDto.setName(eventName);

        //when
        ResultActions resultActions = mockMvc.perform(put("/api/events/{id}",event.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(eventDto)))
                .andDo(print());

        //then
        resultActions
                //나머지 필드 검증은 생략...
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value(eventName))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists());

    }


    private Event generateEvent(int index) {
        Event event = Event.builder()
                .name("event" + index)
                .description("test event")
                .beginEnrollmentDateTime(LocalDateTime.of(2018, 11, 23, 14, 21))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 11, 24, 14, 21))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 25, 14, 21))
                .endEventDateTime(LocalDateTime.of(2018, 11, 26, 14, 21))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타텁 팩토리")
                .free(false)
                .offline(false)
                .eventStatus(EventStatus.DRAFT)
                .build();
        return eventRepository.save(event);
    }


}
