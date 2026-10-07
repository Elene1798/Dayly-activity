package com.example.dailyactivity.service;

import com.example.dailyactivity.model.User;
import com.example.dailyactivity.model.UserVisit;
import com.example.dailyactivity.repository.UserVisitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class UserVisitService {

    private final UserVisitRepository userVisitRepository;

    public UserVisitService(UserVisitRepository userVisitRepository) {
        this.userVisitRepository = userVisitRepository;
    }

    /**
     * Засчитать посещение пользователя за сегодняшний день.
     * Повторные заходы в тот же день ничего не добавляют.
     */
    @Transactional
    public void recordVisit(User user) {

        LocalDate today = LocalDate.now();

        if (userVisitRepository.existsByUserAndVisitDate(user, today)) {
            return;
        }

        UserVisit visit = new UserVisit(user, today);

        userVisitRepository.save(visit);
    }

    /**
     * Все дни посещений пользователя.
     */
    @Transactional(readOnly = true)
    public List<UserVisit> getUserVisits(User user) {
        return userVisitRepository.findByUserOrderByVisitDateAsc(user);
    }
}