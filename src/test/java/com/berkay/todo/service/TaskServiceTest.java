package com.berkay.todo.service;

import com.berkay.todo.dto.request.TaskRequest;
import com.berkay.todo.dto.response.TaskResponse;
import com.berkay.todo.entity.Task;
import com.berkay.todo.entity.User;
import com.berkay.todo.exception.AlreadyExistsException;
import com.berkay.todo.exception.TaskNotFoundException;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
        when(taskRepository.findByIdAndUser_Id(taskId, user.getId())).thenReturn(Optional.of(task));
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

    @Test
    void updateTask_shouldThrowExceptionWhenTaskNotFound() {

        Long taskId = 10L;
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        TaskRequest request = new TaskRequest();
        request.setTitle("update");
        request.setCompleted(true);
        request.setDescription("UPDATED");
        SecurityContextHolder.getContext().setAuthentication(
                new
                        UsernamePasswordAuthenticationToken("test@example.com", null, java.util.Collections.emptyList())
        );
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(taskRepository.findByIdAndUser_Id(taskId, user.getId())).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.updateTask(request, taskId));


        verify(taskRepository, never()).save(any(Task.class));
        verify(taskMapper, never()).toResponse(any(Task.class));


    }

    @Test
    void updateTask_shouldThrowExceptionWhenTitleAlreadyExists() {
        Long taskId = 10L;
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        Task task = new Task();
        task.setId(taskId);
        task.setUser(user);

        TaskRequest request = new TaskRequest();
        request.setTitle("mevcut");
        request.setCompleted(false);
        request.setDescription("yeni");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("test@example.com", null, java.util.Collections.emptyList()));
        when(taskRepository.findByIdAndUser_Id(taskId, user.getId())).thenReturn(Optional.of(task));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(taskRepository.existsByTitleAndIdNot(request.getTitle(), taskId)).thenReturn(true);
        assertThrows(AlreadyExistsException.class, () -> taskService.updateTask(request, taskId));

        verify(taskRepository, never()).save(any(Task.class));


    }

    @Test
    void deleteTask_shouldDeleteTaskSuccessfully() {
        Long taskId = 10L;

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");


        Task task = new Task();
        task.setId(taskId);
        task.setUser(user);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("test@example.com", null, java.util.Collections.emptyList())
        );

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(taskRepository.findByIdAndUser_Id(taskId, user.getId())).thenReturn(Optional.of(task));
        taskService.deleteTask(taskId);

        verify(taskRepository, times(1)).delete(task);


    }

    @Test
    void deleteTask_shouldThrowExceptionWhenTaskNotFound() {
        Long taskId = 10L;

        User user = new User();
        user.setId(1L);
        user.setEmail("1@gmail.com");

        Task task = new Task();
        task.setId(taskId);
        task.setUser(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1@gmail.com", null, java.util.Collections.emptyList())
        );

        when(taskRepository.findByIdAndUser_Id(taskId, user.getId())).thenReturn(Optional.empty());
        when(userRepository.findByEmail("1@gmail.com")).thenReturn(Optional.of(user));
        assertThrows(TaskNotFoundException.class, () -> taskService.deleteTask(taskId));
        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    void findTaskById_shouldReturnTaskWhenFound() {
        Long taskId = 10L;

        User user = new User();
        user.setId(1L);
        user.setEmail("1@gmail.com");


        Task task = new Task();
        task.setId(taskId);

        task.setCompleted(false);
        task.setDescription("açıklama");
        task.setTitle("test");
        task.setUser(user);

        TaskResponse response = new TaskResponse();

        response.setId(taskId);
        response.setTitle("test");
        response.setCompleted(false);
        response.setDescription("açıklama");


        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1@gmail.com", null, java.util.Collections.emptyList())
        );

        when(taskRepository.findByIdAndUser_Id(taskId, user.getId())).thenReturn(Optional.of(task));
        when(taskMapper.toResponse(task)).thenReturn(response);
        when(userRepository.findByEmail("1@gmail.com")).thenReturn(Optional.of(user));

        TaskResponse result = taskService.findTaskById(taskId);
        assertNotNull(result);
        assertEquals(taskId, result.getId());
        assertEquals("test", result.getTitle());

        verify(taskRepository, times(1)).findByIdAndUser_Id(taskId, user.getId());
        verify(taskMapper, times(1)).toResponse(task);

    }

    @Test
    void findTaskById_shouldThrowExceptionWhenTaskNotFound() {
        Long taskId = 12123L;

        User user = new User();
        user.setId(1L);
        user.setEmail("1@gmail.com");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "1@gmail.com", null, java.util.Collections.emptyList())
        );

        when(userRepository.findByEmail("1@gmail.com")).thenReturn(Optional.of(user));
        when(taskRepository.findByIdAndUser_Id(taskId, user.getId())).thenReturn(Optional.empty());

        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class, () -> taskService.findTaskById(taskId));

        assertEquals("Belirtilen ID numarasına sahip görev bulunamadı: " + taskId, exception.getMessage()
        );

        verify(taskRepository, times(1)).findByIdAndUser_Id(taskId, user.getId());
        verify(taskMapper, never()).toResponse(any(Task.class));

    }

    @Test
    void findTaskByTitle_shouldThrowExceptionTaskNotWhenFound() {

        String title = "no";

        User user = new User();

        user.setId(1L);
        user.setEmail("1@gmail.com");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1@gmail.com", null, java.util.Collections.emptyList())
        );

        when(userRepository.findByEmail("1@gmail.com")).thenReturn(Optional.of(user));
        when(taskRepository.findByTitleAndUser_Id(title, user.getId())).thenReturn(Optional.empty());
        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class, () -> taskService.findTaskByTitle(title));

        assertEquals("Belirtilen başlığa sahip görev bulunamadı: " + title, exception.getMessage());
        verify(taskRepository, times(1)).findByTitleAndUser_Id(title, user.getId());
        verify(taskMapper, never()).toResponse(any(Task.class));

    }

    @Test
    void findAllTasks_shouldReturnTasksOfCurrentUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("1@gmail.com");

        Task task = new Task();
        task.setId(10L);
        task.setTitle("test");
        task.setUser(user);

        Task task2 = new Task();
        task2.setId(20L);
        task2.setTitle("test2");
        task2.setUser(user);


        TaskResponse response = new TaskResponse();
        response.setId(10L);
        response.setTitle("test");
        TaskResponse response2 = new TaskResponse();
        response2.setId(20L);
        response2.setTitle("test2");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "1@gmail.com", null, java.util.Collections.emptyList()
                )
        );
        when(userRepository.findByEmail("1@gmail.com")).thenReturn(Optional.of(user));
        when(taskRepository.findAllByUser_Id(user.getId())).thenReturn(Arrays.asList(task, task2));
        when(taskMapper.toResponse(task)).thenReturn(response);
        when(taskMapper.toResponse(task2)).thenReturn(response2);

        List<TaskResponse> result = taskService.findAllTasks();

        assertEquals(2, result.size());
        assertEquals("test", result.get(0).getTitle());
        assertEquals("test2", result.get(1).getTitle());
        verify(taskMapper, times(1)).toResponse(task);
        verify(taskMapper, times(1)).toResponse(task2);


    }



    @Test
    void findAllTasks_shouldReturnEmptyListWhenUserHasNoTasks()
    {
User user = new User();
    user.setId(1L);
    user.setEmail("1@gmail.com");


SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken("1@gmail.com", null, java.util.Collections.emptyList())

);
when(userRepository.findByEmail("1@gmail.com")).thenReturn(Optional.of(user));
when(taskRepository.findAllByUser_Id(user.getId())).thenReturn(List.of());

List<TaskResponse> result = taskService.findAllTasks();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(taskRepository, times(1))
                .findAllByUser_Id(user.getId());

        // Görev olmadığı için mapper hiç çağrılmamalı
        verifyNoInteractions(taskMapper);

    }

    @Test
    void updateCompleted_shouldUpdateCompletedStatusSuccessfully() {
        Long taskId = 10L;
        Boolean newCompletedStatus = true;

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        Task task = new Task();
        task.setId(taskId);
        task.setTitle("Test Görevi");
        task.setDescription("Test Açıklama");
        task.setCompleted(false);
        task.setUser(user);

        TaskResponse response = new TaskResponse();
        response.setId(taskId);
        response.setTitle("Test Görevi");
        response.setDescription("Test Açıklama");
        response.setCompleted(newCompletedStatus);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("test@example.com", null, Collections.emptyList())
        );

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(taskRepository.findByIdAndUser_Id(taskId, user.getId())).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskMapper.toResponse(any(Task.class))).thenReturn(response);

        // Act
        TaskResponse result = taskService.updateCompleted(taskId, newCompletedStatus);

        // Assert
        assertNotNull(result);
        assertEquals(taskId, result.getId());
        assertTrue(result.getCompleted());
        assertTrue(task.getCompleted());

        verify(taskRepository, times(1)).findByIdAndUser_Id(taskId, user.getId());
        verify(taskRepository, times(1)).save(task);
        verify(taskMapper, times(1)).toResponse(task);
    }

    @Test
    void updateCompleted_shouldThrowExceptionWhenTaskNotFound() {
        Long taskId = 99L;

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("test@example.com", null, Collections.emptyList())
        );

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(taskRepository.findByIdAndUser_Id(taskId, user.getId())).thenReturn(Optional.empty());

        // Act & Assert
        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class,
                () -> taskService.updateCompleted(taskId, true));

        assertEquals("Belirtilen ID numarasına sahip görev bulunamadı: " + taskId, exception.getMessage());

        verify(taskRepository, times(1)).findByIdAndUser_Id(taskId, user.getId());
        verify(taskRepository, never()).save(any(Task.class));
        verify(taskMapper, never()).toResponse(any(Task.class));
    }

    @Test
    void updateCompleted_shouldThrowExceptionWhenCurrentUserNotFound() {
        Long taskId = 10L;

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("test@example.com", null, Collections.emptyList())
        );

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> taskService.updateCompleted(taskId, true));

        assertEquals("Kullanıcı bulunamadı", exception.getMessage());

        verify(taskRepository, never()).findByIdAndUser_Id(anyLong(), anyLong());
        verify(taskRepository, never()).save(any(Task.class));
        verify(taskMapper, never()).toResponse(any(Task.class));
    }

}
