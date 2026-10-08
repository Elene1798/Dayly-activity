package com.example.dailyactivity.model;

import jakarta.persistence.*;

@Entity
@Table(name = "mini_game_tasks")
public class MiniGameTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mini_game_id", nullable = false)
    private MiniGame miniGame;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false, length = 500)
    private String answer;

    @Column(length = 20)
    private String difficulty;

    @Column(length = 20)
    private String taskType;

    @Column(length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private boolean active = true;

    public MiniGameTask() {
    }

    public MiniGameTask(
            MiniGame miniGame,
            String content,
            String answer
    ) {
        this.miniGame = miniGame;
        this.content = content;
        this.answer = answer;
        this.active = true;
    }

    public MiniGameTask(
            MiniGame miniGame,
            String content,
            String answer,
            String imageUrl
    ) {
        this.miniGame = miniGame;
        this.content = content;
        this.answer = answer;
        this.imageUrl = imageUrl;
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public MiniGame getMiniGame() {
        return miniGame;
    }

    public void setMiniGame(MiniGame miniGame) {
        this.miniGame = miniGame;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }
}