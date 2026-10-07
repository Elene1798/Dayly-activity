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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}