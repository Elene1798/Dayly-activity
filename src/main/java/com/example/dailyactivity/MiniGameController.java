package com.example.dailyactivity.controller;

import com.example.dailyactivity.model.*;
import com.example.dailyactivity.repository.ActivityCompletionRepository;
import com.example.dailyactivity.repository.ActivityRepository;
import com.example.dailyactivity.repository.UserRepository;
import com.example.dailyactivity.service.AchievementCheckerService;
import com.example.dailyactivity.service.MiniGameService;
import com.example.dailyactivity.service.MiniGameTaskService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/mini-game")
public class MiniGameController {

    private final MiniGameService miniGameService;
    private final MiniGameTaskService miniGameTaskService;
    private final ActivityRepository activityRepository;
    private final ActivityCompletionRepository activityCompletionRepository;
    private final UserRepository userRepository;
    private final AchievementCheckerService achievementCheckerService;

    public MiniGameController(
            MiniGameService miniGameService,
            MiniGameTaskService miniGameTaskService,
            ActivityRepository activityRepository,
            ActivityCompletionRepository activityCompletionRepository,
            UserRepository userRepository,
            AchievementCheckerService achievementCheckerService
    ) {
        this.miniGameService = miniGameService;
        this.miniGameTaskService = miniGameTaskService;
        this.activityRepository = activityRepository;
        this.activityCompletionRepository = activityCompletionRepository;
        this.userRepository = userRepository;
        this.achievementCheckerService = achievementCheckerService;
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
            @RequestParam Long activityId,
            Model model
    ) {

        MiniGame miniGame = miniGameService.findById(id);

        if (miniGame == null || !miniGame.isActive()) {
            return "fragments/mini-game-empty";
        }

        Activity activity = activityRepository
                .findById(activityId)
                .orElse(null);

        if (activity == null
                || activity.getMiniGame() == null
                || !activity.getMiniGame().getId().equals(miniGame.getId())) {

            return "fragments/mini-game-empty";
        }

        List<MiniGameTask> tasks =
                miniGameTaskService.findActiveByGame(miniGame);

        if (tasks.isEmpty()) {
            return "fragments/mini-game-empty";
        }

        MiniGameTask task = tasks.get(
                (int) (Math.random() * tasks.size())
        );

        model.addAttribute("miniGame", miniGame);
        model.addAttribute("task", task);
        model.addAttribute("activityId", activityId);

        return "fragments/mini-game-word";
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
}