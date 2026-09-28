package com.example.dailyactivity.service;

import com.example.dailyactivity.ChallengeStatus;
import com.example.dailyactivity.DailyChallenge;
import com.example.dailyactivity.model.ActivityCompletion;
import com.example.dailyactivity.model.Activity;
import com.example.dailyactivity.model.User;
import com.example.dailyactivity.repository.ActivityCompletionRepository;
import com.example.dailyactivity.repository.ActivityRepository;
import com.example.dailyactivity.repository.DailyChallengeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class ChallengeService {

    private final DailyChallengeRepository dailyChallengeRepository;
    private final ActivityRepository activityRepository;
    private final ActivityCompletionRepository activityCompletionRepository;

    private final Random random = new Random();

    public ChallengeService(
            DailyChallengeRepository dailyChallengeRepository,
            ActivityRepository activityRepository,
            ActivityCompletionRepository activityCompletionRepository) {

        this.dailyChallengeRepository = dailyChallengeRepository;
        this.activityRepository = activityRepository;
        this.activityCompletionRepository = activityCompletionRepository;
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

    @Transactional
    public void completeChallenge(DailyChallenge challenge) {

        // Не создаём повторное выполнение для уже завершённого задания.
        if (challenge == null
                || challenge.getStatus() != ChallengeStatus.IN_PROGRESS) {
            return;
        }

        LocalDateTime completedAt = LocalDateTime.now();

        challenge.setStatus(ChallengeStatus.COMPLETED);
        challenge.setCompletedAt(completedAt);

        dailyChallengeRepository.save(challenge);

        Activity activity = challenge.getActivity();

        if (activity == null) {
            throw new IllegalStateException(
                    "У ежедневного задания не найдено занятие"
            );
        }

        // Дополнительная проверка на случай повторного запроса.
        if (!activityCompletionRepository
                .existsByDailyChallengeId(challenge.getId())) {

            ActivityCompletion completion = new ActivityCompletion(
                    challenge.getUser(),
                    activity,
                    completedAt,
                    "CHALLENGE",
                    challenge.getId()
            );

            activityCompletionRepository.save(completion);
        }
    }

    public long getCompletedCount(User user) {
        return dailyChallengeRepository.countByUserAndStatus(
                user,
                ChallengeStatus.COMPLETED
        );
    }
}