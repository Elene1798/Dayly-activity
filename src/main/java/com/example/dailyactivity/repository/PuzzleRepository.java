package com.example.dailyactivity.repository;

import com.example.dailyactivity.model.MiniGame;
import com.example.dailyactivity.model.Puzzle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PuzzleRepository extends JpaRepository<Puzzle, Long> {

    List<Puzzle> findByMiniGame(MiniGame miniGame);

    List<Puzzle> findByMiniGameAndActiveTrue(MiniGame miniGame);

    List<Puzzle> findAllByOrderByIdDesc();
}