package com.example.dailyactivity;

import com.example.dailyactivity.model.AchievementType;

public class AchievementView {

    private final AchievementType type;
    private final boolean earned;

    public AchievementView(
            AchievementType type,
            boolean earned
    ) {
        this.type = type;
        this.earned = earned;
    }

    public AchievementType getType() {
        return type;
    }

    public boolean isEarned() {
        return earned;
    }
}