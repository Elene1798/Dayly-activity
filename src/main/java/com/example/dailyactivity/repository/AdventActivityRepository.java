package com.example.dailyactivity.repository;

import com.example.dailyactivity.model.AdventActivity;
import com.example.dailyactivity.model.AdventSeason;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdventActivityRepository
        extends JpaRepository<AdventActivity, Long> {

    /** Все задания сезона. */
    List<AdventActivity> findBySeason(AdventSeason season);

    /** Все обычные задания сезона (без особых). */
    List<AdventActivity> findBySeasonAndSpecialFalse(AdventSeason season);

    /** Все особые задания сезона. */
    List<AdventActivity> findBySeasonAndSpecialTrue(AdventSeason season);

    /** Количество заданий в сезоне. */
    long countBySeason(AdventSeason season);
}