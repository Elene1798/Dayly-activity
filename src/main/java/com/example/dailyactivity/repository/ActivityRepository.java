package com.example.dailyactivity.repository;

import com.example.dailyactivity.model.Activity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    Optional<Activity> findByTitle(String title);

    List<Activity> findByCategory(String category);

    List<Activity> findByCategoryAndDurationLessThanEqual(
            String category,
            int duration
    );

    List<Activity> findByCategoryAndLocationAndDurationLessThanEqual(
            String category,
            String location,
            int duration
    );

    List<Activity> findByCategoryAndLocation(
            String category,
            String location
    );


}
