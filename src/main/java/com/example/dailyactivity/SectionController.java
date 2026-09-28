package com.example.dailyactivity;

import com.example.dailyactivity.model.ActivityCompletion;
import com.example.dailyactivity.model.User;
import com.example.dailyactivity.repository.ActivityCompletionRepository;
import com.example.dailyactivity.repository.UserRepository;
import com.example.dailyactivity.service.ChallengeService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Controller
public class SectionController {

    private final UserRepository userRepository;
    private final ChallengeService challengeService;
    private final ActivityCompletionRepository activityCompletionRepository;

    public SectionController(
            UserRepository userRepository,
            ChallengeService challengeService,
            ActivityCompletionRepository activityCompletionRepository) {

        this.userRepository = userRepository;
        this.challengeService = challengeService;
        this.activityCompletionRepository = activityCompletionRepository;
    }

    @GetMapping("/section")
    public String section(
            Authentication authentication,
            Model model) {

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException("Пользователь не найден"));

        DailyChallenge challenge = challengeService.getTodayChallenge(user);
        long completedCount = challengeService.getCompletedCount(user);

        model.addAttribute("user", user);
        model.addAttribute("challenge", challenge);
        model.addAttribute("completedCount", completedCount);

        return "section";
    }

    @GetMapping("/my-activity")
    public String myActivity(
            Authentication authentication,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) String date,
            Model model) {

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException("Пользователь не найден"));

        // Если месяц не передан, показываем текущий.
        YearMonth currentMonth = YearMonth.now();

        int selectedYear = year != null ? year : currentMonth.getYear();
        int selectedMonth = month != null ? month : currentMonth.getMonthValue();

        // Защита от некорректных значений в URL.
        if (selectedMonth < 1 || selectedMonth > 12) {
            selectedYear = currentMonth.getYear();
            selectedMonth = currentMonth.getMonthValue();
        }

        YearMonth displayedMonth = YearMonth.of(selectedYear, selectedMonth);

        LocalDate firstDay = displayedMonth.atDay(1);
        LocalDate nextMonth = displayedMonth.plusMonths(1).atDay(1);

        LocalDateTime start = firstDay.atStartOfDay();
        LocalDateTime end = nextMonth.atStartOfDay();

        List<ActivityCompletion> monthCompletions =
                activityCompletionRepository
                        .findByUserAndCompletedAtGreaterThanEqualAndCompletedAtLessThanOrderByCompletedAtAsc(
                                user, start, end);

        LocalDate selectedDate = null;

        if (date != null && !date.isBlank()) {
            try {
                LocalDate parsedDate = LocalDate.parse(date);

                if (YearMonth.from(parsedDate).equals(displayedMonth)) {
                    selectedDate = parsedDate;
                }
            } catch (java.time.format.DateTimeParseException ignored) {
                // Некорректная дата не выбирается.
            }
        }

        // Если дата не выбрана, список событий пока пуст.
        List<ActivityCompletion> selectedDayCompletions = new ArrayList<>();

        if (selectedDate != null) {
            for (ActivityCompletion completion : monthCompletions) {
                if (completion.getCompletedAt() != null
                        && completion.getCompletedAt().toLocalDate()
                        .equals(selectedDate)) {
                    selectedDayCompletions.add(completion);
                }
            }
        }

        // Список ячеек календаря: сначала пустые ячейки до первого числа,
        // затем все дни месяца.
        List<CalendarDay> calendarDays = new ArrayList<>();

        int firstDayOffset =
                firstDay.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();

        for (int i = 0; i < firstDayOffset; i++) {
            calendarDays.add(CalendarDay.empty());
        }

        for (int day = 1; day <= displayedMonth.lengthOfMonth(); day++) {
            LocalDate currentDate = displayedMonth.atDay(day);

            long count = monthCompletions.stream()
                    .filter(completion -> completion.getCompletedAt() != null)
                    .filter(completion ->
                            completion.getCompletedAt().toLocalDate()
                                    .equals(currentDate))
                    .count();

            calendarDays.add(new CalendarDay(
                    currentDate,
                    day,
                    count,
                    currentDate.equals(LocalDate.now()),
                    currentDate.equals(selectedDate)
            ));
        }

        String[] monthNames = {
                "Январь",
                "Февраль",
                "Март",
                "Апрель",
                "Май",
                "Июнь",
                "Июль",
                "Август",
                "Сентябрь",
                "Октябрь",
                "Ноябрь",
                "Декабрь"
        };

        String monthName = monthNames[displayedMonth.getMonthValue() - 1];

        model.addAttribute("user", user);
        model.addAttribute("calendarDays", calendarDays);
        model.addAttribute("monthName", monthName);
        model.addAttribute("year", displayedMonth.getYear());
        model.addAttribute("month", displayedMonth.getMonthValue());
        model.addAttribute("previousMonth", displayedMonth.minusMonths(1));
        model.addAttribute("nextMonth", displayedMonth.plusMonths(1));
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("selectedDayCompletions", selectedDayCompletions);

        return "activity-calendar";
    }

    @PostMapping("/section/challenge/start")
    public String startChallenge(Authentication authentication) {

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException("Пользователь не найден"));

        DailyChallenge challenge = challengeService.getTodayChallenge(user);

        if (challenge != null) {
            challengeService.startChallenge(challenge);
        }

        return "redirect:/section";
    }

    @PostMapping("/section/challenge/decline")
    public String declineChallenge(Authentication authentication) {

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException("Пользователь не найден"));

        DailyChallenge challenge = challengeService.getTodayChallenge(user);

        if (challenge != null) {
            challengeService.declineChallenge(challenge);
        }

        return "redirect:/section";
    }

    @PostMapping("/section/challenge/complete")
    public String completeChallenge(Authentication authentication) {

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException("Пользователь не найден"));

        DailyChallenge challenge = challengeService.getTodayChallenge(user);

        if (challenge != null) {
            challengeService.completeChallenge(challenge);
        }

        return "redirect:/section";
    }

    @PostMapping("/section/name")
    public String updateName(
            Authentication authentication,
            @RequestParam String displayName) {

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException("Пользователь не найден"));

        displayName = displayName.trim();

        if (!displayName.isEmpty()) {
            user.setDisplayName(displayName);
            userRepository.save(user);
        }

        return "redirect:/section";
    }

    public static class CalendarDay {

        private final LocalDate date;
        private final int dayNumber;
        private final long completionCount;
        private final boolean today;
        private final boolean selected;

        private CalendarDay(
                LocalDate date,
                int dayNumber,
                long completionCount,
                boolean today,
                boolean selected) {
            this.date = date;
            this.dayNumber = dayNumber;
            this.completionCount = completionCount;
            this.today = today;
            this.selected = selected;
        }

        public static CalendarDay empty() {
            return new CalendarDay(null, 0, 0, false, false);
        }

        public LocalDate getDate() {
            return date;
        }

        public int getDayNumber() {
            return dayNumber;
        }

        public long getCompletionCount() {
            return completionCount;
        }

        public boolean isEmpty() {
            return date == null;
        }

        public boolean isToday() {
            return today;
        }

        public boolean isSelected() {
            return selected;
        }

        public boolean hasCompletions() {
            return completionCount > 0;
        }
    }
}