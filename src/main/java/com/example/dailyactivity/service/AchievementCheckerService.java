package com.example.dailyactivity.service;

import com.example.dailyactivity.model.AchievementType;
import com.example.dailyactivity.model.User;
import com.example.dailyactivity.repository.ActivityCompletionRepository;
import org.springframework.stereotype.Service;

@Service
public class AchievementCheckerService {

    private final AchievementService achievementService;
    private final VisitStreakService visitStreakService;
    private final ActivityCompletionRepository activityCompletionRepository;

    public AchievementCheckerService(
            AchievementService achievementService,
            VisitStreakService visitStreakService,
            ActivityCompletionRepository activityCompletionRepository) {

        this.achievementService = achievementService;
        this.visitStreakService = visitStreakService;
        this.activityCompletionRepository = activityCompletionRepository;
    }

    /**
     * Проверяет достижения за серию ежедневных посещений.
     */
    public void checkVisitAchievements(User user) {

        int streak = visitStreakService.getCurrentStreak(user);

        checkVisitAchievement(
                user,
                streak,
                5,
                AchievementType.VISIT_5
        );

        checkVisitAchievement(
                user,
                streak,
                30,
                AchievementType.VISIT_30
        );

        checkVisitAchievement(
                user,
                streak,
                100,
                AchievementType.VISIT_100
        );

        checkVisitAchievement(
                user,
                streak,
                150,
                AchievementType.VISIT_150
        );

        checkVisitAchievement(
                user,
                streak,
                200,
                AchievementType.VISIT_200
        );

        checkVisitAchievement(
                user,
                streak,
                250,
                AchievementType.VISIT_250
        );

        checkVisitAchievement(
                user,
                streak,
                365,
                AchievementType.VISIT_365
        );

        checkVisitAchievement(
                user,
                streak,
                730,
                AchievementType.VISIT_730
        );

        checkVisitAchievement(
                user,
                streak,
                1095,
                AchievementType.VISIT_1095
        );

        checkVisitAchievement(
                user,
                streak,
                1460,
                AchievementType.VISIT_1460
        );

        checkVisitAchievement(
                user,
                streak,
                1825,
                AchievementType.VISIT_1825
        );

        checkVisitAchievement(
                user,
                streak,
                2190,
                AchievementType.VISIT_2190
        );

        checkVisitAchievement(
                user,
                streak,
                2555,
                AchievementType.VISIT_2555
        );
    }

    private void checkVisitAchievement(
            User user,
            int streak,
            int requiredDays,
            AchievementType achievementType) {

        if (streak >= requiredDays) {
            achievementService.award(
                    user,
                    achievementType
            );
        }
    }

    public void checkCompletionAchievements(User user) {

        long completedCount =
                activityCompletionRepository.countByUser(user);

        checkCompletionAchievement(
                user,
                completedCount,
                10,
                AchievementType.COMPLETED_10
        );

        checkCompletionAchievement(
                user,
                completedCount,
                25,
                AchievementType.COMPLETED_25
        );

        checkCompletionAchievement(
                user,
                completedCount,
                50,
                AchievementType.COMPLETED_50
        );

        checkCompletionAchievement(
                user,
                completedCount,
                100,
                AchievementType.COMPLETED_100
        );

        checkCompletionAchievement(
                user,
                completedCount,
                250,
                AchievementType.COMPLETED_250
        );

        checkCompletionAchievement(
                user,
                completedCount,
                500,
                AchievementType.COMPLETED_500
        );

        checkCompletionAchievement(
                user,
                completedCount,
                1000,
                AchievementType.COMPLETED_1000
        );

        checkCompletionAchievement(
                user,
                completedCount,
                2500,
                AchievementType.COMPLETED_2500
        );

        checkCompletionAchievement(
                user,
                completedCount,
                5000,
                AchievementType.COMPLETED_5000
        );

        checkCompletionAchievement(
                user,
                completedCount,
                10000,
                AchievementType.COMPLETED_10000
        );

        long nightCount =
                activityCompletionRepository.countNightCompletions(
                        user,
                        22,
                        6
                );

        if (nightCount >= 5) {
            achievementService.award(
                    user,
                    AchievementType.NIGHT_OWL
            );
        }

        long earlyCount =
                activityCompletionRepository.countEarlyCompletions(
                        user,
                        8
                );

        if (earlyCount >= 1) {
            achievementService.award(
                    user,
                    AchievementType.EARLY_START
            );
        }

        long randomCount =
                activityCompletionRepository.countByUserAndSource(
                        user,
                        "RANDOM"
                );

        if (randomCount >= 20) {
            achievementService.award(
                    user,
                    AchievementType.ADVENTURER
            );
        }

        long completedLocations =
                activityCompletionRepository
                        .countDistinctCompletedLocations(user);

        long availableLocations =
                activityCompletionRepository
                        .countAvailableLocations();

        if (availableLocations > 0
                && completedLocations >= availableLocations) {

            achievementService.award(
                    user,
                    AchievementType.EXPLORER
            );
        }

        long outsideCount = activityCompletionRepository.countOutsideActivities(
                user
        );

        if (outsideCount >= 10) {
            achievementService.award(
                    user,
                    AchievementType.FRESH_AIR
            );
        }

        long creativeCount = activityCompletionRepository.countCreativeActivity(
                user
        );

        if (creativeCount >= 10) {
            achievementService.award(
                    user,
                    AchievementType.CREATIVE_NATURE
            );
        }

        long learningCount = activityCompletionRepository.countLearningActivity(
                user
        );

        if (learningCount >= 10) {
            achievementService.award(
                    user,
                    AchievementType.CURIOUS
            );
        }

        long activeCount = activityCompletionRepository.countActive(
                user
        );

        if (activeCount >= 25) {
            achievementService.award(
                    user,
                    AchievementType.ACTIVIST
            );
        }

        long totalCount = activityCompletionRepository.countTotalCompletedMinutes(
                user
        );

        if (totalCount >= 6000) {
            achievementService.award(
                    user,
                    AchievementType.HUNDRED_HOURS
            );
        }


    }

    private void checkCompletionAchievement(
            User user,
            long completedCount,
            long requiredCount,
            AchievementType achievementType) {

        if (completedCount >= requiredCount) {
            achievementService.award(
                    user,
                    achievementType
            );


        }
    }
}