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

        // 1. Идеальное совпадение:
        // категория + место + подходящее время

        List<Activity> activities =
                activityRepository
                        .findByCategoryAndLocationAndDurationLessThanEqual(
                                category,
                                location,
                                maxDuration
                        );

        if (!activities.isEmpty()) {
            return getRandomActivityFromList(activities);
        }


        // 2. Если ничего нет —
        // ищем категорию + место,
        // но уже без ограничения по времени

        activities =
                activityRepository
                        .findByCategoryAndLocation(
                                category,
                                location
                        );

        if (!activities.isEmpty()) {
            return getRandomActivityFromList(activities);
        }


        // 3. Если и этого нет —
        // ищем просто категорию

        activities =
                activityRepository.findByCategory(category);

        if (!activities.isEmpty()) {
            return getRandomActivityFromList(activities);
        }


        // 4. Вообще ничего не нашли

        return null;
    }


    private Activity getRandomActivityFromList(
            List<Activity> activities) {

        int randomIndex =
                random.nextInt(activities.size());

        return activities.get(randomIndex);
    }
}


