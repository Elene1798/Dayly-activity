package com.example.dailyactivity;

import com.example.dailyactivity.model.Activity;
import com.example.dailyactivity.service.ActivityService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
            Model model) {

        Activity activity =
                activityService.getRandomActivity(
                        category,
                        duration,
                        location
                );

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
