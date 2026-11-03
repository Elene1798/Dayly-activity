package com.example.dailyactivity.repository;

import com.example.dailyactivity.model.SpyLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SpyLocationRepository extends JpaRepository<SpyLocation, Long> {
    List<SpyLocation> findByActiveTrue();
}