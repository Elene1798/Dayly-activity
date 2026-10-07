package com.example.dailyactivity;

import com.example.dailyactivity.model.AchievementType;
import com.example.dailyactivity.model.User;
import com.example.dailyactivity.model.UserAchievement;
import com.example.dailyactivity.repository.UserRepository;
import com.example.dailyactivity.service.AchievementService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class AchievementsController {

    private final UserRepository userRepository;
    private final AchievementService achievementService;

    public AchievementsController(
            UserRepository userRepository,
            AchievementService achievementService) {

        this.userRepository = userRepository;
        this.achievementService = achievementService;
    }

    @GetMapping("/achievements")
    public String achievements(
            Authentication authentication,
            Model model) {

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

        List<UserAchievement> userAchievements =
                achievementService.getUserAchievements(user);

        Set<String> earnedKeys = new HashSet<>();

        for (UserAchievement achievement : userAchievements) {
            earnedKeys.add(achievement.getAchievementKey());
        }

        List<AchievementView> achievements =
                List.of(AchievementType.values())
                        .stream()
                        .map(type -> new AchievementView(
                                type,
                                earnedKeys.contains(type.getKey())
                        ))
                        .toList();

        model.addAttribute("achievements", achievements);
        model.addAttribute("earnedCount", earnedKeys.size());
        model.addAttribute(
                "totalCount",
                AchievementType.values().length
        );

        return "achievements";
    }

    public static class AchievementView {

        private final AchievementType type;
        private final boolean earned;

        public AchievementView(
                AchievementType type,
                boolean earned) {

            this.type = type;
            this.earned = earned;
        }

        public AchievementType getType() {
            return type;
        }

        public boolean isEarned() {
            return earned;
        }
    }
}