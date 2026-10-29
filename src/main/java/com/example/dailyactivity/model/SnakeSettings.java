package com.example.dailyactivity.model;

import jakarta.persistence.*;

@Entity
@Table(name = "snake_settings")
public class SnakeSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "control_mode", nullable = false, length = 20)
    private String controlMode = "swipe";

    public SnakeSettings() {}

    public SnakeSettings(User user, String controlMode) {
        this.user = user;
        this.controlMode = controlMode;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getControlMode() { return controlMode; }
    public void setControlMode(String controlMode) { this.controlMode = controlMode; }
}