package com.example.practicerestapi.events;


import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

/**
 * resourceSupport -> RepresentationModel
 * Bean Serializer을 사용해서 json으로 변환된다.
 */
public class EventPresentationModel extends RepresentationModel<EventPresentationModel> {

    @Getter
    @JsonUnwrapped
    // 현재 event라는 복합 클래스 아래에 _links가 들어간다. 그렇다면 같은 선상에 두려면 @JsonUnwrapped를 사용하면 된다.
    // 근데 JsonUnwrapped는 entitymodel에 있다.
    private Event event;

    public EventPresentationModel(Event event) {
        this.event = event;
    }


}
