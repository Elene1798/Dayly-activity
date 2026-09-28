package com.example.dailyactivity;

import com.example.dailyactivity.model.*;
import com.example.dailyactivity.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
public class LikeFavoriteController {

    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final LikeRepository likeRepository;
    private final FavoriteRepository favoriteRepository;
    private final ActivityCompletionRepository activityCompletionRepository;

    public LikeFavoriteController(
            UserRepository userRepository,
            ActivityRepository activityRepository,
            LikeRepository likeRepository,
            FavoriteRepository favoriteRepository,
            ActivityCompletionRepository activityCompletionRepository) {

        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
        this.likeRepository = likeRepository;
        this.favoriteRepository = favoriteRepository;
        this.activityCompletionRepository = activityCompletionRepository;
    }

    @PostMapping("/activity/{activityId}/like")
    public String toggleLike(
            @PathVariable Long activityId,
            @RequestParam String category,
            @RequestParam(required = false) Integer duration,
            @RequestParam(required = false) String location,
            @RequestParam(required = false, defaultValue = "false") boolean surprise,
            Authentication authentication) {

        User user = getCurrentUser(authentication);

        if (user == null) {
            return "redirect:/login";
        }

        Activity activity = activityRepository
                .findById(activityId)
                .orElse(null);

        if (activity == null) {
            return "redirect:/";
        }

        likeRepository.findByUserAndActivity(user, activity)
                .ifPresentOrElse(
                        likeRepository::delete,
                        () -> likeRepository.save(new Like(user, activity))
                );

        if (surprise) {
            return "redirect:/surprise?activityId=" + activityId;
        }

        return "redirect:/activity?category="
                + category
                + (duration != null
                ? "&duration=" + duration
                : "")
                + (location != null
                ? "&location=" + location
                : "")
                + "&activityId="
                + activityId;
    }

    @PostMapping("/activity/{activityId}/favorite")
    public String toggleFavorite(
            @PathVariable Long activityId,
            @RequestParam String category,
            @RequestParam(required = false) Integer duration,
            @RequestParam(required = false) String location,
            @RequestParam(required = false, defaultValue = "false") boolean surprise,
            Authentication authentication) {

        User user = getCurrentUser(authentication);

        if (user == null) {
            return "redirect:/login";
        }

        Activity activity = activityRepository
                .findById(activityId)
                .orElse(null);

        if (activity == null) {
            return "redirect:/";
        }

        favoriteRepository.findByUserAndActivity(user, activity)
                .ifPresentOrElse(
                        favoriteRepository::delete,
                        () -> favoriteRepository.save(
                                new Favorite(user, activity)
                        )
                );

        if (surprise) {
            return "redirect:/surprise?activityId=" + activityId;
        }

        return "redirect:/activity?category="
                + category
                + (duration != null
                ? "&duration=" + duration
                : "")
                + (location != null
                ? "&location=" + location
                : "")
                + "&activityId="
                + activityId;
    }

    @PostMapping("/activity/{activityId}/complete")
    public String completeActivity(
            @PathVariable Long activityId,
            @RequestParam String category,
            @RequestParam(required = false) Integer duration,
            @RequestParam(required = false) String location,
            @RequestParam(required = false, defaultValue = "false") boolean surprise,
            Authentication authentication) {

        User user = getCurrentUser(authentication);

        if (user == null) {
            return "redirect:/login";
        }

        Activity activity = activityRepository
                .findById(activityId)
                .orElse(null);

        if (activity == null) {
            return "redirect:/";
        }

        ActivityCompletion completion = new ActivityCompletion(
                user,
                activity,
                LocalDateTime.now(),
                "CARD",
                null
        );

        activityCompletionRepository.save(completion);

        if (surprise) {
            return "redirect:/surprise?activityId=" + activityId;
        }

        return "redirect:/activity?category="
                + category
                + (duration != null
                ? "&duration=" + duration
                : "")
                + (location != null
                ? "&location=" + location
                : "")
                + "&activityId="
                + activityId;
    }

    @PostMapping("/activity/{activityId}/uncomplete")
    public String uncompleteActivity(
            @PathVariable Long activityId,
            @RequestParam String category,
            @RequestParam(required = false) Integer duration,
            @RequestParam(required = false) String location,
            @RequestParam(required = false, defaultValue = "false") boolean surprise,
            Authentication authentication) {

        User user = getCurrentUser(authentication);

        if (user == null) {
            return "redirect:/login";
        }

        // Ищем последнее выполнение этого занятия,
        // созданное именно с карточки.
        activityCompletionRepository
                .findFirstByUserAndActivityIdAndSourceOrderByCompletedAtDesc(
                        user,
                        activityId,
                        "CARD"
                )
                .ifPresent(activityCompletionRepository::delete);

        // Возвращаем пользователя на ту же страницу.
        if (surprise) {
            return "redirect:/surprise?activityId=" + activityId;
        }

        return "redirect:/activity?category=" + category
                + (duration != null ? "&duration=" + duration : "")
                + (location != null ? "&location=" + location : "")
                + "&activityId=" + activityId;
    }

    @GetMapping("/favorites")
    public String favorites(
            Authentication authentication,
            Model model) {

        User user = getCurrentUser(authentication);

        if (user == null) {
            return "redirect:/login";
        }

        var favorites = favoriteRepository.findByUser(user);

        // Одноразово заполняем снимки для избранного, созданного
        // до появления механизма snapshot. Пользовательские данные не теряются.
        boolean changed = false;
        for (Favorite favorite : favorites) {
            if (!favorite.hasSnapshot() && favorite.getActivity() != null) {
                favorite.copyFromActivity(favorite.getActivity());
                changed = true;
            }
        }
        if (changed) {
            favoriteRepository.saveAll(favorites);
        }

        model.addAttribute("favorites", favorites);
        return "favorites";
    }

    @PostMapping("/favorites/delete/{favoriteId}")
    public String deleteFavorite(
            @PathVariable Long favoriteId,
            Authentication authentication) {

        User user = getCurrentUser(authentication);

        if (user == null) {
            return "redirect:/login";
        }

        favoriteRepository.findById(favoriteId)
                .ifPresent(favorite -> {

                    if (favorite.getUser().getId().equals(user.getId())) {
                        favoriteRepository.delete(favorite);
                    }
                });

        return "redirect:/favorites";
    }

    private User getCurrentUser(Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication.getName().equals("anonymousUser")) {

            return null;
        }

        return userRepository
                .findByUsername(authentication.getName())
                .orElse(null);
    }
}