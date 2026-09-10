package com.example.dailyactivity.model;

import jakarta.persistence.*;

@Entity
@Table(name = "activities")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 2000)
    private String description;

    private String category;

    private int duration;

    private String location;

    @Column(length = 5000)
    private String instructions;

    @Column(length = 2000)
    private String benefit;

    @Column(length = 2000)
    private String interestingFact;

    @Column(length = 2000)
    private String imageUrl;

    @Column(length = 2000)
    private String linkUrl;

    @Column(length = 2000)
    private String videoUrl;

    public Activity() {
    }

    // Старый конструктор оставляем,
    // чтобы существующие занятия в DataInitializer продолжили работать
    public Activity(
            String title,
            String description,
            String category,
            int duration,
            String location) {

        this.title = title;
        this.description = description;
        this.category = category;
        this.duration = duration;
        this.location = location;
    }

    // Новый конструктор для подробных занятий
    public Activity(
            String title,
            String description,
            String category,
            int duration,
            String location,
            String instructions,
            String benefit,
            String interestingFact,
            String imageUrl,
            String linkUrl,
            String videoUrl) {

        this.title = title;
        this.description = description;
        this.category = category;
        this.duration = duration;
        this.location = location;
        this.instructions = instructions;
        this.benefit = benefit;
        this.interestingFact = interestingFact;
        this.imageUrl = imageUrl;
        this.linkUrl = linkUrl;
        this.videoUrl = videoUrl;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public int getDuration() {
        return duration;
    }

    public String getLocation() {
        return location;
    }

    public String getInstructions() {
        return instructions;
    }

    public String getBenefit() {
        return benefit;
    }

    public String getInterestingFact() {
        return interestingFact;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public void setBenefit(String benefit) {
        this.benefit = benefit;
    }

    public void setInterestingFact(String interestingFact) {
        this.interestingFact = interestingFact;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}