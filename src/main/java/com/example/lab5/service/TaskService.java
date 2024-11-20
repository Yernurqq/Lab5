package com.example.lab5.service;

import com.example.lab5.model.Task;
import com.example.lab5.model.User;
import com.example.lab5.repository.TaskRepository;
import com.example.lab5.repository.StatusRepository;
import com.example.lab5.repository.PriorityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private PriorityRepository taskPriorityRepository;

    public Task createTask(Task task, User user) {
        task.setUser(user);
        validateDueDate(task.getDueDate());
        return taskRepository.save(task);
    }

    public List<Task> getAllTasksForUser(User user) {
        return taskRepository.findByUser(user);
    }

    public Optional<Task> getTaskByIdForUser(Long taskId, User user) {
        return taskRepository.findByIdAndUser(taskId, user);
    }

    @Transactional
    public Task updateTask(Long taskId, Task updatedTask, User user) {
        Task existingTask = taskRepository.findByIdAndUser(taskId, user)
                .orElseThrow(() -> new IllegalArgumentException("Task not found or unauthorized access"));

        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setDueDate(validateDueDate(updatedTask.getDueDate()));
        existingTask.setCategory(updatedTask.getCategory());
        existingTask.setStatus(updatedTask.getStatus());
        existingTask.setPriority(updatedTask.getPriority());

        return taskRepository.save(existingTask);
    }

    public void deleteTask(Long taskId, User user) {
        Task task = taskRepository.findByIdAndUser(taskId, user)
                .orElseThrow(() -> new IllegalArgumentException("Task not found or unauthorized access"));
        taskRepository.delete(task);
    }

    public List<Task> getTasksSortedByDueDate(User user) {
        return taskRepository.findByUserOrderByDueDateAsc(user);
    }

    public List<Task> filterTasksByStatusOrCategory(User user, Long statusId, Long categoryId) {
        return taskRepository.findByUserAndStatusIdOrCategoryId(user, statusId, categoryId);
    }

    private LocalDate validateDueDate(LocalDate dueDate) {
        if (dueDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Due date cannot be in the past");
        }
        return dueDate;
    }
}
