package com.example.dailyactivity.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "activity_completions",
        indexes = {
                @Index(
                        name = "idx_completion_user_date",
                        columnList = "user_id, completed_at"
                ),
                @Index(
                        name = "idx_completion_user_activity",
                        columnList = "user_id, activity_id"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_completion_daily_challenge",
                        columnNames = "daily_challenge_id"
                )
        }
)
public class ActivityCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /*
     * ID занятия сохраняем как число, без внешнего ключа.
     * Благодаря этому история не зависит от того,
     * существует ли занятие в таблице activities сейчас.
     */
    @Column(name = "activity_id")
    private Long activityId;

    /*
     * Снимок данных на момент выполнения.
     * Если название или описание занятия изменится,
     * старая запись в календаре останется прежней.
     */
    @Column(name = "snapshot_title", nullable = false, length = 2000)
    private String snapshotTitle;

    @Column(name = "snapshot_duration")
    private Integer snapshotDuration;

    @Column(name = "snapshot_category")
    private String snapshotCategory;

    @Column(name = "snapshot_location")
    private String snapshotLocation;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    /*
     * CARD — выполнение с карточки занятия.
     * CHALLENGE — выполнение ежедневного задания.
     */
    @Column(name = "source", nullable = false, length = 20)
    private String source;

    /*
     * ID ежедневного задания.
     * Заполняется только для source = CHALLENGE.
     * Уникальность не позволит записать одно задание дважды.
     */
    @Column(name = "daily_challenge_id", unique = true)
    private Long dailyChallengeId;

    public ActivityCompletion() {
    }

    public ActivityCompletion(
            User user,
            Activity activity,
            LocalDateTime completedAt,
            String source,
            Long dailyChallengeId) {

        this.user = user;
        this.activityId = activity.getId();
        this.snapshotTitle = activity.getTitle();
        this.snapshotDuration = activity.getDuration();
        this.snapshotCategory = activity.getCategory();
        this.snapshotLocation = activity.getLocation();
        this.completedAt = completedAt;
        this.source = source;
        this.dailyChallengeId = dailyChallengeId;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Long getActivityId() {
        return activityId;
    }

    public String getSnapshotTitle() {
        return snapshotTitle;
    }

    public Integer getSnapshotDuration() {
        return snapshotDuration;
    }

    public String getSnapshotCategory() {
        return snapshotCategory;
    }

    public String getSnapshotLocation() {
        return snapshotLocation;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public String getSource() {
        return source;
    }

    public Long getDailyChallengeId() {
        return dailyChallengeId;
    }
}