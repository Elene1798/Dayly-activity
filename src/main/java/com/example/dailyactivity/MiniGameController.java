package com.example.dailyactivity.controller;

import com.example.dailyactivity.model.*;
import com.example.dailyactivity.repository.*;
import com.example.dailyactivity.service.AchievementCheckerService;
import com.example.dailyactivity.service.MiniGameService;
import com.example.dailyactivity.service.MiniGameTaskService;
import com.example.dailyactivity.service.PuzzleService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/mini-game")
public class MiniGameController {

    private final MiniGameService miniGameService;
    private final MiniGameTaskService miniGameTaskService;
    private final ActivityRepository activityRepository;
    private final ActivityCompletionRepository activityCompletionRepository;
    private final UserRepository userRepository;
    private final AchievementCheckerService achievementCheckerService;
    private final MiniGameTaskCompletionRepository miniGameTaskCompletionRepository;
    private final SnakeRecordRepository snakeRecordRepository;
    private final PuzzleService puzzleService;
    private final PuzzleCompletionRepository puzzleCompletionRepository;
    private final SudokuRecordRepository sudokuRecordRepository;

    public MiniGameController(
            MiniGameService miniGameService,
            MiniGameTaskService miniGameTaskService,
            ActivityRepository activityRepository,
            ActivityCompletionRepository activityCompletionRepository,
            UserRepository userRepository,
            AchievementCheckerService achievementCheckerService,
            MiniGameTaskCompletionRepository miniGameTaskCompletionRepository,
            SnakeRecordRepository snakeRecordRepository,
            PuzzleService puzzleService,
            PuzzleCompletionRepository puzzleCompletionRepository,
            SudokuRecordRepository sudokuRecordRepository
    ) {
        this.miniGameService = miniGameService;
        this.miniGameTaskService = miniGameTaskService;
        this.activityRepository = activityRepository;
        this.activityCompletionRepository = activityCompletionRepository;
        this.userRepository = userRepository;
        this.achievementCheckerService = achievementCheckerService;
        this.miniGameTaskCompletionRepository = miniGameTaskCompletionRepository;
        this.snakeRecordRepository = snakeRecordRepository;
        this.puzzleService = puzzleService;
        this.puzzleCompletionRepository = puzzleCompletionRepository;
        this.sudokuRecordRepository = sudokuRecordRepository;
    }

    @GetMapping("/{id}")
    public String playGame(
            @PathVariable Long id,
            @RequestParam Long activityId,
            Model model
    ) {

        MiniGame miniGame = miniGameService.findById(id);

        if (miniGame == null || !miniGame.isActive()) {
            return "redirect:/";
        }

        Activity activity = activityRepository
                .findById(activityId)
                .orElse(null);

        if (activity == null
                || activity.getMiniGame() == null
                || !activity.getMiniGame().getId().equals(miniGame.getId())) {

            return "redirect:/";
        }

        List<MiniGameTask> tasks =
                miniGameTaskService.findActiveByGame(miniGame);

        if (tasks.isEmpty()) {
            return "redirect:/";
        }

        MiniGameTask task = tasks.get(
                (int) (Math.random() * tasks.size())
        );

        model.addAttribute("miniGame", miniGame);
        model.addAttribute("task", task);
        model.addAttribute("activityId", activityId);

        return "mini-game-word";
    }



    @GetMapping("/{id}/content")
    public String gameContent(
            @PathVariable Long id,
            @RequestParam(required = false) Long activityId,
            @RequestParam(required = false) String excludeTaskIds,
            Model model,
            Authentication authentication
    ) {
        MiniGame miniGame = miniGameService.findById(id);

        if (miniGame == null || !miniGame.isActive()) {
            return "fragments/mini-game-word";
        }

        if (activityId != null) {
            Activity activity =
                    activityRepository.findById(activityId).orElse(null);

            if (activity == null
                    || activity.getMiniGame() == null
                    || !activity.getMiniGame().getId().equals(miniGame.getId())) {
                return "fragments/mini-game-word";
            }
        }

        boolean standalone = activityId == null
                && !miniGame.isAddToActivityCard();

        if ("SNAKE".equalsIgnoreCase(miniGame.getGameType())) {
            model.addAttribute("miniGame", miniGame);
            model.addAttribute("activityId", activityId);
            return "fragments/mini-game-snake";
        }

        if ("SUDOKU".equalsIgnoreCase(miniGame.getGameType())) {
            model.addAttribute("miniGame", miniGame);
            return "fragments/mini-game-sudoku";
        }

        List<MiniGameTask> tasks;

        if (standalone && "WORD".equalsIgnoreCase(miniGame.getGameType())) {
            tasks = miniGameTaskService.findActiveByGameType("WORD");
        } else {
            tasks = miniGameTaskService.findActiveByGame(miniGame);
        }

        if ("PUZZLE".equalsIgnoreCase(miniGame.getGameType())) {
            List<Puzzle> puzzles = puzzleService.getActiveByMiniGame(miniGame);

            if (puzzles.isEmpty()) {
                model.addAttribute("noTasksAvailable", true);
                return "fragments/mini-game-completed";
            }

            Puzzle puzzle = puzzles.get((int) (Math.random() * puzzles.size()));

            model.addAttribute("puzzle", puzzle);
            model.addAttribute("miniGame", miniGame);

            return "fragments/mini-game-puzzle";
        }

        if (tasks.isEmpty()) {
            model.addAttribute("noTasksAvailable", true);
            return "fragments/mini-game-completed";
        }

        model.addAttribute("miniGame", miniGame);
        model.addAttribute("activityId", activityId);

        if ("WORD".equalsIgnoreCase(miniGame.getGameType())) {

            // Фильтр для гостя: исключаем уже пройденные в этой сессии слова
            if (standalone && excludeTaskIds != null && !excludeTaskIds.isBlank()) {
                List<Long> excluded = new ArrayList<>();
                for (String part : excludeTaskIds.split(",")) {
                    try {
                        excluded.add(Long.parseLong(part.trim()));
                    } catch (NumberFormatException ignored) {
                    }
                }
                tasks = tasks.stream()
                        .filter(task -> !excluded.contains(task.getId()))
                        .toList();
            }

            // Фильтр для авторизованного: исключаем уже пройденные
            if (standalone
                    && authentication != null
                    && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())) {

                User user = userRepository
                        .findByUsername(authentication.getName())
                        .orElse(null);

                if (user != null) {
                    tasks = tasks.stream()
                            .filter(task ->
                                    !miniGameTaskCompletionRepository
                                            .existsByUserAndTask_Id(
                                                    user,
                                                    task.getId()
                                            ))
                            .toList();
                }
            }

            if (tasks.isEmpty()) {
                model.addAttribute("noTasksAvailable", true);
                return "fragments/mini-game-completed";
            }

            MiniGameTask task =
                    tasks.get((int) (Math.random() * tasks.size()));

            model.addAttribute("task", task);

            return "fragments/mini-game-word";
        }

        if ("MEMORY".equalsIgnoreCase(miniGame.getGameType())) {
            // Собираем задачи из ВСЕХ игр MEMORY
            List<MiniGame> allMemoryGames = miniGameService.findActiveGamesByType("MEMORY");

            List<MiniGameTask> allTasks = new ArrayList<>();
            for (MiniGame g : allMemoryGames) {
                allTasks.addAll(miniGameTaskService.findActiveByGame(g));
            }

            model.addAttribute("tasks", allTasks);
            return "fragments/mini-game-memory";
        }

        if ("CROCODILE".equalsIgnoreCase(miniGame.getGameType())) {
            model.addAttribute("tasks", tasks);
            return "fragments/mini-game-crocodile";
        }

        if ("SNAKE".equalsIgnoreCase(miniGame.getGameType())) {
            model.addAttribute("tasks", tasks);
            return "fragments/mini-game-snake";
        }

        return "fragments/mini-game-word";
    }


    @GetMapping("/type/{gameType}/next")
    public String nextGameByType(
            @PathVariable String gameType,
            @RequestParam(required = false) Long currentGameId,
            Model model
    ) {
        if (!"MEMORY".equalsIgnoreCase(gameType)) {
            return "fragments/mini-game-completed";
        }

        List<MiniGame> games = miniGameService.findActiveGamesByType("MEMORY");

        games = games.stream()
                .filter(game -> !game.isAddToActivityCard())
                .filter(game -> !miniGameTaskService.findActiveByGame(game).isEmpty())
                .sorted(java.util.Comparator.comparing(MiniGame::getId))
                .toList();

        if (games.isEmpty()) {
            model.addAttribute("noTasksAvailable", true);
            return "fragments/mini-game-completed";
        }

        int nextIndex = 0;

        if (currentGameId != null) {
            for (int i = 0; i < games.size(); i++) {
                if (games.get(i).getId().equals(currentGameId)) {
                    nextIndex = (i + 1) % games.size();
                    break;
                }
            }
        }

        MiniGame nextGame = games.get(nextIndex);
        List<MiniGameTask> tasks = miniGameTaskService.findActiveByGame(nextGame);

        model.addAttribute("miniGame", nextGame);
        model.addAttribute("activityId", null);
        model.addAttribute("tasks", tasks);

        return "fragments/mini-game-memory";
    }

    @GetMapping("/{id}/crocodile-tasks")
    @ResponseBody
    public List<Map<String, Object>> crocodileTasks(
            @PathVariable Long id,
            @RequestParam(required = false) Long activityId,
            @RequestParam String difficulty,
            @RequestParam String roundType
    ) {
        MiniGame miniGame = miniGameService.findById(id);

        if (miniGame == null
                || !miniGame.isActive()
                || !"CROCODILE".equalsIgnoreCase(miniGame.getGameType())) {
            return List.of();
        }

        // Если игра запущена из карточки активности,
        // проверяем, что она действительно связана с этой активностью.
        if (activityId != null) {
            Activity activity =
                    activityRepository.findById(activityId).orElse(null);

            if (activity == null
                    || activity.getMiniGame() == null
                    || !activity.getMiniGame().getId().equals(id)) {
                return List.of();
            }
        }

        List<MiniGameTask> tasks =
                miniGameTaskService.findActiveByGame(miniGame);

        List<Map<String, Object>> result = new ArrayList<>();

        for (MiniGameTask task : tasks) {
            boolean difficultyMatches =
                    task.getDifficulty() == null
                            || task.getDifficulty().isBlank()
                            || task.getDifficulty().equalsIgnoreCase(difficulty);

            if (!difficultyMatches) {
                continue;
            }

            boolean typeMatches;

            if ("SITUATION".equalsIgnoreCase(roundType)) {
                typeMatches =
                        "SITUATION".equalsIgnoreCase(task.getTaskType());
            } else {
                typeMatches =
                        !"SITUATION".equalsIgnoreCase(task.getTaskType());
            }

            if (!typeMatches) {
                continue;
            }

            Map<String, Object> item = new HashMap<>();
            item.put("id", task.getId());
            item.put("content", task.getContent());

            result.add(item);
        }

        return result;
    }

    @PostMapping("/complete")
    public String completeGame(
            @RequestParam Long activityId,
            @RequestParam Long taskId,
            @RequestParam String answer,
            Authentication authentication
    ) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {

            return "redirect:/login";
        }

        User user = userRepository
                .findByUsername(authentication.getName())
                .orElse(null);

        if (user == null) {
            return "redirect:/login";
        }

        Activity activity = activityRepository
                .findById(activityId)
                .orElse(null);

        if (activity == null) {
            return "redirect:/";
        }

        MiniGameTask task = miniGameTaskService
                .findById(taskId);

        if (task == null) {
            return "redirect:/activity?activityId=" + activityId;
        }

        /*
         * Проверяем, что занятие действительно связано
         * с мини-игрой.
         */
        if (activity.getMiniGame() == null) {
            return "redirect:/activity?activityId=" + activityId;
        }

        /*
         * Проверяем, что задание действительно принадлежит
         * мини-игре этого занятия.
         */
        if (task.getMiniGame() == null
                || !activity.getMiniGame()
                .getId()
                .equals(task.getMiniGame().getId())) {

            return "redirect:/activity?activityId=" + activityId;
        }

        /*
         * Проверяем правильность ответа.
         */
        if (!task.getAnswer()
                .trim()
                .equalsIgnoreCase(answer.trim())) {

            return "redirect:/mini-game/"
                    + activity.getMiniGame().getId()
                    + "?activityId="
                    + activityId;
        }

        /*
         * Проверяем, не было ли уже выполнено
         * это занятие пользователем.
         */
        boolean alreadyCompleted =
                activityCompletionRepository
                        .findFirstByUserAndActivityIdAndSourceOrderByCompletedAtDesc(
                                user,
                                activityId,
                                "CARD"
                        )
                        .isPresent();

        if (!alreadyCompleted) {

            ActivityCompletion completion =
                    new ActivityCompletion(
                            user,
                            activity,
                            LocalDateTime.now(),
                            "CARD",
                            null
                    );

            try {

                activityCompletionRepository.save(completion);

                achievementCheckerService
                        .checkCompletionAchievements(user);

            } catch (DataIntegrityViolationException e) {

                System.out.println(
                        "Повторное выполнение мини-игры "
                                + "не сохранено: user="
                                + user.getId()
                                + ", activity="
                                + activityId
                );
            }
        }

        return "redirect:/activity?activityId=" + activityId;
    }

    @PostMapping("/complete-inline")
    @ResponseBody
    public String completeGameInline(
            @RequestParam Long activityId,
            @RequestParam Long taskId,
            @RequestParam String answer,
            Authentication authentication
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return "AUTH_REQUIRED";
        }

        User user = userRepository
                .findByUsername(authentication.getName())
                .orElse(null);

        if (user == null) {
            return "AUTH_REQUIRED";
        }

        Activity activity = activityRepository
                .findById(activityId)
                .orElse(null);

        if (activity == null || activity.getMiniGame() == null) {
            return "ERROR";
        }

        MiniGameTask task = miniGameTaskService.findById(taskId);

        if (task == null
                || task.getMiniGame() == null
                || !task.getMiniGame().getId().equals(activity.getMiniGame().getId())) {
            return "ERROR";
        }

        if (!task.getAnswer().trim().equalsIgnoreCase(answer.trim())) {
            return "WRONG";
        }

        boolean alreadyCompleted =
                activityCompletionRepository
                        .findFirstByUserAndActivityIdAndSourceOrderByCompletedAtDesc(
                                user,
                                activityId,
                                "CARD"
                        )
                        .isPresent();

        if (!alreadyCompleted) {
            ActivityCompletion completion =
                    new ActivityCompletion(
                            user,
                            activity,
                            LocalDateTime.now(),
                            "CARD",
                            null
                    );

            try {
                activityCompletionRepository.save(completion);
                achievementCheckerService.checkCompletionAchievements(user);
            } catch (DataIntegrityViolationException ignored) {
                // Уже завершено другим запросом
            }
        }

        return "SUCCESS";
    }


    @PostMapping("/complete-word-standalone")
    @ResponseBody
    public String completeWordStandalone(
            @RequestParam Long taskId,
            @RequestParam String answer,
            Authentication authentication
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return "AUTH_REQUIRED";
        }

        User user = userRepository
                .findByUsername(authentication.getName())
                .orElse(null);

        if (user == null) {
            return "AUTH_REQUIRED";
        }

        MiniGameTask task = miniGameTaskService.findById(taskId);

        if (task == null
                || !task.isActive()
                || task.getMiniGame() == null
                || !task.getMiniGame().isActive()
                || !"WORD".equalsIgnoreCase(task.getMiniGame().getGameType())
                || task.getMiniGame().isAddToActivityCard()) {
            return "ERROR";
        }

        if (task.getAnswer() == null
                || !task.getAnswer().trim().equalsIgnoreCase(answer.trim())) {
            return "WRONG";
        }

        if (miniGameTaskCompletionRepository
                .existsByUserAndTask_Id(user, taskId)) {
            return "ALREADY_COMPLETED";
        }

        MiniGameTaskCompletion completion =
                new MiniGameTaskCompletion(user, task, LocalDateTime.now());

        try {
            miniGameTaskCompletionRepository.save(completion);
        } catch (DataIntegrityViolationException ignored) {
            return "ALREADY_COMPLETED";
        }

        return "SUCCESS";
    }

    @PostMapping("/complete-crocodile-inline")
    @ResponseBody
    public String completeCrocodileInline(
            @RequestParam Long activityId,
            Authentication authentication
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return "AUTH_REQUIRED";
        }

        User user =
                userRepository.findByUsername(authentication.getName())
                        .orElse(null);

        if (user == null) {
            return "AUTH_REQUIRED";
        }

        Activity activity =
                activityRepository.findById(activityId).orElse(null);

        if (activity == null
                || activity.getMiniGame() == null
                || !"CROCODILE".equalsIgnoreCase(
                activity.getMiniGame().getGameType()
        )) {
            return "ERROR";
        }

        boolean alreadyCompleted =
                activityCompletionRepository
                        .findFirstByUserAndActivityIdAndSourceOrderByCompletedAtDesc(
                                user,
                                activityId,
                                "CARD"
                        )
                        .isPresent();

        if (!alreadyCompleted) {

            ActivityCompletion completion =
                    new ActivityCompletion(
                            user,
                            activity,
                            LocalDateTime.now(),
                            "CARD",
                            null
                    );

            try {
                activityCompletionRepository.save(completion);

                achievementCheckerService
                        .checkCompletionAchievements(user);

            } catch (DataIntegrityViolationException ignored) {
                // Защита от повторного сохранения
            }
        }

        return "SUCCESS";
    }

    @PostMapping("/complete-memory-inline")
    @ResponseBody
    public String completeMemoryGameInline(
            @RequestParam Long activityId,
            Authentication authentication
    ) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {

            return "AUTH_REQUIRED";
        }

        User user = userRepository
                .findByUsername(authentication.getName())
                .orElse(null);

        if (user == null) {
            return "AUTH_REQUIRED";
        }

        Activity activity = activityRepository
                .findById(activityId)
                .orElse(null);

        if (activity == null
                || activity.getMiniGame() == null) {

            return "ERROR";
        }

        MiniGame miniGame = activity.getMiniGame();

        if (!"MEMORY".equalsIgnoreCase(miniGame.getGameType())) {
            return "ERROR";
        }

        boolean alreadyCompleted =
                activityCompletionRepository
                        .findFirstByUserAndActivityIdAndSourceOrderByCompletedAtDesc(
                                user,
                                activityId,
                                "CARD"
                        )
                        .isPresent();

        if (!alreadyCompleted) {

            ActivityCompletion completion =
                    new ActivityCompletion(
                            user,
                            activity,
                            LocalDateTime.now(),
                            "CARD",
                            null
                    );

            try {

                activityCompletionRepository.save(completion);

                achievementCheckerService
                        .checkCompletionAchievements(user);

            } catch (DataIntegrityViolationException ignored) {
                // Игра уже была сохранена другим запросом
            }
        }

        return "SUCCESS";
    }

    @PostMapping("/snake/save-record")
    @ResponseBody
    public String saveSnakeRecord(
            @RequestParam int score,
            Authentication authentication
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return "AUTH_REQUIRED";
        }

        User user = userRepository
                .findByUsername(authentication.getName())
                .orElse(null);

        if (user == null) {
            return "AUTH_REQUIRED";
        }

        SnakeRecord record = snakeRecordRepository
                .findByUser(user)
                .orElse(null);

        if (record == null) {
            record = new SnakeRecord(user, score, LocalDateTime.now());
        } else if (score > record.getBestScore()) {
            record.setBestScore(score);
            record.setAchievedAt(LocalDateTime.now());
        } else {
            return "OK";
        }

        snakeRecordRepository.save(record);

        return "SUCCESS";
    }

    @GetMapping("/snake/record")
    @ResponseBody
    public String getSnakeRecord(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return "0";
        }

        User user = userRepository
                .findByUsername(authentication.getName())
                .orElse(null);

        if (user == null) {
            return "0";
        }

        return snakeRecordRepository
                .findByUser(user)
                .map(r -> String.valueOf(r.getBestScore()))
                .orElse("0");
    }

    @PostMapping("/puzzle/{id}/complete")
    @ResponseBody
    public String completePuzzle(
            @PathVariable Long id,
            @RequestParam int size,
            @RequestParam int moves,
            @RequestParam int time,
            Authentication auth
    ) {
        if (auth == null
                || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            return "GUEST";
        }

        User user = userRepository
                .findByUsername(auth.getName())
                .orElse(null);

        if (user == null) return "GUEST";

        Puzzle puzzle = puzzleService.findById(id);
        if (puzzle == null) return "ERROR";

        // Проверка: собран ли уже
        boolean exists = puzzleCompletionRepository
                .findByUserAndPuzzleAndGridSize(user, puzzle, size)
                .isPresent();

        if (exists) {
            return "ALREADY_COMPLETED";
        }

        PuzzleCompletion completion = new PuzzleCompletion(
                user, puzzle, size, moves, time
        );

        try {
            puzzleCompletionRepository.save(completion);
        } catch (DataIntegrityViolationException ignored) {
            return "ALREADY_COMPLETED";
        }

        // Проверяем достижения
        achievementCheckerService.checkPuzzleAchievements(user);

        return "SUCCESS";
    }

    @GetMapping("/{id}/puzzle/next")
    public String nextPuzzle(
            @PathVariable Long id,
            @RequestParam(required = false) Long currentPuzzleId,
            @RequestParam(defaultValue = "3") int size,
            Model model
    ) {
        MiniGame miniGame = miniGameService.findById(id);
        if (miniGame == null
                || !miniGame.isActive()
                || !"PUZZLE".equalsIgnoreCase(miniGame.getGameType())) {
            return "fragments/mini-game-completed";
        }

        List<Puzzle> puzzles = puzzleService.getActiveByMiniGame(miniGame);
        if (puzzles.isEmpty()) {
            return "fragments/mini-game-completed";
        }

        // Исключаем текущий
        List<Puzzle> candidates = puzzles;
        if (currentPuzzleId != null && puzzles.size() > 1) {
            candidates = puzzles.stream()
                    .filter(p -> !p.getId().equals(currentPuzzleId))
                    .toList();
        }

        // Если кандидатов не осталось (был один пазл) — возвращаем тот же
        if (candidates.isEmpty()) {
            candidates = puzzles;
        }

        Puzzle next = candidates.get(
                (int) (Math.random() * candidates.size())
        );

        model.addAttribute("puzzle", next);
        model.addAttribute("miniGame", miniGame);
        model.addAttribute("size", size);

        return "fragments/mini-game-puzzle";
    }

    @PostMapping("/sudoku/save-record")
    @ResponseBody
    public String saveSudokuRecord(
            @RequestParam int time,
            @RequestParam String difficulty,
            Authentication auth
    ) {
        if (auth == null
                || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            return "AUTH_REQUIRED";
        }

        User user = userRepository
                .findByUsername(auth.getName())
                .orElse(null);

        if (user == null) {
            return "AUTH_REQUIRED";
        }

        SudokuRecord record = sudokuRecordRepository
                .findByUser(user)
                .orElse(null);

        if (record == null) {
            record = new SudokuRecord();
            record.setUser(user);
            record.setBestTime(time);
            record.setDifficulty(difficulty);
            record.setAchievedAt(LocalDateTime.now());
        } else if (time < record.getBestTime()) {
            // Меньше время — лучше рекорд
            record.setBestTime(time);
            record.setDifficulty(difficulty);
            record.setAchievedAt(LocalDateTime.now());
        } else {
            return "OK";
        }

        sudokuRecordRepository.save(record);

        return "SUCCESS";
    }

    @GetMapping("/sudoku/record")
    @ResponseBody
    public String getSudokuRecord(Authentication auth) {
        if (auth == null
                || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            return "0";
        }

        User user = userRepository
                .findByUsername(auth.getName())
                .orElse(null);

        if (user == null) {
            return "0";
        }

        return sudokuRecordRepository
                .findByUser(user)
                .map(r -> String.valueOf(r.getBestTime()))
                .orElse("0");
    }

}