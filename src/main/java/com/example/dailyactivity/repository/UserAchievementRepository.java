package com.example.dailyactivity.repository;

import com.example.dailyactivity.model.User;
import com.example.dailyactivity.model.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAchievementRepository
        extends JpaRepository<UserAchievement, Long> {

    boolean existsByUserAndAchievementKey(
            User user,
            String achievementKey
    );

    List<UserAchievement> findByUserOrderByEarnedAtAsc(
            User user
    );

    long countByUser(User user);
}