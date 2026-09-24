package com.example.dailyactivity.repository;

import com.example.dailyactivity.DailyRecommendation;
import com.example.dailyactivity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyRecommendationRepository
        extends JpaRepository<DailyRecommendation, Long> {

    Optional<DailyRecommendation> findByUserAndRecommendationDate(
            User user,
            LocalDate recommendationDate
    );
}