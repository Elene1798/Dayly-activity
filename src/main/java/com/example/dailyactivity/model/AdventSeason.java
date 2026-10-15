package com.example.dailyactivity.model;

import java.time.LocalDate;

public enum AdventSeason {

    SPRING("Весна", 3, "🌸"),
    SUMMER("Лето", 7, "☀️"),
    AUTUMN("Осень", 10, "🍂"),
    WINTER("Зима", 12, "❄️");

    private final String label;
    private final int month;
    private final String icon;

    AdventSeason(String label, int month, String icon) {
        this.label = label;
        this.month = month;
        this.icon = icon;
    }

    public String getLabel() {
        return label;
    }

    public int getMonth() {
        return month;
    }

    public String getIcon() {
        return icon;
    }

    public static AdventSeason fromMonth(int month) {
        for (AdventSeason season : values()) {
            if (season.month == month) {
                return season;
            }
        }
        return null;
    }

    public AdventSeason getCurrentSeason() {
        int month = LocalDate.now().getMonthValue();
        AdventSeason season = AdventSeason.fromMonth(month);
        System.out.println("[ADVENT] Month = " + month + ", Season = " + season);
        return season;
    }
}