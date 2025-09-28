package com.niallantony.deulaubaba.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.function.Supplier;


@Component
public class JsonUtils {
    private final ObjectMapper mapper;

    public JsonUtils(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public <T, E extends RuntimeException> T parse(String json, Class<T> type, Supplier<E> exceptionSupplier) {
        try {
            return mapper.readValue(json, type);
        } catch (IOException e) {
            throw exceptionSupplier.get();
        }
    }
}
