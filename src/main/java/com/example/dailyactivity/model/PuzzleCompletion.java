package com.example.dailyactivity.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "puzzle_completions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_puzzle_user_size",
                columnNames = {"user_id", "puzzle_id", "grid_size"}
        )
)
public class PuzzleCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "puzzle_id", nullable = false)
    private Puzzle puzzle;

    @Column(name = "grid_size", nullable = false)
    private int gridSize;

    @Column(name = "moves_count", nullable = false)
    private int movesCount;

    @Column(name = "time_seconds", nullable = false)
    private int timeSeconds;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    public PuzzleCompletion() {
    }

    public PuzzleCompletion(
            User user,
            Puzzle puzzle,
            int gridSize,
            int movesCount,
            int timeSeconds
    ) {
        this.user = user;
        this.puzzle = puzzle;
        this.gridSize = gridSize;
        this.movesCount = movesCount;
        this.timeSeconds = timeSeconds;
        this.completedAt = LocalDateTime.now();
    }

    // Геттеры

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Puzzle getPuzzle() { return puzzle; }
    public int getGridSize() { return gridSize; }
    public int getMovesCount() { return movesCount; }
    public int getTimeSeconds() { return timeSeconds; }
    public LocalDateTime getCompletedAt() { return completedAt; }
}
