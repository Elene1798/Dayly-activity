package com.example.dailyactivity.service;

import com.example.dailyactivity.ChallengeStatus;
import com.example.dailyactivity.DailyChallenge;
import com.example.dailyactivity.model.Activity;
import com.example.dailyactivity.model.User;
import com.example.dailyactivity.repository.ActivityRepository;
import com.example.dailyactivity.repository.DailyChallengeRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Service
public class ChallengeService {

    private final DailyChallengeRepository dailyChallengeRepository;
    private final ActivityRepository activityRepository;

    private final Random random = new Random();

    public ChallengeService(
            DailyChallengeRepository dailyChallengeRepository,
            ActivityRepository activityRepository) {

        this.dailyChallengeRepository = dailyChallengeRepository;
        this.activityRepository = activityRepository;
    }

    public DailyChallenge getTodayChallenge(User user) {

        LocalDate today = LocalDate.now();

        return dailyChallengeRepository
                .findByUserAndChallengeDate(user, today)
                .orElseGet(() -> createTodayChallenge(user, today));
    }

    private DailyChallenge createTodayChallenge(
            User user,
            LocalDate today) {

        List<Activity> activities = activityRepository.findAll();

        if (activities.isEmpty()) {
            return null;
        }

        Activity activity =
                activities.get(random.nextInt(activities.size()));

        DailyChallenge challenge = new DailyChallenge(
                user,
                activity,
                today,
                ChallengeStatus.AVAILABLE
        );

        return dailyChallengeRepository.save(challenge);
    }

    public void startChallenge(DailyChallenge challenge) {

        if (challenge.getStatus() == ChallengeStatus.AVAILABLE) {
            challenge.setStatus(ChallengeStatus.IN_PROGRESS);
            dailyChallengeRepository.save(challenge);
        }
    }

    public void declineChallenge(DailyChallenge challenge) {

        if (challenge.getStatus() == ChallengeStatus.AVAILABLE) {
            challenge.setStatus(ChallengeStatus.DECLINED);
            dailyChallengeRepository.save(challenge);
        }
    }

    public void completeChallenge(@NonNull DailyChallenge challenge) {

        if (challenge.getStatus() == ChallengeStatus.IN_PROGRESS) {
            challenge.setStatus(ChallengeStatus.COMPLETED);
            dailyChallengeRepository.save(challenge);
        }
    }

    public long getCompletedCount(User user) {

        return dailyChallengeRepository.countByUserAndStatus(
                user,
                ChallengeStatus.COMPLETED
        );
    }
}