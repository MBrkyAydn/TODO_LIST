package com.berkay.todo.service;

import com.berkay.todo.dto.request.TaskRequest;
import com.berkay.todo.dto.response.TaskResponse;
import com.berkay.todo.entity.Task;
import com.berkay.todo.entity.User;
import com.berkay.todo.exception.AlreadyExistsException;
import com.berkay.todo.mapper.TaskMapper;
import com.berkay.todo.repository.TaskRepository;
import com.berkay.todo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private TaskMapper taskMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;

    @InjectMocks
    private TaskService taskService;

    @Test
    void testClassLoads() {
    }

    @Test
    void createTask_shouldSaveTaskForCurrentUser() {
        TaskRequest request = new TaskRequest();
        request.setTitle("Test Görevi");
        request.setDescription("JUnit ile oluşturulan görev");
        request.setCompleted(false);


        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");


        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setCompleted(request.getCompleted());

        Task savedTask = new Task();
        savedTask.setId(10L);
        savedTask.setTitle(request.getTitle());
        savedTask.setDescription(request.getDescription());
        savedTask.setCompleted(request.getCompleted());
        savedTask.setUser(user);


        TaskResponse response = new TaskResponse();
        response.setId(10L);
        response.setTitle("Test Görevi");
        response.setDescription("JUnit ile oluşturulan görev");
        response.setCompleted(false);

        when(taskRepository.existsByTitle(request.getTitle())).
                thenReturn(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(taskMapper.toEntity(request)).thenReturn(task);
        when(taskMapper.toResponse(savedTask)).thenReturn(response);
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "test@example.com",
                        null,
                        java.util.Collections.emptyList()
                )
        );

        var result = taskService.createTask(request);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Test Görevi", result.getTitle());

        verify(taskRepository).save(any(Task.class));

        verify(taskMapper).toResponse(savedTask);


    }

    @Test
    void createTask_shouldThrowExceptionWhenTitleAlreadyExists() {
        TaskRequest request = new TaskRequest();
        request.setTitle("Test Görevi");
        when(taskRepository.existsByTitle("Test Görevi")).thenReturn(true);
        // ACT
        assertThrows(AlreadyExistsException.class, () -> taskService.createTask(request));

        //verify

        verify(taskRepository, never()).save(any(Task.class));


    }

    @Test
    void createTask_shouldThrowExcepitonWhenCurrentUserNotFound() {
        TaskRequest request = new TaskRequest();
        request.setTitle("Test");
        request.setCompleted(false);
        request.setDescription("JUNIT");

        when(taskRepository.existsByTitle("Test")).thenReturn(false);
        when(taskMapper.toEntity(request)).thenReturn(new Task());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("test@example.com", null, java.util.Collections.emptyList()));
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());

//act

        RuntimeException exception = assertThrows(RuntimeException.class, () -> taskService.createTask(request));
        assertEquals("Kullanıcı bulunamadı", exception.getMessage());
        //verify

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTask_shouldUpdateTaskSuccessfully() {
        Long taskId = 10L;

        TaskRequest request = new TaskRequest();

        request.setTitle("update");
        request.setCompleted(true);
        request.setDescription("UPDATED");


        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");


        Task task = new Task();
        task.setId(taskId);
        task.setTitle("eski");
        task.setDescription("Eski açıklama");
        task.setCompleted(false);
        task.setUser(user);

        TaskResponse response = new TaskResponse();

        response.setId(taskId);
        response.setTitle("update");
        response.setDescription("UPDATED");
        response.setCompleted(true);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("test@example.com", null, java.util.Collections.emptyList())
        );

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.existsByTitleAndIdNot(request.getTitle(), taskId)).thenReturn(false);
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));
        when(taskMapper.toResponse(any(Task.class))).thenReturn(response);

        // act
        TaskResponse result = taskService.updateTask(request, taskId);

        // assert
        assertNotNull(result);
        assertEquals(taskId, result.getId());
        assertEquals("update", result.getTitle());
        assertEquals("UPDATED", result.getDescription());
        assertTrue(result.getCompleted());

        verify(taskRepository, times(1)).save(any(Task.class));
        verify(taskMapper).toResponse(any(Task.class));
    }
}
