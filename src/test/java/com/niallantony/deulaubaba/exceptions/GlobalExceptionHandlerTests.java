package com.niallantony.deulaubaba.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleResourceNotFound_returns404WithMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not here!");
        ResponseEntity<?> response = handler.handleResourceNotFoundException(ex);

        assertEquals(404, response.getStatusCode().value());
        assertEquals(Map.of("message", "Not here!"), response.getBody());
    }

    @Test
    void handleUserNotAuthorized_returns401WithMessage() {
        UserNotAuthorizedException ex = new UserNotAuthorizedException("Nope!");
        ResponseEntity<?> response = handler.handleUserNotAuthorizedException(ex);

        assertEquals(401, response.getStatusCode().value());
        assertEquals(Map.of("message", "Nope!"), response.getBody());
    }

    @Test
    void handleInvalidUserData_returns400WithMessage() {
        InvalidUserDataException ex = new InvalidUserDataException("Bad input!");
        ResponseEntity<?> response = handler.handleInvalidUserDataException(ex);

        assertEquals(400, response.getStatusCode().value());
        assertEquals(Map.of("message", "Bad input!"), response.getBody());
    }
}