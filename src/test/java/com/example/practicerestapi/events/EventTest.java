package com.example.practicerestapi.events;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class EventTest {
    @Test
    public void builder(){
        Event event = Event.builder()
                .name("Inflearn Spring REST API").build();
        assertThat(event).isNotNull();
    }
    @Test
    public void javaBean(){
        //given
        Event event = new Event();
        String name = "Event";

        //when
        event.setName(name);
        String description = "Spring";
        event.setDescription(description);

        //then
        assertThat(event.getName()).isEqualTo(name);
        assertThat(event.getDescription()).isEqualTo(description);
    }



}