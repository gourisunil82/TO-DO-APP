package com.todo.repository;

import com.todo.model.Task;
import com.todo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserOrderByCreatedAtDesc(User user);
    List<Task> findByUserAndCompletedOrderByCreatedAtDesc(User user, boolean completed);
    long countByUserAndCompleted(User user, boolean completed);
}