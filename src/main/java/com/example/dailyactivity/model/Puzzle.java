package com.example.dailyactivity.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "puzzles")
public class Puzzle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "image_file", nullable = false, length = 255)
    private String imageFile;

    /** Размер сетки по умолчанию: 3, 4, 5 или 6. */
    @Column(name = "default_size", nullable = false)
    private int defaultSize = 3;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** Привязка к мини-игре типа PUZZLE. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mini_game_id")
    private MiniGame miniGame;

    public Puzzle() {
    }

    public Puzzle(String title, String imageFile, int defaultSize) {
        this.title = title;
        this.imageFile = imageFile;
        this.defaultSize = defaultSize;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getImageFile() { return imageFile; }
    public void setImageFile(String imageFile) { this.imageFile = imageFile; }
    public int getDefaultSize() { return defaultSize; }
    public void setDefaultSize(int defaultSize) { this.defaultSize = defaultSize; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public MiniGame getMiniGame() { return miniGame; }
    public void setMiniGame(MiniGame miniGame) { this.miniGame = miniGame; }
}
