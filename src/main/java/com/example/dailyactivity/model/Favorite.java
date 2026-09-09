package com.example.dailyactivity.model;

import jakarta.persistence.*;

@Entity
@Table(
        name = "favorites",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "activity_id"})
        }
)
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "activity_id")
    private Activity activity;

    public Favorite() {
    }

    public Favorite(User user, Activity activity) {
        this.user = user;
        this.activity = activity;
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
}