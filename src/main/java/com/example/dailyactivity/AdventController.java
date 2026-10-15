package com.example.dailyactivity;

import com.example.dailyactivity.model.*;
import com.example.dailyactivity.repository.UserRepository;
import com.example.dailyactivity.service.AdventService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Controller
@RequestMapping("/advent")
public class AdventController {

    private final AdventService adventService;
    private final UserRepository userRepository;

    public AdventController(
            AdventService adventService,
            UserRepository userRepository
    ) {
        this.adventService = adventService;
        this.userRepository = userRepository;
    }

    // =========================================================
    //  СТРАНИЦА АДВЕНТА
    // =========================================================

    @GetMapping
    public String adventPage(
            Model model,
            Authentication authentication
    ) {
        boolean loggedIn = isLoggedIn(authentication);
        model.addAttribute("loggedIn", loggedIn);

        AdventSeason currentSeason = adventService.getCurrentSeason();

        // Сезон сейчас не активен
        if (currentSeason == null) {
            model.addAttribute("adventActive", false);
            return "advent";
        }

        model.addAttribute("adventActive", true);
        model.addAttribute("season", currentSeason);
        model.addAttribute("seasonLabel", currentSeason.getLabel());
        model.addAttribute("seasonIcon", currentSeason.getIcon());
        model.addAttribute("seasonClass", currentSeason.name().toLowerCase());

        LocalDate today = LocalDate.now();
        YearMonth ym = YearMonth.from(today);

        model.addAttribute("daysInMonth", ym.lengthOfMonth());
        model.addAttribute("firstDayOfWeek", ym.atDay(1).getDayOfWeek().getValue());
        model.addAttribute("today", today);

        // Гость — показываем размытый календарь
        if (!loggedIn) {
            return "advent";
        }

        // Авторизованный — генерируем/получаем расписание
        User user = userRepository
                .findByUsername(authentication.getName())
                .orElse(null);

        if (user == null) {
            model.addAttribute("adventActive", false);
            return "advent";
        }

        List<AdventDay> days =
                adventService.getOrCreateAdventForCurrentSeason(user);

        model.addAttribute("days", days);

        model.addAttribute(
                "openedCount",
                adventService.getOpenedCountForCurrentSeason(user)
        );

        model.addAttribute(
                "completedCount",
                adventService.getCompletedCountForCurrentSeason(user)
        );

        return "advent";
    }

    // =========================================================
    //  ОТКРЫТИЕ ЯЧЕЙКИ (AJAX)
    // =========================================================

    @PostMapping("/open/{date}")
    public String openDay(
            @PathVariable String date,
            Model model,
            Authentication authentication
    ) {
        if (!isLoggedIn(authentication)) {
            return "fragments/advent-auth-required";
        }

        User user = userRepository
                .findByUsername(authentication.getName())
                .orElse(null);

        if (user == null) {
            return "fragments/advent-auth-required";
        }

        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(date);
        } catch (Exception e) {
            return "fragments/advent-day-error";
        }

        if (parsedDate.isAfter(LocalDate.now())) {
            return "fragments/advent-day-future";
        }

        adventService.openDay(user, parsedDate);

        AdventDay day = adventService
                .getOrCreateAdventForCurrentSeason(user)
                .stream()
                .filter(d -> d.getAdventDate().equals(parsedDate))
                .findFirst()
                .orElse(null);

        if (day == null) {
            return "fragments/advent-day-error";
        }

        model.addAttribute("day", day);
        model.addAttribute("activity", day.getActivity());
        model.addAttribute("loggedIn", true);

        return "fragments/advent-day-modal";
    }

    // =========================================================
    //  ОТМЕТКА «ВЫПОЛНЕНО» (AJAX)
    // =========================================================

    @PostMapping("/complete/{date}")
    @ResponseBody
    public ResponseEntity<String> completeDay(
            @PathVariable String date,
            Authentication authentication
    ) {
        if (!isLoggedIn(authentication)) {
            return ResponseEntity.status(403).body("AUTH_REQUIRED");
        }

        User user = userRepository
                .findByUsername(authentication.getName())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(403).body("AUTH_REQUIRED");
        }

        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(date);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("BAD_DATE");
        }

        boolean completed = adventService.completeDay(user, parsedDate);

        if (completed) {
            return ResponseEntity.ok("SUCCESS");
        }

        return ResponseEntity.ok("ALREADY_COMPLETED");
    }

    // =========================================================
    //  ВСПОМОГАТЕЛЬНОЕ
    // =========================================================

    private boolean isLoggedIn(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
    }
}