
package com.example.dailyactivity.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "mini_game_task_completions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_mini_game_task",
                        columnNames = {"user_id", "mini_game_task_id"}
                )
        }
)
public class MiniGameTaskCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mini_game_task_id", nullable = false)
    private MiniGameTask task;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    public MiniGameTaskCompletion() {
    }

    public MiniGameTaskCompletion(
            User user,
            MiniGameTask task,
            LocalDateTime completedAt
    ) {
        this.user = user;
        this.task = task;
        this.completedAt = completedAt;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public MiniGameTask getTask() {
        return task;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}
