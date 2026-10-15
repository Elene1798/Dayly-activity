package com.example.dailyactivity.model;

import jakarta.persistence.*;

@Entity
@Table(name = "advent_activities")
public class AdventActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AdventSeason season;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(length = 5000)
    private String instructions;

    /** Эмодзи, например 🎁. Если null — используется 🎁 по умолчанию. */
    @Column(length = 50)
    private String icon;

    @Column(name = "image_url", length = 2000)
    private String imageUrl;

    @Column(name = "video_url", length = 2000)
    private String videoUrl;

    @Column(name = "link_url", length = 2000)
    private String linkUrl;

    /** Особое ли задание (для 8 марта, 31 октября, 31 декабря). */
    @Column(nullable = false)
    private boolean special = false;

    /**
     * Число месяца (1–31). Заполняется, только если {@code special = true}.
     * Месяц определяется сезоном, год — текущим при генерации.
     */
    @Column(name = "special_day")
    private Integer specialDay;

    public AdventActivity() {
    }

    public AdventActivity(AdventSeason season, String title) {
        this.season = season;
        this.title = title;
    }

    // --- Геттеры и сеттеры ---

    public Long getId() {
        return id;
    }

    public AdventSeason getSeason() {
        return season;
    }

    public void setSeason(AdventSeason season) {
        this.season = season;
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

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public boolean isSpecial() {
        return special;
    }

    public void setSpecial(boolean special) {
        this.special = special;
    }

    public Integer getSpecialDay() {
        return specialDay;
    }

    public void setSpecialDay(Integer specialDay) {
        this.specialDay = specialDay;
    }
}