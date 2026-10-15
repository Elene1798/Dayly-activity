package com.example.dailyactivity.service;

import com.example.dailyactivity.model.*;
import com.example.dailyactivity.repository.AdventActivityRepository;
import com.example.dailyactivity.repository.AdventDayRepository;
import com.example.dailyactivity.repository.ActivityCompletionRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdventService {

    private final AdventActivityRepository activityRepository;
    private final AdventDayRepository dayRepository;
    private final ActivityCompletionRepository completionRepository;

    public AdventService(
            AdventActivityRepository activityRepository,
            AdventDayRepository dayRepository,
            ActivityCompletionRepository completionRepository
    ) {
        this.activityRepository = activityRepository;
        this.dayRepository = dayRepository;
        this.completionRepository = completionRepository;
    }

    //  СЕЗОН
    public AdventSeason getCurrentSeason() {
        return AdventSeason.fromMonth(LocalDate.now().getMonthValue());
    }

    public boolean isAdventActive() {
        return getCurrentSeason() != null;
    }

    public String getCurrentSeasonLabel() {
        AdventSeason season = getCurrentSeason();
        return season != null ? season.getLabel() : null;
    }

    //  ГЕНЕРАЦИЯ РАСПИСАНИЯ

    @Transactional
    public List<AdventDay> getOrCreateAdventForCurrentSeason(User user) {

        AdventSeason season = getCurrentSeason();

        if (season == null) {
            return List.of();
        }

        LocalDate today = LocalDate.now();
        YearMonth yearMonth = YearMonth.from(today);

        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        if (dayRepository.existsByUserAndAdventDateBetween(user, start, end)) {
            return dayRepository.findByUserAndAdventDateBetween(user, start, end);
        }
        return generateAdventFor(user, season, yearMonth);
    }

    /**
     * Генерирует расписание для пользователя на указанный месяц.
     */
    @Transactional
    protected List<AdventDay> generateAdventFor(
            User user,
            AdventSeason season,
            YearMonth yearMonth
    ) {
        int daysInMonth = yearMonth.lengthOfMonth();

        List<AdventActivity> regular =
                activityRepository.findBySeasonAndSpecialFalse(season);

        List<AdventActivity> special =
                activityRepository.findBySeasonAndSpecialTrue(season);

        if (regular.isEmpty() && special.isEmpty()) {
            return List.of();
        }

        // Проверяем, что заданий хватает (у нас должно быть >= дней)
        // Если меньше — просто используем сколько есть.
        List<AdventActivity> pool = new ArrayList<>();

        // Перемешиваем обычные
        List<AdventActivity> shuffledRegular = new ArrayList<>(regular);
        Collections.shuffle(shuffledRegular);

        // Особые — привязываем к конкретным датам
        Map<Integer, AdventActivity> specialByDay = new HashMap<>();
        for (AdventActivity sp : special) {
            if (sp.getSpecialDay() == null) continue;

            int dayOfMonth = sp.getSpecialDay();
            if (dayOfMonth >= 1 && dayOfMonth <= daysInMonth) {
                specialByDay.put(dayOfMonth, sp);
            }
        }

        // Раскидываем
        List<AdventDay> result = new ArrayList<>();
        int regularIndex = 0;

        for (int day = 1; day <= daysInMonth; day++) {

            LocalDate date = yearMonth.atDay(day);
            AdventActivity chosen;

            if (specialByDay.containsKey(day)) {
                chosen = specialByDay.get(day);
            } else {
                if (regularIndex < shuffledRegular.size()) {
                    chosen = shuffledRegular.get(regularIndex++);
                } else {
                    // Задания закончились — но у нас больше 31, не должно случиться
                    // На всякий случай — пропускаем день
                    continue;
                }
            }

            AdventDay adventDay = new AdventDay(user, date, chosen);
            result.add(adventDay);
        }

        return dayRepository.saveAll(result);
    }

    @Transactional
    public boolean openDay(User user, LocalDate date) {

        // Можно открывать только сегодняшние или прошедшие
        if (date.isAfter(LocalDate.now())) {
            return false;
        }

        Optional<AdventDay> opt =
                dayRepository.findByUserAndAdventDate(user, date);

        if (opt.isEmpty()) {
            return false;
        }

        AdventDay day = opt.get();

        if (day.isOpened()) {
            return false; // уже открыта, анимацию не проигрываем
        }

        day.setOpened(true);
        day.setOpenedAt(LocalDateTime.now());
        dayRepository.save(day);

        return true;
    }

    //  ВЫПОЛНЕНИЕ ЗАДАНИЯ

    @Transactional
    public boolean completeDay(User user, LocalDate date) {

        Optional<AdventDay> opt =
                dayRepository.findByUserAndAdventDate(user, date);

        if (opt.isEmpty()) {
            return false;
        }

        AdventDay day = opt.get();

        if (day.isCompleted()) {
            return false; // уже отмечено
        }

        day.setCompleted(true);
        day.setCompletedAt(LocalDateTime.now());
        dayRepository.save(day);

        // Создаём запись в общей активности
        ActivityCompletion completion = new ActivityCompletion(
                user,
                day.getActivity(),
                LocalDateTime.now(),
                "ADVENT"
        );

        try {
            completionRepository.save(completion);
        } catch (DataIntegrityViolationException ignored) {
            // Если вдруг сработала уникальность — просто игнорируем
        }

        return true;
    }

    //  ПРОГРЕСС

    /**
     * Сколько ячеек пользователь открыл в текущем сезоне.
     */
    public long getOpenedCountForCurrentSeason(User user) {

        AdventSeason season = getCurrentSeason();
        if (season == null) return 0;

        YearMonth ym = YearMonth.now();
        return dayRepository.countByUserAndAdventDateBetweenAndOpenedTrue(
                user,
                ym.atDay(1),
                ym.atEndOfMonth()
        );
    }

    /**
     * Сколько ячеек пользователь выполнил в текущем сезоне.
     */
    public long getCompletedCountForCurrentSeason(User user) {

        AdventSeason season = getCurrentSeason();
        if (season == null) return 0;

        YearMonth ym = YearMonth.now();

        return dayRepository
                .findByUserAndAdventDateBetween(
                        user, ym.atDay(1), ym.atEndOfMonth()
                )
                .stream()
                .filter(AdventDay::isCompleted)
                .count();
    }

    //  АДМИНКА

    public List<AdventActivity> getActivitiesBySeason(AdventSeason season) {
        return activityRepository.findBySeason(season);
    }

    @Transactional
    public AdventActivity save(AdventActivity activity) {
        return activityRepository.save(activity);
    }

    @Transactional
    public void delete(Long id) {
        activityRepository.deleteById(id);
    }

    public AdventActivity findById(Long id) {
        return activityRepository.findById(id).orElse(null);
    }

    public List<AdventActivity> getAllActivities() {
        return activityRepository.findAll();
    }

    public long countBySeason(AdventSeason season) {
        return activityRepository.countBySeason(season);
    }
}