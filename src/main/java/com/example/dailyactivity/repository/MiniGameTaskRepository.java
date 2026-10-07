package com.example.dailyactivity.repository;

import com.example.dailyactivity.model.MiniGame;
import com.example.dailyactivity.model.MiniGameTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MiniGameTaskRepository
        extends JpaRepository<MiniGameTask, Long> {

    List<MiniGameTask> findByMiniGameAndActiveTrue(MiniGame miniGame);

    List<MiniGameTask> findByMiniGameOrderByIdAsc(MiniGame miniGame);
}