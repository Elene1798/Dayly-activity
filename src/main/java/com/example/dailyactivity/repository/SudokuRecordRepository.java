package com.example.dailyactivity.repository;

import com.example.dailyactivity.model.SudokuRecord;
import com.example.dailyactivity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SudokuRecordRepository
        extends JpaRepository<SudokuRecord, Long> {

    Optional<SudokuRecord> findByUser(User user);
}