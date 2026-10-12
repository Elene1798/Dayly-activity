package com.example.dailyactivity.service;

import com.example.dailyactivity.model.Activity;
import com.example.dailyactivity.model.MiniGame;
import com.example.dailyactivity.model.MiniGameTask;
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

    /**
     * Игры для отдельной страницы «Мини-игры».
     */
    public List<MiniGame> findActiveStandaloneGames() {
        List<MiniGame> games = miniGameRepository.findByActiveTrue()
                .stream()
                .filter(game -> !game.isAddToActivityCard())
                .toList();

        List<MiniGame> result = new java.util.ArrayList<>();
        java.util.Set<String> addedTypes = new java.util.HashSet<>();

        for (MiniGame game : games) {
            String type = game.getGameType();

            if (type != null
                    && ("WORD".equalsIgnoreCase(type)
                    || "MEMORY".equalsIgnoreCase(type))) {

                if (addedTypes.add(type.toUpperCase(java.util.Locale.ROOT))) {
                    result.add(game);
                }
            } else {
                result.add(game);
            }
        }

        return result;
    }

    public List<MiniGame> findActiveGamesByType(String gameType) {
        return miniGameRepository
                .findByGameTypeAndActiveTrueOrderByIdAsc(gameType)
                .stream()
                .filter(game -> !game.isAddToActivityCard())
                .toList();
    }

    public MiniGame findById(Long id) {
        return miniGameRepository.findById(id).orElse(null);
    }

    @Transactional
    public MiniGame save(MiniGame miniGame) {

        boolean newGame = miniGame.getId() == null;

        // Значения по умолчанию для необязательных полей.
        if (miniGame.getCategory() == null
                || miniGame.getCategory().isBlank()) {
            miniGame.setCategory("entertainment");
        }

        if (miniGame.getDuration() <= 0) {
            miniGame.setDuration(5);
        }

        MiniGame savedGame = miniGameRepository.save(miniGame);

        /*
         * Создаём карточку активности только для новой игры,
         * если пользователь включил соответствующий чекбокс.
         *
         * При редактировании игры новые карточки не создаются.
         */
        if (newGame && savedGame.isAddToActivityCard()) {

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

        /*
         * Не удаляем связанные Activity.
         * Отсоединяем игру, чтобы сохранить карточку и её историю,
         * а также не нарушить внешний ключ в базе данных.
         */
        List<Activity> linkedActivities = activityRepository.findAll()
                .stream()
                .filter(activity -> activity.getMiniGame() != null)
                .filter(activity ->
                        activity.getMiniGame().getId().equals(id))
                .toList();

        for (Activity activity : linkedActivities) {
            activity.setMiniGame(null);
            activityRepository.save(activity);
        }

        // Удаляем задания игры.
        List<MiniGameTask> tasks =
                miniGameTaskRepository.findByMiniGameOrderByIdAsc(miniGame);

        miniGameTaskRepository.deleteAll(tasks);

        // Удаляем саму игру.
        miniGameRepository.delete(miniGame);
    }
}

