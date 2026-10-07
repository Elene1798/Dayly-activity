package com.example.dailyactivity.service;

import com.example.dailyactivity.model.MiniGame;
import com.example.dailyactivity.model.MiniGameTask;
import com.example.dailyactivity.repository.MiniGameTaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MiniGameTaskService {

    private final MiniGameTaskRepository taskRepository;

    public MiniGameTaskService(MiniGameTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<MiniGameTask> findAllByGame(MiniGame miniGame) {
        return taskRepository.findByMiniGameOrderByIdAsc(miniGame);
    }

    public List<MiniGameTask> findActiveByGame(MiniGame miniGame) {
        return taskRepository.findByMiniGameAndActiveTrue(miniGame);
    }

    public MiniGameTask findById(Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    public MiniGameTask save(MiniGameTask task) {
        return taskRepository.save(task);
    }

    public void delete(Long id) {
        taskRepository.deleteById(id);
    }
}