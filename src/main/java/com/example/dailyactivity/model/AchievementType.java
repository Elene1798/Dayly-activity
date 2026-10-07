package com.example.dailyactivity.model;

public enum AchievementType {

    // =========================
    // ЕЖЕДНЕВНЫЕ ПОСЕЩЕНИЯ
    // =========================

    VISIT_5(
            "visit_5",
            "Рядовой",
            "Посещай сайт 5 дней подряд",
            "🪖",
            true
    ),

    VISIT_30(
            "visit_30",
            "Сержант",
            "Посещай сайт 30 дней подряд",
            "🎖️",
            true
    ),

    VISIT_100(
            "visit_100",
            "Прапорщик",
            "Посещай сайт 100 дней подряд",
            "🎖️",
            true
    ),

    VISIT_150(
            "visit_150",
            "Лейтенант",
            "Посещай сайт 150 дней подряд",
            "⭐",
            true
    ),

    VISIT_200(
            "visit_200",
            "Капитан",
            "Посещай сайт 200 дней подряд",
            "⭐",
            true
    ),

    VISIT_250(
            "visit_250",
            "Майор",
            "Посещай сайт 250 дней подряд",
            "⭐",
            true
    ),

    VISIT_365(
            "visit_365",
            "Подполковник",
            "Посещай сайт 365 дней подряд",
            "🏅",
            true
    ),

    VISIT_730(
            "visit_730",
            "Полковник",
            "Посещай сайт 2 года подряд",
            "🏅",
            true
    ),

    VISIT_1095(
            "visit_1095",
            "Генерал-майор",
            "Посещай сайт 3 года подряд",
            "🏅",
            true
    ),

    VISIT_1460(
            "visit_1460",
            "Генерал-лейтенант",
            "Посещай сайт 4 года подряд",
            "🏅",
            true
    ),

    VISIT_1825(
            "visit_1825",
            "Генерал-полковник",
            "Посещай сайт 5 лет подряд",
            "🏅",
            true
    ),

    VISIT_2190(
            "visit_2190",
            "Генерал армии",
            "Посещай сайт 6 лет подряд",
            "🏅",
            true
    ),

    VISIT_2555(
            "visit_2555",
            "Маршал",
            "Посещай сайт 7 лет подряд",
            "🏆",
            true
    ),


    // =========================
    // ВЫПОЛНЕННЫЕ ЗАНЯТИЯ
    // =========================

    COMPLETED_10(
            "completed_10",
            "Первый шаг",
            "Выполни 10 занятий",
            "🥉",
            true
    ),

    COMPLETED_25(
            "completed_25",
            "В ритме",
            "Выполни 25 занятий",
            "🥉",
            true
    ),

    COMPLETED_50(
            "completed_50",
            "Уверенный темп",
            "Выполни 50 занятий",
            "🥈",
            true
    ),

    COMPLETED_100(
            "completed_100",
            "Сотня",
            "Выполни 100 занятий",
            "🥈",
            true
    ),

    COMPLETED_250(
            "completed_250",
            "Мастер привычки",
            "Выполни 250 занятий",
            "🥇",
            true
    ),

    COMPLETED_500(
            "completed_500",
            "Покоритель",
            "Выполни 500 занятий",
            "🥇",
            true
    ),

    COMPLETED_1000(
            "completed_1000",
            "Тысяча",
            "Выполни 1000 занятий",
            "🏆",
            true
    ),

    COMPLETED_2500(
            "completed_2500",
            "Легендарный темп",
            "Выполни 2500 занятий",
            "🏆",
            true
    ),

    COMPLETED_5000(
            "completed_5000",
            "Неудержимый",
            "Выполни 5000 занятий",
            "💎",
            true
    ),

    COMPLETED_10000(
            "completed_10000",
            "Легенда Daily Activity",
            "Выполни 10000 занятий",
            "💎",
            true
    ),


    // =========================
    // ОСОБЫЕ ДОСТИЖЕНИЯ
    // =========================

    NIGHT_OWL(
            "night_owl",
            "Сова",
            "Выполни 5 занятий после 22:00",
            "🌙",
            true
    ),

    EARLY_START(
            "early_start",
            "Ранний старт",
            "Выполни занятие до 08:00",
            "🌅",
            true
    ),

    ADVENTURER(
            "adventurer",
            "Авантюрист",
            "Выполни 20 случайных занятий",
            "🎲",
            true
    ),

    EXPLORER(
            "explorer",
            "Исследователь",
            "Выполни занятия во всех доступных местах",
            "🧭",
            true
    ),

    FRESH_AIR(
            "fresh_air",
            "На свежем воздухе",
            "Выполни 10 занятий на улице",
            "🌳",
            true
    ),

    CREATIVE_NATURE(
            "creative_nature",
            "Творческая натура",
            "Выполни 10 творческих занятий",
            "🎨",
            false
    ),

    CURIOUS(
            "curious",
            "Любознательный",
            "Выполни 10 обучающих занятий",
            "📚",
            false
    ),

    ACTIVIST(
            "activist",
            "Активист",
            "Выполни 25 активных занятий",
            "🏃",
            false
    ),

    HUNDRED_HOURS(
            "hundred_hours",
            "100 часов",
            "Выполни занятия общей продолжительностью 100 часов",
            "💯",
            false
    );


    private final String key;
    private final String title;
    private final String description;
    private final String icon;
    private final boolean visible;


    AchievementType(
            String key,
            String title,
            String description,
            String icon,
            boolean visible) {

        this.key = key;
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.visible = visible;
    }


    public String getKey() {
        return key;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }

    public boolean isVisible() {
        return visible;
    }
}