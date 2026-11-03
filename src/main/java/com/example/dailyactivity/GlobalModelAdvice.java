package com.example.dailyactivity;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

@ControllerAdvice
public class GlobalModelAdvice {

    @ModelAttribute
    public void addHeaderCalendar(Model model) {
        LocalDate today = LocalDate.now();

        String monthLabel = today.getMonth()
                .getDisplayName(TextStyle.SHORT, new Locale("ru"))
                .replace(".", "")
                .substring(0, 3)
                .toUpperCase(new Locale("ru"));

        model.addAttribute("monthLabel", monthLabel);
        model.addAttribute("dayNumber", today.getDayOfMonth());
    }
}