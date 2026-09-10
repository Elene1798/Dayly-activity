package com.example.dailyactivity;

import com.example.dailyactivity.model.Activity;
import com.example.dailyactivity.repository.ActivityRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ActivityRepository activityRepository;

    public AdminController(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @GetMapping
    public String adminPage(Model model) {

        model.addAttribute(
                "activities",
                activityRepository.findAll()
        );

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

        Activity activity = new Activity(
                title,
                description,
                category,
                duration,
                location,
                instructions,
                benefit,
                interestingFact,
                imageUrl,
                linkUrl,
                videoUrl
        );

        activityRepository.save(activity);

        return "redirect:/admin";
    }

    @PostMapping("/delete/{id}")
    public String deleteActivity(
            @PathVariable Long id) {

        activityRepository.deleteById(id);

        return "redirect:/admin";
    }
}