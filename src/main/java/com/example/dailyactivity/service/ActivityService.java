package com.example.dailyactivity.service;

import com.example.dailyactivity.model.Activity;
import com.example.dailyactivity.repository.ActivityRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final Random random = new Random();

    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public Activity getRandomActivity(
            String category,
            int maxDuration,
            String location) {

        List<Activity> activities;

        // Если пользователь выбрал конкретное место
        if (!"anywhere".equals(location)) {

            // 1. Категория + место + подходящее время
            activities = activityRepository
                    .findByCategoryAndLocationAndDurationLessThanEqual(
                            category,
                            location,
                            maxDuration
                    );

            if (!activities.isEmpty()) {
                return getRandomActivityFromList(activities);
            }

            // 2. Категория + любое место + подходящее время
            activities = activityRepository
                    .findByCategoryAndLocationAndDurationLessThanEqual(
                            category,
                            "anywhere",
                            maxDuration
                    );

            if (!activities.isEmpty()) {
                return getRandomActivityFromList(activities);
            }

            // 3. Категория + выбранное место,
            // даже если занятие дольше указанного времени
            activities = activityRepository
                    .findByCategoryAndLocation(
                            category,
                            location
                    );

            if (!activities.isEmpty()) {
                return getRandomActivityFromList(activities);
            }

            // 4. Категория + любое место
            activities = activityRepository
                    .findByCategoryAndLocation(
                            category,
                            "anywhere"
                    );

            if (!activities.isEmpty()) {
                return getRandomActivityFromList(activities);
            }
        }

        // 5. Просто категория + подходящее время
        activities = activityRepository
                .findByCategoryAndDurationLessThanEqual(
                        category,
                        maxDuration
                );

        if (!activities.isEmpty()) {
            return getRandomActivityFromList(activities);
        }

        // 6. Просто категория
        activities = activityRepository
                .findByCategory(category);

        if (!activities.isEmpty()) {
            return getRandomActivityFromList(activities);
        }

        // 7. Ничего не найдено
        return null;
    }

    private Activity getRandomActivityFromList(
            List<Activity> activities) {

        int randomIndex = random.nextInt(activities.size());

        return activities.get(randomIndex);
    }
}