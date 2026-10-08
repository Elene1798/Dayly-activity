package com.example.dailyactivity.service;

import com.example.dailyactivity.model.Activity;
import com.example.dailyactivity.model.MiniGame;
import com.example.dailyactivity.repository.ActivityRepository;
import com.example.dailyactivity.repository.MiniGameRepository;
import com.example.dailyactivity.repository.MiniGameTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MiniGameService {

    private final MiniGameRepository miniGameRepository;
    private final MiniGameTaskRepository miniGameTaskRepository;
    private final ActivityRepository activityRepository;

    public MiniGameService(
            MiniGameRepository miniGameRepository,
            MiniGameTaskRepository miniGameTaskRepository,
            ActivityRepository activityRepository
    ) {
        this.miniGameRepository = miniGameRepository;
        this.miniGameTaskRepository = miniGameTaskRepository;
        this.activityRepository = activityRepository;
    }

    public List<MiniGame> findAll() {
        return miniGameRepository.findAll();
    }

    public List<MiniGame> findActive() {
        return miniGameRepository.findByActiveTrue();
    }

    public MiniGame findById(Long id) {
        return miniGameRepository.findById(id).orElse(null);
    }

    @Transactional
    public MiniGame save(MiniGame miniGame) {

        boolean newGame = miniGame.getId() == null;

        MiniGame savedGame = miniGameRepository.save(miniGame);

        if (newGame) {

            String location = "anywhere";

            if ("CROCODILE".equalsIgnoreCase(savedGame.getGameType())) {
                location = "friends";
            }

            Activity activity = new Activity(
                    savedGame.getTitle(),
                    savedGame.getDescription(),
                    savedGame.getCategory(),
                    savedGame.getDuration(),
                    location,
                    savedGame.getInstructions(),
                    null,
                    null,
                    null,
                    null,
                    null
            );

            activity.setMiniGame(savedGame);

            activityRepository.save(activity);
        }

        return savedGame;
    }

    @Transactional
    public void delete(Long id) {

        MiniGame miniGame = miniGameRepository.findById(id).orElse(null);

        if (miniGame == null) {
            return;
        }

        // 1. Удаляем карточку занятия,
        // которая связана с этой мини-игрой
        Activity activity = activityRepository
                .findAll()
                .stream()
                .filter(a -> a.getMiniGame() != null)
                .filter(a -> a.getMiniGame().getId().equals(id))
                .findFirst()
                .orElse(null);

        if (activity != null) {
            activityRepository.delete(activity);
        }

        // 2. Удаляем задания мини-игры
        miniGameTaskRepository.deleteAll(
                miniGameTaskRepository.findByMiniGameOrderByIdAsc(miniGame)
        );

        // 3. Теперь можно удалить саму мини-игру
        miniGameRepository.delete(miniGame);
    }
}