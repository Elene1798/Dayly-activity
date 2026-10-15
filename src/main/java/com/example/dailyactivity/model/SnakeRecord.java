package com.example.dailyactivity.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "snake_records",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_snake_record",
                        columnNames = {"user_id"}
                )
        }
)
public class SnakeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "best_score", nullable = false)
    private int bestScore = 0;

    @Column(name = "achieved_at", nullable = false)
    private LocalDateTime achievedAt;

    public SnakeRecord() {
    }

    public SnakeRecord(User user, int bestScore, LocalDateTime achievedAt) {
        this.user = user;
        this.bestScore = bestScore;
        this.achievedAt = achievedAt;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public int getBestScore() {
        return bestScore;
    }

    public void setBestScore(int bestScore) {
        this.bestScore = bestScore;
    }

    public LocalDateTime getAchievedAt() {
        return achievedAt;
    }

    public void setAchievedAt(LocalDateTime achievedAt) {
        this.achievedAt = achievedAt;
    }
}