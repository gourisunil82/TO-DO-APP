package com.todo.service;

import com.todo.model.Task;
import com.todo.model.User;
import com.todo.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {
    
    @Autowired
    private TaskRepository taskRepository;

    public List<Task> getUserTasks(User user) {
        return taskRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<Task> getUserTasksByStatus(User user, boolean completed) {
        return taskRepository.findByUserAndCompletedOrderByCreatedAtDesc(user, completed);
    }

    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    public Task updateTask(Task task) {
        return taskRepository.save(task);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public long getCompletedTaskCount(User user) {
        return taskRepository.countByUserAndCompleted(user, true);
    }

    public long getTotalTaskCount(User user) {
        return taskRepository.countByUserAndCompleted(user, true) + 
               taskRepository.countByUserAndCompleted(user, false);
    }
}