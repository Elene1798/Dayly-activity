package com.example.dailyactivity.repository;

import com.example.dailyactivity.DailyChallenge;
import com.example.dailyactivity.ChallengeStatus;
import com.example.dailyactivity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyChallengeRepository
        extends JpaRepository<DailyChallenge, Long> {

    Optional<DailyChallenge> findByUserAndChallengeDate(
            User user,
            LocalDate challengeDate
    );

    long countByUserAndStatus(
            User user,
            ChallengeStatus status
    );
}