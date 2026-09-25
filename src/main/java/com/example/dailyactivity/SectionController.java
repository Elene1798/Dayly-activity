package com.example.dailyactivity;

import com.example.dailyactivity.model.User;
import com.example.dailyactivity.repository.UserRepository;
import com.example.dailyactivity.service.ChallengeService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class SectionController {

    private final UserRepository userRepository;
    private final ChallengeService challengeService;

    public SectionController(
            UserRepository userRepository,
            ChallengeService challengeService) {

        this.userRepository = userRepository;
        this.challengeService = challengeService;
    }

    @GetMapping("/section")
    public String section(
            Authentication authentication,
            Model model) {

        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalStateException("Пользователь не найден")
                );

        DailyChallenge challenge =
                challengeService.getTodayChallenge(user);

        long completedCount =
                challengeService.getCompletedCount(user);

        model.addAttribute("user", user);
        model.addAttribute("challenge", challenge);
        model.addAttribute("completedCount", completedCount);

        return "section";
    }

    @PostMapping("/section/challenge/start")
    public String startChallenge(
            Authentication authentication) {

        User user = userRepository.findByUsername(
                authentication.getName()
        ).orElseThrow(() ->
                new IllegalStateException("Пользователь не найден")
        );

        DailyChallenge challenge =
                challengeService.getTodayChallenge(user);

        if (challenge != null) {
            challengeService.startChallenge(challenge);
        }

        return "redirect:/section";
    }

    @PostMapping("/section/challenge/decline")
    public String declineChallenge(
            Authentication authentication) {

        User user = userRepository.findByUsername(
                authentication.getName()
        ).orElseThrow(() ->
                new IllegalStateException("Пользователь не найден")
        );

        DailyChallenge challenge =
                challengeService.getTodayChallenge(user);

        if (challenge != null) {
            challengeService.declineChallenge(challenge);
        }

        return "redirect:/section";
    }

    @PostMapping("/section/challenge/complete")
    public String completeChallenge(
            Authentication authentication) {

        User user = userRepository.findByUsername(
                authentication.getName()
        ).orElseThrow(() ->
                new IllegalStateException("Пользователь не найден")
        );

        DailyChallenge challenge =
                challengeService.getTodayChallenge(user);

        if (challenge != null) {
            challengeService.completeChallenge(challenge);
        }

        return "redirect:/section";
    }

    @PostMapping("/section/name")
    public String updateName(
            Authentication authentication,
            @RequestParam String displayName) {

        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalStateException("Пользователь не найден")
                );

        displayName = displayName.trim();

        if (!displayName.isEmpty()) {
            user.setDisplayName(displayName);
            userRepository.save(user);
        }

        return "redirect:/section";
    }
}