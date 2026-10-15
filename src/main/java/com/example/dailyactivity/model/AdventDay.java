package com.example.dailyactivity.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "advent_days",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_advent_day_user_date",
                columnNames = {"user_id", "advent_date"}
        )
)
public class AdventDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "advent_date", nullable = false)
    private LocalDate adventDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "advent_activity_id", nullable = false)
    private AdventActivity activity;

    @Column(nullable = false)
    private boolean opened = false;

    @Column(name = "opened_at")
    private LocalDateTime openedAt;

    @Column(nullable = false)
    private boolean completed = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public AdventDay() {
    }

    public AdventDay(
            User user,
            LocalDate adventDate,
            AdventActivity activity
    ) {
        this.user = user;
        this.adventDate = adventDate;
        this.activity = activity;
    }

    // --- Геттеры и сеттеры ---

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getAdventDate() {
        return adventDate;
    }

    public void setAdventDate(LocalDate adventDate) {
        this.adventDate = adventDate;
    }

    public AdventActivity getActivity() {
        return activity;
    }

    public void setActivity(AdventActivity activity) {
        this.activity = activity;
    }

    public boolean isOpened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    public LocalDateTime getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(LocalDateTime openedAt) {
        this.openedAt = openedAt;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
