package com.niallantony.deulaubaba.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class JsonUtilsTest {

    private JsonUtils jsonUtils;

    static class Person {
        public String name;
        public int age;
    }

    static class MyRuntimeException extends RuntimeException {}

    @BeforeEach
    void setUp() {
        jsonUtils = new JsonUtils(new ObjectMapper());
    }

    @Test
    void parse_givenValidJson_returnsObject() {
        String json = "{\"name\":\"Alice\",\"age\":30}";

        Person person = jsonUtils.parse(json, Person.class, MyRuntimeException::new);

        assertEquals("Alice", person.name);
        assertEquals(30, person.age);
    }

    @Test
    void parse_whenGivenInvalidJson_throwsSuppliedException() {
        String invalidJson = "{not valid json}";

        assertThrows(MyRuntimeException.class, () ->
                jsonUtils.parse(invalidJson, Person.class, MyRuntimeException::new)
        );
    }
}