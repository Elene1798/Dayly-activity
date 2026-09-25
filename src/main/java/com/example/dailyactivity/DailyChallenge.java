package com.example.dailyactivity;

import com.example.dailyactivity.model.Activity;
import com.example.dailyactivity.model.User;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "daily_challenges",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"user_id", "challenge_date"}
        )
)
public class DailyChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @Column(name = "challenge_date", nullable = false)
    private LocalDate challengeDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengeStatus status;

    public DailyChallenge() {
    }

    public DailyChallenge(
            User user,
            Activity activity,
            LocalDate challengeDate,
            ChallengeStatus status) {

        this.user = user;
        this.activity = activity;
        this.challengeDate = challengeDate;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Activity getActivity() {
        return activity;
    }

    public LocalDate getChallengeDate() {
        return challengeDate;
    }

    public ChallengeStatus getStatus() {
        return status;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setChallengeDate(LocalDate challengeDate) {
        this.challengeDate = challengeDate;
    }

    public void setStatus(ChallengeStatus status) {
        this.status = status;
    }
}
