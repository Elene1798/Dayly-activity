package com.example.dailyactivity;

import com.example.dailyactivity.model.Activity;
import com.example.dailyactivity.model.MiniGame;
import com.example.dailyactivity.model.MiniGameTask;
import com.example.dailyactivity.repository.ActivityRepository;
import com.example.dailyactivity.service.MiniGameService;
import com.example.dailyactivity.service.MiniGameTaskService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final ActivityRepository activityRepository;
    private final MiniGameService miniGameService;
    private final MiniGameTaskService miniGameTaskService;

    public AdminController(ActivityRepository activityRepository,
                           MiniGameService miniGameService,
                           MiniGameTaskService miniGameTaskService)
    {
        this.activityRepository = activityRepository;
        this.miniGameService = miniGameService;
        this.miniGameTaskService = miniGameTaskService;
    }

    @GetMapping
    public String adminPage(Model model) {
        model.addAttribute("activities", activityRepository.findAll());
        model.addAttribute("miniGames", miniGameService.findAll());
        return "admin";
    }

    @PostMapping("/add")
    public String addActivity(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String category,
            @RequestParam int duration,
            @RequestParam String location,
            @RequestParam(required = false) String instructions,
            @RequestParam(required = false) String benefit,
            @RequestParam(required = false) String interestingFact,
            @RequestParam(required = false) String imageUrl,
            @RequestParam(required = false) String linkUrl,
            @RequestParam(required = false) String videoUrl) {

        Activity activity = new Activity(title, description, category, duration, location,
                instructions, benefit, interestingFact, imageUrl, linkUrl, videoUrl);
        activityRepository.save(activity);
        return "redirect:/admin";
    }

    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable Long id, Model model) {
        Activity activity = activityRepository.findById(id).orElse(null);
        if (activity == null) return "redirect:/admin";
        model.addAttribute("activity", activity);
        model.addAttribute("miniGames", miniGameService.findActive());
        return "admin-edit";
    }

    @PostMapping("/edit/{id}")
    public String editActivity(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String category,
            @RequestParam int duration,
            @RequestParam String location,
            @RequestParam(required = false) String instructions,
            @RequestParam(required = false) String benefit,
            @RequestParam(required = false) String interestingFact,
            @RequestParam(required = false) String imageUrl,
            @RequestParam(required = false) String linkUrl,
            @RequestParam(required = false) String videoUrl,
            @RequestParam(required = false) Long miniGameId) {

        Activity activity = activityRepository.findById(id).orElse(null);
        if (activity == null) return "redirect:/admin";

        activity.setTitle(title);
        activity.setDescription(description);
        activity.setCategory(category);
        activity.setDuration(duration);
        activity.setLocation(location);
        activity.setInstructions(instructions);
        activity.setBenefit(benefit);
        activity.setInterestingFact(interestingFact);
        activity.setImageUrl(imageUrl);
        activity.setLinkUrl(linkUrl);
        activity.setVideoUrl(videoUrl);
        if (miniGameId != null) {
            MiniGame miniGame = miniGameService.findById(miniGameId);
            activity.setMiniGame(miniGame);
        } else {
            activity.setMiniGame(null);
        }

        activityRepository.save(activity);
        return "redirect:/admin";
    }

    @PostMapping("/delete/{id}")
    public String deleteActivity(@PathVariable Long id) {
        activityRepository.deleteById(id);
        return "redirect:/admin";
    }

    @PostMapping("/mini-game/add")
    public String addMiniGame(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam String gameType,
            @RequestParam String category,
            @RequestParam int duration,
            @RequestParam(required = false) String instructions
    ) {
        MiniGame miniGame = new MiniGame(
                title,
                description,
                gameType,
                category,
                duration,
                instructions
        );

        miniGameService.save(miniGame);

        return "redirect:/admin";
    }

    @PostMapping("/mini-game/delete/{id}")
    public String deleteMiniGame(@PathVariable Long id) {
        miniGameService.delete(id);

        return "redirect:/admin";
    }

    @PostMapping("/mini-game/edit/{id}")
    public String editMiniGame(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam String gameType,
            @RequestParam String category,
            @RequestParam int duration,
            @RequestParam(required = false) String instructions,
            @RequestParam(defaultValue = "false") boolean active
    ) {
        MiniGame miniGame = miniGameService.findById(id);

        if (miniGame != null) {
            miniGame.setTitle(title);
            miniGame.setDescription(description);
            miniGame.setGameType(gameType);
            miniGame.setCategory(category);
            miniGame.setDuration(duration);
            miniGame.setInstructions(instructions);
            miniGame.setActive(active);

            miniGameService.save(miniGame);
        }

        return "redirect:/admin";
    }

    @GetMapping("/mini-game/{id}/tasks")
    public String miniGameTasks(
            @PathVariable Long id,
            Model model
    ) {
        MiniGame miniGame = miniGameService.findById(id);

        if (miniGame == null) {
            return "redirect:/admin";
        }

        model.addAttribute("miniGame", miniGame);
        model.addAttribute(
                "tasks",
                miniGameTaskService.findAllByGame(miniGame)
        );

        return "admin-mini-game-tasks";
    }

    @PostMapping("/mini-game/{id}/tasks/add")
    public String addMiniGameTask(
            @PathVariable Long id,
            @RequestParam String content,
            @RequestParam String answer
    ) {
        MiniGame miniGame = miniGameService.findById(id);

        if (miniGame != null) {
            MiniGameTask task = new MiniGameTask(
                    miniGame,
                    content,
                    answer
            );

            miniGameTaskService.save(task);
        }

        return "redirect:/admin/mini-game/" + id + "/tasks";
    }

    @PostMapping("/mini-game/task/delete/{id}")
    public String deleteMiniGameTask(
            @PathVariable Long id
    ) {
        MiniGameTask task = miniGameTaskService.findById(id);

        if (task == null) {
            return "redirect:/admin";
        }

        Long miniGameId = task.getMiniGame().getId();

        miniGameTaskService.delete(id);

        return "redirect:/admin/mini-game/"
                + miniGameId
                + "/tasks";
    }

    @PostMapping("/mini-game/task/edit/{id}")
    public String editMiniGameTask(
            @PathVariable Long id,
            @RequestParam String content,
            @RequestParam String answer,
            @RequestParam(defaultValue = "false") boolean active
    ) {
        MiniGameTask task = miniGameTaskService.findById(id);

        if (task == null) {
            return "redirect:/admin";
        }

        Long miniGameId = task.getMiniGame().getId();

        task.setContent(content);
        task.setAnswer(answer);
        task.setActive(active);

        miniGameTaskService.save(task);

        return "redirect:/admin/mini-game/" + miniGameId + "/tasks";
    }


}
