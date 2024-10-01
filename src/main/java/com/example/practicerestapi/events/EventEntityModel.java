package com.example.practicerestapi.events;

import org.springframework.hateoas.EntityModel;

public class EventEntityModel extends EntityModel<Event> {

    //이거 굳이 안말들고 사용할 객체에서 EntityModel entityModel = Entity.of(); 로 해도됨
}
