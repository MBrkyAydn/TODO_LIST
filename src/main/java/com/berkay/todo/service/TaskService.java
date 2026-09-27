package com.berkay.todo.service;


import com.berkay.todo.dto.request.TaskRequest;
import com.berkay.todo.dto.response.TaskResponse;
import com.berkay.todo.entity.Task;
import com.berkay.todo.exception.TaskNotFoundException;
import com.berkay.todo.mapper.TaskMapper;
import com.berkay.todo.repository.TaskRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskMapper taskMapper;
    private final TaskRepository taskRepository;

    public TaskResponse createTask(TaskRequest taskRequest) {
        Task task = taskMapper.toEntity(taskRequest);
        Task savedTask = taskRepository.save(task);
        return taskMapper.toResponse(savedTask);

    }

    public TaskResponse updateTask(TaskRequest taskRequest, Long id) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));

        task.setTitle(taskRequest.getTitle());
        task.setDescription(taskRequest.getDescription());
        task.setCompleted(taskRequest.getCompleted());
        Task updatedTask = taskRepository.save(task);
        return taskMapper.toResponse(updatedTask);
    }

    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
        taskRepository.delete(task);


    }

    public List<TaskResponse> findAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(taskMapper::toResponse)
                .toList();

    }

    public TaskResponse findTaskById(Long id) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));

        return taskMapper.toResponse(task);

    }

    public TaskResponse findTaskByTitle(String title) {
        Task task = taskRepository.findTaskByTitleIs(title);
        if (task == null) {
            throw new TaskNotFoundException("Task not found with title: " + title);
        }
        return taskMapper.toResponse(task);

    }

    public TaskResponse updateCompleted(Long id, Boolean completed) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
        task.setCompleted(completed);
        Task updatedTask = taskRepository.save(task);
        return taskMapper.toResponse(updatedTask);

    }


}

