package com.example.dailyactivity;

import com.example.dailyactivity.model.Activity;
import com.example.dailyactivity.model.User;
import jakarta.persistence.*;


import java.time.LocalDate;

@Entity
@Table(name = "daily_recommendations")
public class DailyRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @Column(nullable = false)
    private LocalDate recommendationDate;

    public DailyRecommendation() {
    }

    public DailyRecommendation(
            User user,
            Activity activity,
            LocalDate recommendationDate) {

        this.user = user;
        this.activity = activity;
        this.recommendationDate = recommendationDate;
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

    public LocalDate getRecommendationDate() {
        return recommendationDate;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setRecommendationDate(LocalDate recommendationDate) {
        this.recommendationDate = recommendationDate;
    }
}