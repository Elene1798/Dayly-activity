package com.example.dailyactivity.service;

import com.example.dailyactivity.model.User;
import com.example.dailyactivity.model.UserVisit;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class VisitStreakService {

    private final UserVisitService userVisitService;

    public VisitStreakService(UserVisitService userVisitService) {
        this.userVisitService = userVisitService;
    }

    /**
     * Возвращает текущую непрерывную серию посещений.
     *
     * Если сегодня пользователь ещё не заходил,
     * серия считается от вчерашнего дня.
     *
     * Если пропущен хотя бы один день,
     * серия начинается заново.
     */
    public int getCurrentStreak(User user) {

        List<UserVisit> visits =
                userVisitService.getUserVisits(user);

        if (visits.isEmpty()) {
            return 0;
        }

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        LocalDate lastVisit =
                visits.get(visits.size() - 1).getVisitDate();

        // Если последний визит был раньше вчерашнего дня,
        // текущая серия уже прервана.
        if (!lastVisit.equals(today)
                && !lastVisit.equals(yesterday)) {
            return 0;
        }

        int streak = 1;

        LocalDate previousDate = lastVisit;

        for (int i = visits.size() - 2; i >= 0; i--) {

            LocalDate currentDate =
                    visits.get(i).getVisitDate();

            if (currentDate.equals(previousDate.minusDays(1))) {
                streak++;
                previousDate = currentDate;
            } else if (currentDate.equals(previousDate)) {
                // Защита от повторной даты.
                continue;
            } else {
                break;
            }
        }

        return streak;
    }
}