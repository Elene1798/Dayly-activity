
package com.example.dailyactivity.repository;

import com.example.dailyactivity.model.MiniGameTaskCompletion;
import com.example.dailyactivity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MiniGameTaskCompletionRepository
        extends JpaRepository<MiniGameTaskCompletion, Long> {

    boolean existsByUserAndTaskId(User user, Long taskId);
    boolean existsByUserAndTask_Id(User user, Long taskId);

    Optional<MiniGameTaskCompletion> findByUserAndTaskId(
            User user,
            Long taskId
    );

    List<MiniGameTaskCompletion> findByUserAndTask_MiniGame_Id(
            User user,
            Long miniGameId
    );
}