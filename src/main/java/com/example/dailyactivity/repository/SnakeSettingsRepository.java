package com.example.dailyactivity.repository;

import com.example.dailyactivity.model.SnakeSettings;
import com.example.dailyactivity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SnakeSettingsRepository extends JpaRepository<SnakeSettings, Long> {
    Optional<SnakeSettings> findByUser(User user);
}