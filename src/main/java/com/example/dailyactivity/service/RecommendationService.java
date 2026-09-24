package com.example.dailyactivity.service;

import com.example.dailyactivity.model.Activity;
import com.example.dailyactivity.model.Favorite;
import com.example.dailyactivity.model.Like;
import com.example.dailyactivity.model.User;
import com.example.dailyactivity.repository.ActivityRepository;
import com.example.dailyactivity.repository.FavoriteRepository;
import com.example.dailyactivity.repository.LikeRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@Service
public class RecommendationService {

    private final LikeRepository likeRepository;
    private final FavoriteRepository favoriteRepository;
    private final ActivityRepository activityRepository;

    private final Random random = new Random();

    public RecommendationService(
            LikeRepository likeRepository,
            FavoriteRepository favoriteRepository,
            ActivityRepository activityRepository) {

        this.likeRepository = likeRepository;
        this.favoriteRepository = favoriteRepository;
        this.activityRepository = activityRepository;
    }

    public Activity getPersonalRecommendation(User user) {

        List<Like> likes = likeRepository.findByUser(user);
        List<Favorite> favorites = favoriteRepository.findByUser(user);

        if (likes.isEmpty() && favorites.isEmpty()) {
            return null;
        }

        Map<String, Integer> categoryScores = new HashMap<>();

        Set<Long> excludedActivityIds = new HashSet<>();

        for (Like like : likes) {
            Activity activity = like.getActivity();

            if (activity != null) {
                excludedActivityIds.add(activity.getId());

                categoryScores.merge(
                        activity.getCategory(),
                        1,
                        Integer::sum
                );
            }
        }

        for (Favorite favorite : favorites) {
            Activity activity = favorite.getActivity();

            if (activity != null) {
                excludedActivityIds.add(activity.getId());

                categoryScores.merge(
                        activity.getCategory(),
                        2,
                        Integer::sum
                );
            }
        }

        if (categoryScores.isEmpty()) {
            return null;
        }

        String preferredCategory = categoryScores.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        if (preferredCategory == null) {
            return null;
        }

        List<Activity> candidates =
                activityRepository.findByCategory(preferredCategory)
                        .stream()
                        .filter(activity ->
                                !excludedActivityIds.contains(activity.getId()))
                        .toList();

        if (candidates.isEmpty()) {
            return null;
        }

        return candidates.get(
                random.nextInt(candidates.size())
        );
    }
}
