package com.example.lab5.service;

import com.example.lab5.model.Status;
import com.example.lab5.repository.StatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StatusService {

    @Autowired
    private StatusRepository statusRepository;

    public List<Status> getAllStatuses() {
        return statusRepository.findAll();
    }

    public Optional<Status> getStatusById(Long statusId) {
        return statusRepository.findById(statusId);
    }

    public Optional<Status> getStatusByName(String status) {
        return statusRepository.findByStatus(status);
    }

    public Status addStatus(Status taskStatus) {
        if (statusRepository.findByStatus(taskStatus.getStatus()).isPresent()) {
            throw new IllegalArgumentException("Task status already exists");
        }
        return statusRepository.save(taskStatus);
    }

    public void deleteStatus(Long statusId) {
        statusRepository.deleteById(statusId);
    }
}
