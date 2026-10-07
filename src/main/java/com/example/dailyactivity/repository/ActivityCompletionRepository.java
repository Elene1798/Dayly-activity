package com.example.dailyactivity.repository;

import com.example.dailyactivity.model.ActivityCompletion;
import com.example.dailyactivity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    long countByUser(User user);

    boolean existsByDailyChallengeId(Long dailyChallengeId);

    long countByUserAndSource(
            User user,
            String source
    );

    @Query("""
    SELECT COUNT(ac)
    FROM ActivityCompletion ac
    WHERE ac.user = :user
      AND (
            EXTRACT(HOUR FROM ac.completedAt) >= :fromHour
            OR EXTRACT(HOUR FROM ac.completedAt) < :toHour
      )
    """)
    long countNightCompletions(
            @Param("user") User user,
            @Param("fromHour") int fromHour,
            @Param("toHour") int toHour
    );

    @Query("""
    SELECT COUNT(ac)
    FROM ActivityCompletion ac
    WHERE ac.user = :user
      AND EXTRACT(HOUR FROM ac.completedAt) < :beforeHour
    """)
    long countEarlyCompletions(
            @Param("user") User user,
            @Param("beforeHour") int beforeHour
    );

    @Query("""
    SELECT COUNT(DISTINCT ac.snapshotLocation)
    FROM ActivityCompletion ac
    WHERE ac.user = :user
      AND ac.snapshotLocation IS NOT NULL
      AND ac.snapshotLocation <> ''
      AND ac.snapshotLocation <> 'anywhere'
    """)
    long countDistinctCompletedLocations(User user);

    @Query("""
    SELECT COUNT(DISTINCT a.location)
    FROM Activity a
    WHERE a.location IS NOT NULL
      AND a.location <> ''
      AND a.location <> 'anywhere'
    """)
    long countAvailableLocations();

    @Query("""
    SELECT COUNT(ac) 
    FROM ActivityCompletion ac
    WHERE ac.user = :user
      AND ac.snapshotLocation = 'outside'
""")
    long countOutsideActivities(User user);

    @Query("""
    SELECT COUNT(ac) 
    FROM ActivityCompletion ac
    WHERE ac.user = :user
      AND ac.snapshotCategory = 'creative'
""")
    long countCreativeActivity(User user);

    @Query("""
    SELECT COUNT(ac) 
    FROM ActivityCompletion ac
    WHERE ac.user = :user
      AND ac.snapshotCategory = 'learning'
""")
    long countLearningActivity(User user);

    @Query("""
    SELECT COUNT(ac)
    FROM ActivityCompletion ac
    WHERE ac.user = :user
      AND ac.snapshotCategory = 'active'
""")
    long countActive(User user);

    @Query("""
    SELECT COALESCE(SUM(ac.snapshotDuration), 0)
    FROM ActivityCompletion ac
    WHERE ac.user = :user
""")
    long countTotalCompletedMinutes(User user);




}