package com.berkay.todo.controller;

import com.berkay.todo.dto.request.TaskRequest;
import com.berkay.todo.dto.request.TaskStatusRequest;
import com.berkay.todo.dto.response.TaskResponse;
import com.berkay.todo.exception.AlreadyExistsException;
import com.berkay.todo.exception.TaskNotFoundException;
import com.berkay.todo.security.JwtService;
import com.berkay.todo.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private TaskResponse createTaskResponse(Long id, String title, String description, Boolean completed) {
        TaskResponse response = new TaskResponse();
        response.setId(id);
        response.setTitle(title);
        response.setDescription(description);
        response.setCompleted(completed);
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());
        return response;
    }

    @Test
    void createTask_shouldReturnCreatedTask_whenRequestIsValid() throws Exception {
        TaskRequest request = new TaskRequest("test", "açıklama", false);
        TaskResponse response = createTaskResponse(1L, "test", "açıklama", false);

        when(taskService.createTask(any(TaskRequest.class))).thenReturn(response);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("test"))
                .andExpect(jsonPath("$.description").value("açıklama"))
                .andExpect(jsonPath("$.completed").value(false));

        verify(taskService, times(1)).createTask(any(TaskRequest.class));
    }

    @Test
    void createTask_shouldReturnBadRequest_whenTitleIsBlank() throws Exception {
        TaskRequest request = new TaskRequest("", "açıklama", false);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Doğrulama Hatası"));

        verify(taskService, never()).createTask(any(TaskRequest.class));
    }

    @Test
    void createTask_shouldReturnConflict_whenTitleAlreadyExists() throws Exception {
        TaskRequest request = new TaskRequest("test", "açıklama", false);

        when(taskService.createTask(any(TaskRequest.class)))
                .thenThrow(new AlreadyExistsException("hata"));

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Kayıt Zaten Mevcut"))
                .andExpect(jsonPath("$.message").value("hata"));

        verify(taskService, times(1)).createTask(any(TaskRequest.class));
    }

    @Test
    void getTasks_shouldReturnTaskList() throws Exception {
        List<TaskResponse> responses = List.of(
                createTaskResponse(1L, "test", "açıklama", false),
                createTaskResponse(2L, "test2", "açıklama2", true)
        );

        when(taskService.findAllTasks()).thenReturn(responses);

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("test"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].title").value("test2"));

        verify(taskService, times(1)).findAllTasks();
    }

    @Test
    void getTaskById_shouldReturnTask_whenTaskExists() throws Exception {
        TaskResponse response = createTaskResponse(1L, "test", "açıklama", false);

        when(taskService.findTaskById(1L)).thenReturn(response);

        mockMvc.perform(get("/tasks/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("test"))
                .andExpect(jsonPath("$.description").value("açıklama"))
                .andExpect(jsonPath("$.completed").value(false));

        verify(taskService, times(1)).findTaskById(1L);
    }

    @Test
    void getTaskById_shouldReturnNotFound_whenTaskDoesNotExist() throws Exception {
        when(taskService.findTaskById(99L))
                .thenThrow(new TaskNotFoundException("hata"));

        mockMvc.perform(get("/tasks/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Kayıt Bulunamadı"))
                .andExpect(jsonPath("$.message").value("hata"));

        verify(taskService, times(1)).findTaskById(99L);
    }

    @Test
    void updateTask_shouldReturnUpdatedTask_whenRequestIsValid() throws Exception {
        TaskRequest request = new TaskRequest("update", "açıklama", true);
        TaskResponse response = createTaskResponse(1L, "update", "açıklama", true);

        when(taskService.updateTask(any(TaskRequest.class), eq(1L))).thenReturn(response);

        mockMvc.perform(put("/tasks/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("update"))
                .andExpect(jsonPath("$.description").value("açıklama"))
                .andExpect(jsonPath("$.completed").value(true));

        verify(taskService, times(1)).updateTask(any(TaskRequest.class), eq(1L));
    }

    @Test
    void updateTask_shouldReturnBadRequest_whenRequestIsInvalid() throws Exception {
        TaskRequest request = new TaskRequest("", "", null);

        mockMvc.perform(put("/tasks/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Doğrulama Hatası"));

        verify(taskService, never()).updateTask(any(TaskRequest.class), anyLong());
    }

    @Test
    void updateTask_shouldReturnNotFound_whenTaskDoesNotExist() throws Exception {
        TaskRequest request = new TaskRequest("test", "açıklama", true);

        when(taskService.updateTask(any(TaskRequest.class), eq(99L)))
                .thenThrow(new TaskNotFoundException("hata"));

        mockMvc.perform(put("/tasks/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Kayıt Bulunamadı"));

        verify(taskService, times(1)).updateTask(any(TaskRequest.class), eq(99L));
    }

    @Test
    void deleteTask_shouldReturnNoContent_whenTaskExists() throws Exception {
        doNothing().when(taskService).deleteTask(1L);

        mockMvc.perform(delete("/tasks/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(taskService, times(1)).deleteTask(1L);
    }

    @Test
    void deleteTask_shouldReturnNotFound_whenTaskDoesNotExist() throws Exception {
        doThrow(new TaskNotFoundException("hata"))
                .when(taskService).deleteTask(99L);

        mockMvc.perform(delete("/tasks/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Kayıt Bulunamadı"));

        verify(taskService, times(1)).deleteTask(99L);
    }

    @Test
    void getTaskByTitle_shouldReturnTask_whenTaskExists() throws Exception {
        TaskResponse response = createTaskResponse(1L, "test", "açıklama", false);

        when(taskService.findTaskByTitle("test")).thenReturn(response);

        mockMvc.perform(get("/tasks/title/{title}", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("test"));

        verify(taskService, times(1)).findTaskByTitle("test");
    }

    @Test
    void getTaskByTitle_shouldReturnNotFound_whenTaskDoesNotExist() throws Exception {
        when(taskService.findTaskByTitle("no"))
                .thenThrow(new TaskNotFoundException("hata"));

        mockMvc.perform(get("/tasks/title/{title}", "no"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Kayıt Bulunamadı"));

        verify(taskService, times(1)).findTaskByTitle("no");
    }

    @Test
    void updateTaskCompleted_shouldReturnUpdatedTask_whenRequestIsValid() throws Exception {
        TaskStatusRequest request = new TaskStatusRequest(true);
        TaskResponse response = createTaskResponse(1L, "test", "açıklama", true);

        when(taskService.updateCompleted(1L, true)).thenReturn(response);

        mockMvc.perform(patch("/tasks/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.completed").value(true));

        verify(taskService, times(1)).updateCompleted(1L, true);
    }

    @Test
    void updateTaskCompleted_shouldReturnBadRequest_whenCompletedIsNull() throws Exception {
        TaskStatusRequest request = new TaskStatusRequest(null);

        mockMvc.perform(patch("/tasks/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Doğrulama Hatası"));

        verify(taskService, never()).updateCompleted(anyLong(), any());
    }

    @Test
    void updateTaskCompleted_shouldReturnNotFound_whenTaskDoesNotExist() throws Exception {
        TaskStatusRequest request = new TaskStatusRequest(true);

        when(taskService.updateCompleted(99L, true))
                .thenThrow(new TaskNotFoundException("hata"));

        mockMvc.perform(patch("/tasks/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Kayıt Bulunamadı"));

        verify(taskService, times(1)).updateCompleted(99L, true);
    }
}
