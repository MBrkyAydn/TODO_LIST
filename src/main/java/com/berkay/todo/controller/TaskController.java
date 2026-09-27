package com.berkay.todo.controller;

import com.berkay.todo.dto.request.TaskRequest;
import com.berkay.todo.dto.request.TaskStatusRequest;
import com.berkay.todo.dto.response.TaskResponse;
import com.berkay.todo.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/tasks")
@RestController
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(@Valid @RequestBody TaskRequest taskRequest) {
        return taskService.createTask(taskRequest);

    }

    @GetMapping
    public List<TaskResponse> getAllTasks() {
        return taskService.findAllTasks();
    }

    @GetMapping("/{id}")
    public TaskResponse getTaskById(@PathVariable Long id) {
        return taskService.findTaskById(id);
    }

    @PutMapping("/{id}")
    public TaskResponse updateTask(@PathVariable Long id, @Valid @RequestBody TaskRequest taskRequest) {
        return taskService.updateTask(taskRequest, id);

    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);

    }

    @GetMapping("/title/{title}")
    public TaskResponse getTaskByTitle(@PathVariable String title) {
        return taskService.findTaskByTitle(title);

    }
    @PatchMapping("/{id}")
    public TaskResponse updateTaskCompleted(@PathVariable Long id, @Valid @RequestBody TaskStatusRequest request) {
        return taskService.updateCompleted(id, request.getCompleted());
    }

}
