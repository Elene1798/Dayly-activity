package com.example.dailyactivity.repository;

import com.example.dailyactivity.model.User;
import com.example.dailyactivity.model.UserVisit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface UserVisitRepository
        extends JpaRepository<UserVisit, Long> {

    boolean existsByUserAndVisitDate(
            User user,
            LocalDate visitDate
    );

    List<UserVisit> findByUserOrderByVisitDateAsc(
            User user
    );
}