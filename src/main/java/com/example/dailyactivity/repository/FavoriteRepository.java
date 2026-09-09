package com.example.dailyactivity.repository;

import com.example.dailyactivity.model.Activity;
import com.example.dailyactivity.model.Favorite;
import com.example.dailyactivity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserAndActivity(User user, Activity activity);

    List<Favorite> findByUser(User user);
}