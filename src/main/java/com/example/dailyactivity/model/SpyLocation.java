package com.example.dailyactivity.model;

import jakarta.persistence.*;

@Entity
@Table(name = "spy_locations")
public class SpyLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // "Пляж", "Школа", "Космос"

    @Column(length = 100)
    private String icon = "📍"; // Иконка для красоты

    @Column(nullable = false)
    private boolean active = true;

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}