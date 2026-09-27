package com.berkay.todo.repository;

import com.berkay.todo.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {


    Task findTaskByTitleIs(String title);
}
