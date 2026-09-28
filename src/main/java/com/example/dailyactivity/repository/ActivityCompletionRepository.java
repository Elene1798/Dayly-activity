package com.example.dailyactivity.repository;

import com.example.dailyactivity.model.ActivityCompletion;
import com.example.dailyactivity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ActivityCompletionRepository
        extends JpaRepository<ActivityCompletion, Long> {

    List<ActivityCompletion> findByUserAndCompletedAtGreaterThanEqualAndCompletedAtLessThanOrderByCompletedAtAsc(
            User user,
            LocalDateTime start,
            LocalDateTime end
    );

    Optional<ActivityCompletion>
    findFirstByUserAndActivityIdAndSourceOrderByCompletedAtDesc(
            User user,
            Long activityId,
            String source
    );

    boolean existsByUserAndActivityId(User user, Long activityId);

    boolean existsByDailyChallengeId(Long dailyChallengeId);
}