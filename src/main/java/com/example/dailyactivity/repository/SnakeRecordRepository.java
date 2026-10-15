package com.example.dailyactivity.repository;

import com.example.dailyactivity.model.SnakeRecord;
import com.example.dailyactivity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SnakeRecordRepository
        extends JpaRepository<SnakeRecord, Long> {

    Optional<SnakeRecord> findByUser(User user);
}
