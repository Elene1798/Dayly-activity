package com.example.dailyactivity.repository;

import com.example.dailyactivity.model.AdventDay;
import com.example.dailyactivity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AdventDayRepository
        extends JpaRepository<AdventDay, Long> {

    /** Все ячейки пользователя за конкретный месяц. */
    List<AdventDay> findByUserAndAdventDateBetween(
            User user,
            LocalDate start,
            LocalDate end
    );

    /** Конкретная ячейка пользователя на дату. */
    Optional<AdventDay> findByUserAndAdventDate(
            User user,
            LocalDate adventDate
    );

    /** Проверка, что у пользователя уже есть ячейки за этот период. */
    boolean existsByUserAndAdventDateBetween(
            User user,
            LocalDate start,
            LocalDate end
    );

    /** Сколько ячеек пользователь открыл (для прогресса). */
    long countByUserAndAdventDateBetweenAndOpenedTrue(
            User user,
            LocalDate start,
            LocalDate end
    );
}