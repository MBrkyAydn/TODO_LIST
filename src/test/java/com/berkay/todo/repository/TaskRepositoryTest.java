package com.berkay.todo.repository;


import com.berkay.todo.entity.Task;
import com.berkay.todo.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class TaskRepositoryTest {
    @Autowired
    TaskRepository taskRepository;
    @Autowired
    UserRepository userRepository;

    @Test
    void saveTask_shouldSaveAndFindTask() {

        Task task = new Task();
        task.setTitle("Task1");
        task.setDescription("Task Description");
        task.setCompleted(false);


        Task savedTask = taskRepository.save(task);

        assertNotNull(savedTask.getId());


        Task foundTask = taskRepository.findById(savedTask.getId()).orElseThrow();

        assertEquals("Task1", foundTask.getTitle());
        assertEquals("Task Description",
                foundTask.getDescription());
        assertEquals(false, foundTask.getCompleted());

    }

    @Test
    void existsBytTitle_shouldReturnTrueWhenTitleExists() {
        Task task = new Task();
        task.setTitle("Task1");
        task.setCompleted(false);

        taskRepository.save(task);


        boolean exists = taskRepository.existsByTitle("Task1");
        assertTrue(exists);


    }

    @Test
    void existsByTitle_shouldReturnFalseWhenTitleDoesNotExist() {
        String title = "Olmayan görev";
        boolean exists = taskRepository.existsByTitle(title);

        assertFalse(exists);


    }

    @Test
    void existsByTitleAndIdNot_shouldReturnTrueWhenAnotherTaskHasSameTitle() {

        Task task = new Task();
        task.setTitle("Task");
        task.setCompleted(false);
        taskRepository.save(task);

        Task task2 = new Task();
        task2.setTitle("Task");
        task2.setCompleted(false);
        taskRepository.save(task2);
        boolean exists = taskRepository.existsByTitleAndIdNot("Task", task.getId());

        assertTrue(exists);


    }

    @Test
    void existsByTitleAndIdNot_shouldReturnFalseWhenOnlySameTaskExists() {
        Task task = new Task();
        task.setTitle("Task");
        task.setCompleted(false);
        taskRepository.save(task);
        boolean exists = taskRepository.existsByTitleAndIdNot("Task", task.getId());

        assertFalse(exists);
    }


    @Test
    void findByTitleAndUser_Id_shouldReturnTaskWhenTitleAndUserMatch() {
        // Arrange
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("123456");
        user = userRepository.save(user);

        Task task = new Task();
        task.setTitle("Kitap oku");
        task.setCompleted(false);
        task.setUser(user);
        taskRepository.save(task);

        // Act
        var result = taskRepository.findByTitleAndUser_Id(
                "Kitap oku",
                user.getId()
        );

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Kitap oku", result.get().getTitle());
    }

    @Test
    void findByTitleAndUser_Id_shouldReturnEmptyWhenUserDoesNotOwnTask() {
        // Arrange
        User owner = new User();
        owner.setEmail("owner@example.com");
        owner.setPassword("123456");
        owner = userRepository.save(owner);

        User anotherUser = new User();
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("123456");
        anotherUser = userRepository.save(anotherUser);

        Task task = new Task();
        task.setTitle("Kitap oku");
        task.setCompleted(false);
        task.setUser(owner);
        taskRepository.save(task);

        // Act
        var result = taskRepository.findByTitleAndUser_Id(
                "Kitap oku",
                anotherUser.getId()
        );

        // Assert
        assertTrue(result.isEmpty());
    }

}
