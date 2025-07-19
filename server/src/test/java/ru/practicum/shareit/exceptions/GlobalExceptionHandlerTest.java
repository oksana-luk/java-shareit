package ru.practicum.shareit.exceptions;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;


import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler gl;

    @BeforeEach
    void setUp() {
        gl = new GlobalExceptionHandler();
    }

    @Test
    void shouldReturnConflictForDuplicateEmail() throws Exception {
        ResponseEntity<Map<String, String>> response = gl.handleEmailConflict(
                new DataIntegrityViolationException("message"));
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void shouldReturnMassageForValidationException() throws Exception {
        Map<String, String> response = gl.handleValidationExceptions(
                new ValidationException("message"));
        assertTrue(response.containsKey("Validation error"));
        assertEquals(response.get("Validation error"), "message");
    }

    @Test
    void shouldReturnMassageForUnacceptableValueException() throws Exception {
        Map<String, String> response = gl.handleUnacceptableUserException(
                new UnacceptableValueException("message"));
        assertTrue(response.containsKey("Unacceptable value"));
        assertEquals(response.get("Unacceptable value"), "message");
    }
}