package com.example.lab5.repository;

import com.example.lab5.model.Task;
import com.example.lab5.model.User;
import com.example.lab5.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUser(User user);

    Optional<Task> findByIdAndUser(Long id, User user);

    List<Task> findByUserAndStatus(User user, Status status);

    List<Task> findByUserAndStatusIdOrCategoryId(User user, Long statusId, Long categoryId);

    List<Task> findByUserOrderByDueDateAsc(User user);

    List<Task> findByUserAndPriorityId(User user, Long priorityId);
}
