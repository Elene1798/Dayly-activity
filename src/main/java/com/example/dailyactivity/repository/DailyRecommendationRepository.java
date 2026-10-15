package com.example.dailyactivity.repository;

import com.example.dailyactivity.DailyRecommendation;
import com.example.dailyactivity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyRecommendationRepository
        extends JpaRepository<DailyRecommendation, Long> {

    Optional<DailyRecommendation> findByUserAndRecommendationDate(
            User user,
            LocalDate recommendationDate
    );


    @Modifying
    @Transactional
    @Query("DELETE FROM DailyRecommendation r WHERE r.activity.id = :activityId")
    void deleteByActivityId(@Param("activityId") Long activityId);
}