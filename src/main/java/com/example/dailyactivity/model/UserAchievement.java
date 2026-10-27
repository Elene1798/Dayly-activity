package com.example.dailyactivity.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_achievements",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_achievement",
                        columnNames = {"user_id", "achievement_key"}
                )
        }
)
public class UserAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "achievement_key", nullable = false, length = 100)
    private String achievementKey;

    @Column(name = "earned_at", nullable = false)
    private LocalDateTime earnedAt;

    /**
     * null у старых записей означает, что достижение было получено
     * до появления уведомлений и не должно показываться задним числом.
     * У новых достижений значение false до первого показа.
     */
    @Column(name = "notification_shown")
    private Boolean notificationShown;


    public UserAchievement() {
    }


    public UserAchievement(
            User user,
            String achievementKey,
            LocalDateTime earnedAt) {

        this.user = user;
        this.achievementKey = achievementKey;
        this.earnedAt = earnedAt;
        this.notificationShown = false;
    }


    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getAchievementKey() {
        return achievementKey;
    }

    public LocalDateTime getEarnedAt() {
        return earnedAt;
    }

    public Boolean getNotificationShown() {
        return notificationShown;
    }

    public void setNotificationShown(Boolean notificationShown) {
        this.notificationShown = notificationShown;
    }
}