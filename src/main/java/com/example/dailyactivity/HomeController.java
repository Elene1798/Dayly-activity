package com.example.dailyactivity;

import com.example.dailyactivity.model.User;
import com.example.dailyactivity.repository.DailyRecommendationRepository;
import com.example.dailyactivity.repository.FavoriteRepository;
import com.example.dailyactivity.repository.LikeRepository;
import com.example.dailyactivity.repository.UserRepository;
import com.example.dailyactivity.service.RecommendationService;
import org.springframework.security.core.Authentication;

import com.example.dailyactivity.model.Activity;
import com.example.dailyactivity.service.ActivityService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Controller
public class HomeController {

    private final ActivityService activityService;
    private final RecommendationService recommendationService;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final FavoriteRepository favoriteRepository;
    private final DailyRecommendationRepository dailyRecommendationRepository;

    public HomeController(
            ActivityService activityService,
            RecommendationService recommendationService,
            UserRepository userRepository,
            LikeRepository likeRepository,
            FavoriteRepository favoriteRepository,
            DailyRecommendationRepository dailyRecommendationRepository) {

        this.activityService = activityService;
        this.recommendationService = recommendationService;
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
        this.favoriteRepository = favoriteRepository;
        this.dailyRecommendationRepository = dailyRecommendationRepository;
    }

    @GetMapping("/")
    public String home(HttpSession session) {
        // Возвращение на главную означает новый выбор.
        // Старый список "Другое занятие" больше не должен влиять
        // на следующий вход в категорию.
        session.removeAttribute("activityHistory");

        return "index";
    }

    @GetMapping("/surprise")
    public String surprise(
            @RequestParam(required = false) Long activityId,
            Model model,
            HttpSession session,
            Authentication authentication) {

        Map<String, Set<Long>> history =
                (Map<String, Set<Long>>) session.getAttribute(
                        "activityHistory"
                );

        if (history == null) {
            history = new HashMap<>();
            session.setAttribute("activityHistory", history);
        }

        Set<Long> usedIds = history.computeIfAbsent(
                "surprise",
                key -> new HashSet<>()
        );

        Activity activity;
        boolean personalRecommendation = false;

        if (activityId != null) {

            activity = activityService
                    .getActivityById(activityId)
                    .orElse(null);

        } else {

            activity = null;

            boolean loggedIn =
                    authentication != null
                            && authentication.isAuthenticated()
                            && !"anonymousUser".equals(
                            authentication.getPrincipal()
                    );

            if (loggedIn) {

                User user = userRepository
                        .findByUsername(authentication.getName())
                        .orElse(null);

                if (user != null) {

                    LocalDate today = LocalDate.now();

                    DailyRecommendation dailyRecommendation =
                            dailyRecommendationRepository
                                    .findByUserAndRecommendationDate(
                                            user,
                                            today
                                    )
                                    .orElse(null);

                    if (dailyRecommendation != null) {

                    } else {

                        Activity recommended =
                                recommendationService
                                        .getPersonalRecommendation(user);

                        if (recommended != null) {

                            dailyRecommendation =
                                    new DailyRecommendation(
                                            user,
                                            recommended,
                                            today
                                    );

                            dailyRecommendationRepository.save(
                                    dailyRecommendation
                            );

                            activity = recommended;
                            personalRecommendation = true;

                            usedIds.add(recommended.getId());
                        }
                    }
                }
            }

            if (activity == null) {

                activity =
                        activityService.getRandomSurpriseActivity(usedIds);

                if (activity != null) {
                    usedIds.add(activity.getId());
                }
            }
        }

        model.addAttribute(
                "personalRecommendation",
                personalRecommendation
        );

        model.addAttribute("activity", activity);

        model.addAttribute(
                "selectedCategory",
                activity != null ? activity.getCategory() : null
        );

        model.addAttribute(
                "selectedDuration",
                activity != null ? activity.getDuration() : null
        );

        model.addAttribute(
                "selectedLocation",
                activity != null ? activity.getLocation() : null
        );

        model.addAttribute("surprise", true);

        boolean loggedIn =
                authentication != null
                        && authentication.isAuthenticated()
                        && !"anonymousUser".equals(
                        authentication.getPrincipal()
                );

        model.addAttribute("loggedIn", loggedIn);

        if (loggedIn && activity != null) {

            User user = userRepository
                    .findByUsername(authentication.getName())
                    .orElse(null);

            if (user != null) {

                model.addAttribute(
                        "liked",
                        likeRepository
                                .findByUserAndActivity(user, activity)
                                .isPresent()
                );

                model.addAttribute(
                        "favorite",
                        favoriteRepository
                                .findByUserAndActivity(user, activity)
                                .isPresent()
                );

            } else {
                model.addAttribute("liked", false);
                model.addAttribute("favorite", false);
            }

        } else {
            model.addAttribute("liked", false);
            model.addAttribute("favorite", false);
        }

        return "activity";
    }

    @GetMapping("/activity")
    public String getActivity(
            @RequestParam String category,
            @RequestParam(required = false) Integer duration,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Long activityId,
            Model model,
            HttpSession session,
            Authentication authentication) {

        String durationKey =
                duration == null ? "any" : String.valueOf(duration);

        String locationKey =
                location == null ? "any" : location;

        String historyKey =
                category + "|" + durationKey + "|" + locationKey;

        Map<String, Set<Long>> history =
                (Map<String, Set<Long>>) session.getAttribute(
                        "activityHistory"
                );

        if (history == null) {
            history = new HashMap<>();

            session.setAttribute(
                    "activityHistory",
                    history
            );
        }

        Set<Long> usedIds = history.computeIfAbsent(
                historyKey,
                key -> new HashSet<>()
        );

        Activity activity;

        /*
         * Если открываем конкретное занятие,
         * например из избранного или после лайка,
         * загружаем его непосредственно по ID.
         */
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

        model.addAttribute(
                "surprise",
                false
        );

        boolean loggedIn =
                authentication != null
                        && authentication.isAuthenticated()
                        && !"anonymousUser".equals(
                        authentication.getPrincipal()
                );

        model.addAttribute(
                "loggedIn",
                loggedIn
        );

        if (loggedIn && activity != null) {

            User user = userRepository
                    .findByUsername(authentication.getName())
                    .orElse(null);

            if (user != null) {

                model.addAttribute(
                        "liked",
                        likeRepository.findByUserAndActivity(user, activity).isPresent()
                );

                model.addAttribute(
                        "favorite",
                        favoriteRepository.findByUserAndActivity(user, activity).isPresent()
                );

            } else {
                model.addAttribute("liked", false);
                model.addAttribute("favorite", false);
            }

        } else {
            model.addAttribute("liked", false);
            model.addAttribute("favorite", false);
        }

        return "activity";
    }
}