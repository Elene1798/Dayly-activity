package com.example.dailyactivity.repository;

import com.example.dailyactivity.model.Activity;
import com.example.dailyactivity.model.Like;
import com.example.dailyactivity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByUserAndActivity(User user, Activity activity);
}