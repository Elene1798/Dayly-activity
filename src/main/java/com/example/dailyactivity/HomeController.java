package com.example.dailyactivity;

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

    public HomeController(ActivityService activityService) {
        this.activityService = activityService;
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
            Model model,
            HttpSession session) {

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

        Activity activity =
                activityService.getRandomActivity(
                        category,
                        duration,
                        location,
                        usedIds
                );
        if (activity != null) {
            usedIds.add(activity.getId());
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

        return "activity";
    }
}
