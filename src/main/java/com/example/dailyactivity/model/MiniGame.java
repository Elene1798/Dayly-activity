package com.example.dailyactivity.model;

import jakarta.persistence.*;

@Entity
@Table(name = "mini_games")
public class MiniGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(name = "game_type", nullable = false, length = 50)
    private String gameType;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false)
    private int duration;

    @Column(nullable = false)
    private boolean active = true;

    @Column(length = 2000)
    private String instructions;

    public MiniGame() {
    }

    public MiniGame(
            String title,
            String description,
            String gameType,
            String category,
            int duration,
            String instructions
    ) {
        this.title = title;
        this.description = description;
        this.gameType = gameType;
        this.category = category;
        this.duration = duration;
        this.instructions = instructions;
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
}