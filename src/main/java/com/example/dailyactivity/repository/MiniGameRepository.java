package com.example.dailyactivity.repository;

import com.example.dailyactivity.model.MiniGame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MiniGameRepository extends JpaRepository<MiniGame, Long> {

    List<MiniGame> findByActiveTrue();

    List<MiniGame> findByCategoryAndActiveTrue(String category);
}