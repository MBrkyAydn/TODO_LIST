package com.berkay.todo.mapper;

import com.berkay.todo.dto.request.TaskRequest;
import com.berkay.todo.dto.response.TaskResponse;
import com.berkay.todo.entity.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskMapperTest {

    private TaskMapper taskMapper;

    @BeforeEach
    void setUp() {
        taskMapper = new TaskMapper();
    }

    @Test
    void toEntity_shouldMapTaskRequestToTask() {
        TaskRequest request = new TaskRequest("test", "açıklama", false);

        Task task = taskMapper.toEntity(request);

        assertNotNull(task);
        assertEquals("test", task.getTitle());
        assertEquals("açıklama", task.getDescription());
        assertFalse(task.getCompleted());
    }

    @Test
    void toResponse_shouldMapTaskToTaskResponse() {
        LocalDateTime now = LocalDateTime.now();

        Task task = new Task();
        task.setId(1L);
        task.setTitle("test");
        task.setDescription("açıklama");
        task.setCompleted(true);
        task.setCreatedAt(now);
        task.setUpdatedAt(now);

        TaskResponse response = taskMapper.toResponse(task);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("test", response.getTitle());
        assertEquals("açıklama", response.getDescription());
        assertTrue(response.getCompleted());
        assertEquals(now, response.getCreatedAt());
        assertEquals(now, response.getUpdatedAt());
    }
}
