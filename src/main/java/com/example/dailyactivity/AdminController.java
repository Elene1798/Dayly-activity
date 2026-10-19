package com.example.dailyactivity;

import com.example.dailyactivity.model.*;
import com.example.dailyactivity.repository.ActivityRepository;
import com.example.dailyactivity.repository.DailyRecommendationRepository;
import com.example.dailyactivity.service.AdventService;
import com.example.dailyactivity.service.MiniGameService;
import com.example.dailyactivity.service.MiniGameTaskService;
import com.example.dailyactivity.service.PuzzleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ActivityRepository activityRepository;
    private final MiniGameService miniGameService;
    private final MiniGameTaskService miniGameTaskService;
    private final AdventService adventService;
    private final DailyRecommendationRepository dailyRecommendationRepository;
    private final PuzzleService puzzleService;

    public AdminController(
            ActivityRepository activityRepository,
            MiniGameService miniGameService,
            MiniGameTaskService miniGameTaskService,
            AdventService adventService,
            DailyRecommendationRepository dailyRecommendationRepository,
            PuzzleService puzzleService
    ) {
        this.activityRepository = activityRepository;
        this.miniGameService = miniGameService;
        this.miniGameTaskService = miniGameTaskService;
        this.adventService = adventService;
        this.dailyRecommendationRepository = dailyRecommendationRepository;
        this.puzzleService = puzzleService;
    }

    // =========================================================
    //  ГЛАВНАЯ АДМИНКИ
    // =========================================================

    @GetMapping
    public String adminPage(Model model) {
        model.addAttribute("activities", activityRepository.findAll());
        model.addAttribute("miniGames", miniGameService.findAll());

        // Адвент
        model.addAttribute("adventActivities", adventService.getAllActivities());
        model.addAttribute("adventSeasons", AdventSeason.values());

        Map<AdventSeason, Long> counts = new EnumMap<>(AdventSeason.class);
        for (AdventSeason s : AdventSeason.values()) {
            counts.put(s, adventService.countBySeason(s));
        }
        model.addAttribute("adventSeasonCounts", counts);

        model.addAttribute(
                "currentSeason",
                AdventSeason.fromMonth(LocalDate.now().getMonthValue())
        );

        return "admin";
    }

    // =========================================================
    //  ОБЫЧНЫЕ ЗАНЯТИЯ (без изменений)
    // =========================================================

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

        Activity activity = new Activity(
                title, description, category, duration, location,
                instructions, benefit, interestingFact,
                imageUrl, linkUrl, videoUrl
        );
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
        dailyRecommendationRepository.deleteByActivityId(id);
        activityRepository.deleteById(id);
        return "redirect:/admin";
    }

    // =========================================================
    //  МИНИ-ИГРЫ (без изменений)
    // =========================================================

    @PostMapping("/mini-game/add")
    public String addMiniGame(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam String gameType,
            @RequestParam String category,
            @RequestParam(required = false) Integer duration,
            @RequestParam(required = false) String instructions,
            @RequestParam(defaultValue = "false") boolean addToActivityCard
    ) {
        MiniGame miniGame = new MiniGame(
                title, description, gameType, category, duration, instructions
        );
        miniGame.setAddToActivityCard(addToActivityCard);
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
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer duration,
            @RequestParam(required = false) String instructions,
            @RequestParam(defaultValue = "false") boolean active,
            @RequestParam(defaultValue = "false") boolean addToActivityCard
    ) {
        MiniGame miniGame = miniGameService.findById(id);

        if (miniGame != null) {
            miniGame.setTitle(title);
            miniGame.setDescription(description);
            miniGame.setGameType(gameType);

            if (category != null && !category.isBlank()) {
                miniGame.setCategory(category);
            }

            if (duration != null && duration > 0) {
                miniGame.setDuration(duration);
            }

            miniGame.setInstructions(instructions);
            miniGame.setActive(active);
            miniGame.setAddToActivityCard(addToActivityCard);

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
        model.addAttribute("tasks", miniGameTaskService.findAllByGame(miniGame));

        return "admin-mini-game-tasks";
    }

    @PostMapping("/mini-game/{id}/tasks/add")
    public String addMiniGameTask(
            @PathVariable Long id,
            @RequestParam String content,
            @RequestParam(required = false) String answer,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) String imageUrl
    ) {
        MiniGame miniGame = miniGameService.findById(id);

        if (miniGame != null) {

            if ("CROCODILE".equalsIgnoreCase(miniGame.getGameType())) {
                answer = content;
            }

            MiniGameTask task = new MiniGameTask(miniGame, content, answer);

            task.setDifficulty(difficulty);
            task.setTaskType(taskType);
            task.setImageUrl(imageUrl);

            miniGameTaskService.save(task);
        }

        return "redirect:/admin/mini-game/" + id + "/tasks";
    }

    @PostMapping("/mini-game/task/delete/{id}")
    public String deleteMiniGameTask(@PathVariable Long id) {
        MiniGameTask task = miniGameTaskService.findById(id);

        if (task == null) {
            return "redirect:/admin";
        }

        Long miniGameId = task.getMiniGame().getId();
        miniGameTaskService.delete(id);

        return "redirect:/admin/mini-game/" + miniGameId + "/tasks";
    }

    @PostMapping("/mini-game/task/edit/{id}")
    public String editMiniGameTask(
            @PathVariable Long id,
            @RequestParam String content,
            @RequestParam(required = false) String answer,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) String imageUrl,
            @RequestParam(defaultValue = "false") boolean active
    ) {
        MiniGameTask task = miniGameTaskService.findById(id);

        if (task != null) {

            if ("CROCODILE".equalsIgnoreCase(task.getMiniGame().getGameType())) {
                answer = content;
            }

            task.setContent(content);
            task.setAnswer(answer);
            task.setDifficulty(difficulty);
            task.setTaskType(taskType);
            task.setImageUrl(imageUrl);
            task.setActive(active);

            miniGameTaskService.save(task);

            return "redirect:/admin/mini-game/"
                    + task.getMiniGame().getId()
                    + "/tasks";
        }

        return "redirect:/admin";
    }

    // =========================================================
    //  АДВЕНТ-АКТИВНОСТИ
    // =========================================================

    @PostMapping("/advent/add")
    public String addAdventActivity(
            @RequestParam String season,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String instructions,
            @RequestParam(required = false) String icon,
            @RequestParam(required = false) String imageUrl,
            @RequestParam(required = false) String videoUrl,
            @RequestParam(required = false) String linkUrl,
            @RequestParam(defaultValue = "false") boolean special,
            @RequestParam(required = false) Integer specialDay,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        AdventSeason adventSeason;
        try {
            adventSeason = AdventSeason.valueOf(season);
        } catch (Exception e) {
            return "redirect:/admin?adventError=bad-season";
        }

        AdventActivity activity = new AdventActivity(adventSeason, title);
        activity.setDescription(blankToNull(description));
        activity.setInstructions(blankToNull(instructions));
        activity.setIcon(blankToNull(icon));
        activity.setImageUrl(blankToNull(imageUrl));
        activity.setVideoUrl(blankToNull(videoUrl));
        activity.setLinkUrl(blankToNull(linkUrl));
        activity.setSpecial(special);

        if (special) {
            if (specialDay == null || specialDay < 1 || specialDay > 31) {
                return "redirect:/admin?adventError=special-day-required";
            }

            int year = LocalDate.now().getYear();
            int daysInMonth = java.time.YearMonth
                    .of(year, adventSeason.getMonth())
                    .lengthOfMonth();

            if (specialDay > daysInMonth) {
                return "redirect:/admin?adventError=day-outside-month";
            }

            activity.setSpecialDay(specialDay);
        } else {
            activity.setSpecialDay(null);
        }

        adventService.save(activity, image);

        return "redirect:/admin?adventSuccess=added";
    }

    @PostMapping("/advent/edit/{id}")
    public String editAdventActivity(
            @PathVariable Long id,
            @RequestParam String season,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String instructions,
            @RequestParam(required = false) String icon,
            @RequestParam(required = false) String imageUrl,
            @RequestParam(required = false) String videoUrl,
            @RequestParam(required = false) String linkUrl,
            @RequestParam(defaultValue = "false") boolean special,
            @RequestParam(required = false) Integer specialDay,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        AdventActivity activity = adventService.findById(id);
        if (activity == null) {
            return "redirect:/admin?adventError=not-found";
        }

        AdventSeason adventSeason;
        try {
            adventSeason = AdventSeason.valueOf(season);
        } catch (Exception e) {
            return "redirect:/admin?adventError=bad-season";
        }

        activity.setSeason(adventSeason);
        activity.setTitle(title);
        activity.setDescription(blankToNull(description));
        activity.setInstructions(blankToNull(instructions));
        activity.setIcon(blankToNull(icon));
        activity.setImageUrl(blankToNull(imageUrl));
        activity.setVideoUrl(blankToNull(videoUrl));
        activity.setLinkUrl(blankToNull(linkUrl));
        activity.setSpecial(special);

        if (special) {
            if (specialDay == null || specialDay < 1 || specialDay > 31) {
                return "redirect:/admin?adventError=special-day-required";
            }

            int year = LocalDate.now().getYear();
            int daysInMonth = java.time.YearMonth
                    .of(year, adventSeason.getMonth())
                    .lengthOfMonth();

            if (specialDay > daysInMonth) {
                return "redirect:/admin?adventError=day-outside-month";
            }

            activity.setSpecialDay(specialDay);
        } else {
            activity.setSpecialDay(null);
        }

        adventService.save(activity, image);

        return "redirect:/admin?adventSuccess=edited";
    }

    @PostMapping("/advent/delete/{id}")
    public String deleteAdventActivity(@PathVariable Long id) {
        adventService.delete(id);
        return "redirect:/admin?adventSuccess=deleted";
    }

    // =========================================================
    //  ХЕЛПЕРЫ
    // =========================================================

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    // =========================================================
//  ПАЗЛЫ (задания для мини-игры PUZZLE)
// =========================================================

    @PostMapping("/mini-game/{id}/puzzles/add")
    public String addPuzzle(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam int defaultSize,
            @RequestParam("image") MultipartFile image
    ) {
        MiniGame miniGame = miniGameService.findById(id);

        if (miniGame == null || !"PUZZLE".equalsIgnoreCase(miniGame.getGameType())) {
            return "redirect:/admin";
        }

        Puzzle puzzle = new Puzzle(title, null, defaultSize);
        puzzle.setMiniGame(miniGame);

        puzzleService.save(puzzle, image);

        return "redirect:/admin/mini-game/" + id + "/puzzles";
    }

    @PostMapping("/mini-game/puzzle/edit/{id}")
    public String editPuzzle(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam int defaultSize,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(defaultValue = "false") boolean active
    ) {
        Puzzle puzzle = puzzleService.findById(id);
        if (puzzle == null) return "redirect:/admin";

        puzzle.setTitle(title);
        puzzle.setDefaultSize(defaultSize);
        puzzle.setActive(active);

        puzzleService.save(puzzle, image);

        return "redirect:/admin/mini-game/"
                + puzzle.getMiniGame().getId()
                + "/puzzles";
    }

    @PostMapping("/mini-game/puzzle/delete/{id}")
    public String deletePuzzle(@PathVariable Long id) {
        Puzzle puzzle = puzzleService.findById(id);
        if (puzzle == null) return "redirect:/admin";

        Long miniGameId = puzzle.getMiniGame().getId();
        puzzleService.delete(id);

        return "redirect:/admin/mini-game/" + miniGameId + "/puzzles";
    }

    @GetMapping("/mini-game/{id}/puzzles")
    public String miniGamePuzzles(
            @PathVariable Long id,
            Model model
    ) {
        MiniGame miniGame = miniGameService.findById(id);

        if (miniGame == null
                || !"PUZZLE".equalsIgnoreCase(miniGame.getGameType())) {
            return "redirect:/admin";
        }

        model.addAttribute("miniGame", miniGame);
        model.addAttribute("puzzles", puzzleService.getByMiniGame(miniGame));

        return "admin-mini-game-puzzles";
    }
}