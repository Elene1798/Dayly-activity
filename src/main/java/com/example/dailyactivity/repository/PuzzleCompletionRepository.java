package com.example.dailyactivity.repository;

import com.example.dailyactivity.model.Puzzle;
import com.example.dailyactivity.model.PuzzleCompletion;
import com.example.dailyactivity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PuzzleCompletionRepository
        extends JpaRepository<PuzzleCompletion, Long> {

    Optional<PuzzleCompletion> findByUserAndPuzzleAndGridSize(
            User user, Puzzle puzzle, int gridSize);

    List<PuzzleCompletion> findByUser(User user);

    long countByUser(User user);

    long countByUserAndGridSize(User user, int gridSize);
}
