package com.berkay.todo.dto;

import com.berkay.todo.dto.request.TaskRequest;
import com.berkay.todo.dto.request.TaskStatusRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TaskDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void taskRequest_shouldPassValidation_whenFieldsAreValid() {
        TaskRequest request = new TaskRequest("test", "açıklama", false);

        Set<ConstraintViolation<TaskRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void taskRequest_shouldFailValidation_whenTitleIsBlank() {
        TaskRequest request = new TaskRequest("", "açıklama", false);

        Set<ConstraintViolation<TaskRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("title")));
    }

    @Test
    void taskRequest_shouldFailValidation_whenDescriptionIsBlank() {
        TaskRequest request = new TaskRequest("test", "", false);

        Set<ConstraintViolation<TaskRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("description")));
    }

    @Test
    void taskRequest_shouldFailValidation_whenCompletedIsNull() {
        TaskRequest request = new TaskRequest("test", "açıklama", null);

        Set<ConstraintViolation<TaskRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("completed")));
    }

    @Test
    void taskStatusRequest_shouldPassValidation_whenCompletedIsNotNull() {
        TaskStatusRequest request = new TaskStatusRequest(true);

        Set<ConstraintViolation<TaskStatusRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void taskStatusRequest_shouldFailValidation_whenCompletedIsNull() {
        TaskStatusRequest request = new TaskStatusRequest(null);

        Set<ConstraintViolation<TaskStatusRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("completed")));
    }
}
