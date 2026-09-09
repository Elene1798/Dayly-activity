package com.example.dailyactivity;

import com.example.dailyactivity.model.User;
import com.example.dailyactivity.repository.FavoriteRepository;
import com.example.dailyactivity.repository.LikeRepository;
import com.example.dailyactivity.repository.UserRepository;
import org.springframework.security.core.Authentication;

import com.example.dailyactivity.model.Activity;
import com.example.dailyactivity.service.ActivityService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Controller
public class HomeController {

    private final ActivityService activityService;

    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final FavoriteRepository favoriteRepository;

    public HomeController(
            ActivityService activityService,
            UserRepository userRepository,
            LikeRepository likeRepository,
            FavoriteRepository favoriteRepository) {

        this.activityService = activityService;
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
        this.favoriteRepository = favoriteRepository;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/activity")
    public String getActivity(
            @RequestParam String category,
            @RequestParam int duration,
            @RequestParam String location,
            @RequestParam(required = false) Long activityId,
            Model model,
            HttpSession session,
            Authentication authentication) {

        String historyKey = category + "|" + duration + "|" + location;

        Map<String, Set<Long>> history =
                (Map<String, Set<Long>>) session.getAttribute("activityHistory");

        if (history == null) {
            history = new HashMap<>();
            session.setAttribute("activityHistory", history);
        }

        Set<Long> usedIds = history.computeIfAbsent(
                historyKey,
                key -> new HashSet<>()
        );

        Activity activity;

        if (activityId != null) {

            activity = activityService
                    .getActivityById(activityId)
                    .orElse(null);

        } else {

            activity = activityService.getRandomActivity(
                    category,
                    duration,
                    location,
                    usedIds
            );

            if (activity != null) {
                usedIds.add(activity.getId());
            }
        }

        if (activity == null) {
            model.addAttribute(
                    "message",
                    "Пока нет подходящих занятий, попробуй изменить категорию, время или место"
            );
        }

        model.addAttribute(
                "activity",
                activity
        );

        model.addAttribute(
                "selectedCategory",
                category
        );

        model.addAttribute(
                "selectedDuration",
                duration
        );

        model.addAttribute(
                "selectedLocation",
                location
        );

        boolean liked = false;
        boolean favorite = false;

        if (authentication != null &&
                authentication.isAuthenticated() &&
                !authentication.getName().equals("anonymousUser")) {

            User user = userRepository
                    .findByUsername(authentication.getName())
                    .orElse(null);

            if (user != null && activity != null) {

                liked = likeRepository
                        .findByUserAndActivity(user, activity)
                        .isPresent();

                favorite = favoriteRepository
                        .findByUserAndActivity(user, activity)
                        .isPresent();
            }
        }

        model.addAttribute("liked", liked);
        model.addAttribute("favorite", favorite);
        model.addAttribute(
                "loggedIn",
                authentication != null &&
                        authentication.isAuthenticated() &&
                        !authentication.getName().equals("anonymousUser")
        );

        return "activity";
    }
}
