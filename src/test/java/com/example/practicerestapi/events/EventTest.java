package com.example.practicerestapi.events;

import junitparams.JUnitParamsRunner;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Locale;

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

    @ParameterizedTest
    @CsvSource({
        "0, 0, true",
        "100, 0, false",
        "0, 100, false"
    })
    public void testFree(int basePrice, int maxPrice, boolean isFree){
        //given
        Event event = Event.builder()
                .basePrice(basePrice)
                .maxPrice(maxPrice)
                .build();

        //when
        event.update();

        //then
        assertThat(event.isFree()).isEqualTo(isFree);
    }

    @ParameterizedTest
    @CsvSource({
            "우리집, true",
            ", false",
            "   , false"
    })
    public void testOffline(String location, boolean isOffline){
        Event event = Event.builder()
                .location(location)
                .build();

        event.update();

        assertThat(event.isOffline()).isEqualTo(isOffline);

    }



}