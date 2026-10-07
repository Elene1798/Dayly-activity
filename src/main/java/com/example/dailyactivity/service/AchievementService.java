package com.example.dailyactivity.service;

import com.example.dailyactivity.model.AchievementType;
import com.example.dailyactivity.model.User;
import com.example.dailyactivity.model.UserAchievement;
import com.example.dailyactivity.repository.UserAchievementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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