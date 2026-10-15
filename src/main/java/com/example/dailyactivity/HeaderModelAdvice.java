package com.example.dailyactivity;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDate;

/**
 * Добавляет в модель атрибуты для шапки сайта:
 *  - monthLabel — название месяца для иконки-календарика
 *  - dayNumber  — текущее число месяца
 *
 * Работает для всех контроллеров автоматически.
 */
@ControllerAdvice
public class HeaderModelAdvice {

    @ModelAttribute("monthLabel")
    public String monthLabel() {

        int month = LocalDate.now().getMonthValue();

        return switch (month) {
            case 3  -> "Март";
            case 7  -> "Июль";
            case 10 -> "Окт";
            case 12 -> "Дек";
            default -> "";
        };
    }

    @ModelAttribute("dayNumber")
    public int dayNumber() {
        return LocalDate.now().getDayOfMonth();
    }
}