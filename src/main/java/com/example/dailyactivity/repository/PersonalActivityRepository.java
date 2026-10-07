package com.example.dailyactivity.repository;

import com.example.dailyactivity.model.PersonalActivity;
import com.example.dailyactivity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PersonalActivityRepository
        extends JpaRepository<PersonalActivity, Long> {

    List<PersonalActivity> findByUserAndActivityDateOrderByCreatedAtAsc(
            User user,
            LocalDate activityDate
    );

    List<PersonalActivity> findByUserAndActivityDateBetweenOrderByActivityDateAscCreatedAtAsc(
            User user,
            LocalDate startDate,
            LocalDate endDate
    );
}
