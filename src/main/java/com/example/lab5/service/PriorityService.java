package com.example.lab5.service;

import com.example.lab5.model.Priority;
import com.example.lab5.repository.PriorityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PriorityService {

    @Autowired
    private PriorityRepository taskPriorityRepository;

    public List<Priority> getAllPriorities() {
        return taskPriorityRepository.findAll();
    }

    public Optional<Priority> getPriorityById(Long priorityId) {
        return taskPriorityRepository.findById(priorityId);
    }

    public Priority addPriority(Priority taskPriority) {
        if (taskPriorityRepository.findByPriority(taskPriority.getPriority()).isPresent()) {
            throw new IllegalArgumentException("Task priority already exists");
        }
        return taskPriorityRepository.save(taskPriority);
    }

    public void deletePriority(Long priorityId) {
        taskPriorityRepository.deleteById(priorityId);
    }
}
