package com.example.dailyactivity.service;

import com.example.dailyactivity.model.Activity;
import com.example.dailyactivity.repository.ActivityRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

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
            String location,
            Set<Long> usedIds) {

        List<Activity> activities;

        // Если выбрано "Везде" — учитываем только категорию и время
        if ("anywhere".equals(location)) {
            activities = activityRepository
                    .findByCategoryAndDurationLessThanEqual(
                            category,
                            maxDuration
                    );
        } else {
            // В обычном случае учитываем категорию, место и время
            activities = activityRepository
                    .findByCategoryAndLocationAndDurationLessThanEqual(
                            category,
                            location,
                            maxDuration
                    );
        }

        // Убираем занятия, которые уже показывали
        List<Activity> availableActivities = activities.stream()
                .filter(activity -> !usedIds.contains(activity.getId()))
                .toList();

        // Если подходящих новых занятий больше нет
        if (availableActivities.isEmpty()) {
            return null;
        }

        // Выбираем случайное занятие
        return availableActivities.get(
                random.nextInt(availableActivities.size())
        );
    }

    public Optional<Activity> getActivityById(Long id) {
        return activityRepository.findById(id);
    }
}