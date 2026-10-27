package com.example.dailyactivity.service;

import com.example.dailyactivity.model.AchievementType;
import com.example.dailyactivity.model.User;
import com.example.dailyactivity.model.UserAchievement;
import com.example.dailyactivity.repository.UserAchievementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;

@Service
public class AchievementService {

    private final UserAchievementRepository userAchievementRepository;

    public AchievementService(
            UserAchievementRepository userAchievementRepository) {
        this.userAchievementRepository = userAchievementRepository;
    }

    /**
     * Выдать достижение пользователю, если он ещё его не получил.
     */
    @Transactional
    public boolean award(User user, AchievementType achievementType) {

        if (userAchievementRepository.existsByUserAndAchievementKey(
                user,
                achievementType.getKey())) {

            return false;
        }

        UserAchievement achievement = new UserAchievement(
                user,
                achievementType.getKey(),
                LocalDateTime.now()
        );

        userAchievementRepository.save(achievement);

        return true;
    }

    /**
     * Забирает одно новое уведомление и сразу фиксирует в базе,
     * что поздравление уже было показано. Старые записи с null
     * не попадут в выборку.
     */
    @Transactional
    public AchievementType claimNextNotification(User user) {

        return userAchievementRepository
                .findFirstByUserAndNotificationShownFalseOrderByEarnedAtAsc(user)
                .map(achievement -> {
                    achievement.setNotificationShown(true);
                    userAchievementRepository.save(achievement);

                    return Arrays.stream(AchievementType.values())
                            .filter(type -> type.getKey().equals(
                                    achievement.getAchievementKey()))
                            .findFirst()
                            .orElse(null);
                })
                .orElse(null);
    }

    /**
     * Все полученные достижения пользователя.
     */
    @Transactional(readOnly = true)
    public List<UserAchievement> getUserAchievements(User user) {
        return userAchievementRepository
                .findByUserOrderByEarnedAtAsc(user);
    }

    /**
     * Получено ли конкретное достижение.
     */
    @Transactional(readOnly = true)
    public boolean hasAchievement(
            User user,
            AchievementType achievementType) {

        return userAchievementRepository.existsByUserAndAchievementKey(
                user,
                achievementType.getKey()
        );
    }

    /**
     * Количество полученных достижений.
     */
    @Transactional(readOnly = true)
    public long countUserAchievements(User user) {
        return userAchievementRepository.countByUser(user);
    }
}