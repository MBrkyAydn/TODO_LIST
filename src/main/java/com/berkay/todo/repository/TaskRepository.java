package com.berkay.todo.repository;

import com.berkay.todo.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {


    Optional<Task> findByTitle(String title);


    boolean existsByTitle(String title);

    boolean existsByTitleAndIdNot(String title, Long id);
}
