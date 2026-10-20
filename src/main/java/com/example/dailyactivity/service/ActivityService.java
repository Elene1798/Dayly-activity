package com.example.dailyactivity.service;

import com.example.dailyactivity.model.Activity;
import com.example.dailyactivity.repository.ActivityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final Random random = new Random();
    private final FileStorageService fileStorage;

    public ActivityService(ActivityRepository activityRepository,
                           FileStorageService fileStorage) {
        this.activityRepository = activityRepository;
        this.fileStorage = fileStorage;
    }

    public Activity getRandomActivity(
            String category,
            Integer maxDuration,
            String location,
            Set<Long> usedIds,
            Set<Long> completedMiniGameActivityIds) {

        List<Activity> activities =
                activityRepository.findByCategory(category);

        List<Activity> availableActivities = activities.stream()

                // Фильтр по времени, если он выбран
                .filter(activity ->
                        maxDuration == null
                                || activity.getDuration() <= maxDuration)

                // Фильтр по месту, если он выбран
                .filter(activity ->
                        location == null
                                || "anywhere".equals(location)
                                || location.equals(activity.getLocation()))

                // Не показываем уже использованные занятия
                .filter(activity ->
                        !usedIds.contains(activity.getId()))

                .filter(activity ->
                        activity.getMiniGame() == null
                                || "MEMORY".equalsIgnoreCase(
                                activity.getMiniGame().getGameType()
                        )
                                || "CROCODILE".equalsIgnoreCase(
                                activity.getMiniGame().getGameType()
                        )
                                || !completedMiniGameActivityIds.contains(
                                activity.getId()
                        )
                )

                .toList();

        if (availableActivities.isEmpty()) {
            return null;
        }

        return availableActivities.get(
                random.nextInt(availableActivities.size())
        );
    }

    public Activity getRandomSurpriseActivity(Set<Long> usedIds) {

        List<Activity> activities =
                activityRepository.findAll();

        List<Activity> availableActivities = activities.stream()

                // "Для двоих" не участвует в "Удиви меня"
                .filter(activity ->
                        !"couple".equals(activity.getCategory()))

                // Не показываем уже использованные занятия
                .filter(activity ->
                        !usedIds.contains(activity.getId()))

                .toList();

        if (availableActivities.isEmpty()) {
            return null;
        }

        return availableActivities.get(
                random.nextInt(availableActivities.size())
        );
    }

    public Activity getRandomCoupleActivity(Set<Long> usedIds) {

        List<Activity> activities =
                activityRepository.findByCategory("couple");

        List<Activity> availableActivities = activities.stream()
                .filter(activity ->
                        !usedIds.contains(activity.getId()))
                .toList();

        if (availableActivities.isEmpty()) {
            return null;
        }

        return availableActivities.get(
                random.nextInt(availableActivities.size())
        );
    }

    public Optional<Activity> getActivityById(Long id) {
        return activityRepository.findById(id);
    }

    @Transactional
    public Activity save(Activity activity, MultipartFile image) {

        if (image != null && !image.isEmpty()) {
            String fileName = fileStorage.save(image, "activities");

            // Если был старый файл — удалить
            if (activity.getImageFile() != null) {
                fileStorage.delete("activities", activity.getImageFile());
            }

            activity.setImageFile(fileName);
        }

        return activityRepository.save(activity);
    }

    /**
     * Удаляет активность и её картинку.
     */
    @Transactional
    public void delete(Long id) {
        Activity activity = activityRepository.findById(id).orElse(null);
        if (activity == null) return;

        if (activity.getImageFile() != null) {
            fileStorage.delete("activities", activity.getImageFile());
        }

        activityRepository.delete(activity);
    }
}

