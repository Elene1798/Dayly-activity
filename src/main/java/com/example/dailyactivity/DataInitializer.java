package com.example.dailyactivity;

import com.example.dailyactivity.model.Activity;
import com.example.dailyactivity.repository.ActivityRepository;
import com.example.dailyactivity.repository.FavoriteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ActivityRepository activityRepository;
    private final FavoriteRepository favoriteRepository;

    public DataInitializer(ActivityRepository activityRepository, FavoriteRepository favoriteRepository) {
        this.activityRepository = activityRepository;
        this.favoriteRepository = favoriteRepository;
    }

    @Override
    public void run(String... args) {

        saveIfMissing(
                "Прогуляйся 30 минут",
                "Выйди на улицу и просто погуляй без конкретной цели",
                "active", 30, "outside",
                null, null, null, null, null, null
        );

        saveIfMissing(
                "Сделай короткую тренировку",
                "Попробуй выполнить несколько простых упражнений дома",
                "active", 20, "anywhere",
                null, null, null, null, null, null
        );

        saveIfMissing(
                "Покатайся на велосипеде",
                "Выбери новый маршрут и отправляйся на небольшую велосипедную прогулку",
                "active", 60, "outside",
                null, null, null, null, null, null
        );

        saveIfMissing(
                "Потанцуй под любимую музыку",
                "Включи несколько любимых песен и просто двигайся в своё удовольствие",
                "active", 15, "anywhere",
                null, null, null, null, null, null
        );

        saveIfMissing(
                "Нарисуй картину",
                "Возьми бумагу и любые краски и нарисуй то, что видишь вокруг себя",
                "creative", 60, "home",
                null, null, null, null, null, null
        );

        saveIfMissing(
                "Сделай необычную фотографию",
                "Попробуй найти вокруг себя интересный объект и сделать красивую фотографию",
                "creative", 30, "anywhere",
                null, null, null, null, null, null
        );

        saveIfMissing(
                "Напиши короткий рассказ",
                "Придумай историю длиной примерно в одну страницу",
                "creative", 40, "home",
                null, null, null, null, null, null
        );

        saveIfMissing(
                "Создай коллаж",
                "Собери несколько фотографий или изображений в один красивый коллаж",
                "creative", 45, "home",
                null, null, null, null, null, null
        );

        saveIfMissing(
                "Изучи новую тему",
                "Выбери тему, которая тебе интересна, и изучай её в течение 30 минут",
                "learning", 30, "home",
                null, null, null, null, null, null
        );

        saveIfMissing(
                "Выучи 10 новых слов",
                "Выбери иностранный язык и выучи десять новых слов",
                "learning", 20, "home",
                null, null, null, null, null, null
        );

        saveIfMissing(
                "Посмотри образовательное видео",
                "Найди качественное видео на тему, о которой тебе хочется узнать больше",
                "learning", 30, "anywhere",
                null, null, null, null, null, null
        );

        saveIfMissing(
                "Прочитай несколько страниц книги",
                "Возьми книгу, которую давно откладывал, и прочитай хотя бы 20 страниц",
                "learning", 30, "anywhere",
                null, null, null, null, null, null
        );

        saveIfMissing(
                "Посмотри хороший фильм",
                "Выбери фильм, который давно хотел посмотреть",
                "entertainment", 120, "anywhere",
                null, null, null, null, null, null
        );

        saveIfMissing(
                "Поиграй в игру",
                "Выбери любимую игру и проведи за ней немного времени",
                "entertainment", 60, "friends",
                null, null, null, null, null, null
        );

        saveIfMissing(
                "Послушай новый альбом",
                "Найди музыкального исполнителя, которого раньше не слушал",
                "entertainment", 45, "anywhere",
                null, null, null, null, null, null
        );

        saveIfMissing(
                "Позвони другу",
                "Позвони человеку, с которым давно не разговаривал",
                "entertainment", 30, "friends",
                null, null, null, null, null, null
        );

        saveIfMissing(
                "Расслабься в ванной",
                "Налей себе горячую ванну, зажги свечи, включи расслабляющую музыку или фильм",
                "active", 30, "anywhere",
                null, null, null, null, null, null
        );

        saveIfMissing(
                "Вытащи мясо из морозилки",
                "Вытащи мясо и приготовь его в духовке",
                "active", 60, "home",
                null, null, null, null, null, null
        );

        saveIfMissing(
                "Эффект Доплера",
                "Узнай, как работает эффект Доплера",
                "learning", 20, "anywhere",
                """
                1. Перейди по ссылке на видео
                2. Прочитай об эффекте Доплера
                3. Попробуй объяснить, что это такое
                """,
                "Научная информация делает тебя умнее. (ну и, конечно, помогает выпендриться в диалогах с друзьями)",
                "Эффект Доплера широко используется в медицине, а именно, с помощью него делают УЗИ сосудов (оценивают кровоток по отражению ультразвука)",
                null,
                "https://samesound.ru/prod/88430-effekt-doplera-prostymi-slovami-gid-po-effektam",
                null
        );

        saveIfMissing(
                "Прогулка с поиском желтого цвета",
                "Обычная прогулка превращается в небольшую игру на внимательность",
                "active", 15, "outside",
                "Выйди на улицу и найди вокруг себя пять предметов желтого цвета",
                "Помогает переключиться, немного подвигаться и обратить внимание на окружающую обстановку",
                "Наш мозг постоянно фильтрует огромное количество информации вокруг нас, поэтому во время привычной прогулки мы замечаем далеко не всё",
                null, null, null
        );

        saveIfMissing(
                "Зарядка",
                "Ты можешь сделать свою зарядку, а можешь воспользоваться ссылкой",
                "active", 15, "anywhere",
                """
                1. Наклоны головы 30 секунд
                2. Вращение плечами 30 секунд
                3. Наклоны в сторону 30 секунд
                4. Вращение тазом 30 секунд
                5. Приседания 30 секунд
                6. Выпады 10 раз на одну ногу
                7. Выпады 10 раз на другую ногу
                8. Планка 1 минута
                9. Восстановление дыхания
                """,
                "Короткая физическая активность помогает размяться после долгого сидения или зарядиться настроением и силами утром",
                "Даже небольшие периоды движения в течение дня лучше, чем полностью сидячий день",
                null, null,
                "https://yandex.ru/video/preview/7874478355858460491"
        );

        saveIfMissing(
                "Настало время порисовать!",
                "Нарисуй предмет, не отрывая руки",
                "creative", 10, "home",
                "Положи перед собой любой предмет. Попробуй нарисовать его одной непрерывной линией, не отрывая ручку или карандаш от бумаги",
                "Это простое упражнение помогает избавиться от стремления сделать рисунок идеальным",
                null,
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT6acUTE-Sd8W7L13bStYuZEaOK2T6vlcZZmRl7aB9v3oPwary5E32gm6w&s=10",
                null, null
        );

        saveIfMissing(
                "Открыто4ка",
                "Попробуй повторить открытку из видео",
                "creative", 20, "home",
                null, null, null, null, null,
                "https://rutube.ru/video/6f18f9ee0af7e0861efe10b42bb36c5b/?playlist=55469"
        );

        saveIfMissing(
                "ДИХОТОМИЯ",
                "Разделение чего-то на две взаимоисключающие или противоположные части",
                "learning", 5, "anywhere",
                "Выучи новое слово",
                null,
                "Слово происходит от греческого dicha («надвое») и tome («сечение, деление»)",
                null, null, null
        );

        saveIfMissing(
                "Давай сыграем в игру???",
                "Любишь судоку? Полюбишь и мурдоку!",
                "entertainment", 60, "anywhere",
                """
                1. Перейди по ссылке на игру
                2. Включи свои дедуктивные навыки
                3. Наслаждайся
                """,
                null, null, null,
                "https://murdoku.com/?lang=en",
                null
        );

        saveIfMissing(
                "Послушайте песню",
                "Я чувствую, что вам не хватает музыки, ну и, конечно, Сергея Лазарева",
                "entertainment", 5, "friends",
                "Переходи по ссылке на клип",
                null, null, null, null,
                "https://vkvideo.ru/video381875183_456240129"
        );

        saveIfMissing(
                "ПЮПИТР",
                "Наклонная подставка или столик для удобного размещения нот, книг или текстов",
                "learning", 5, "anywhere",
                null, null,
                "В XIX веке, в эпоху механических изобретений, мастера создавали «умные» пюпитры. Существовали модели с ножными педалями, которые позволяли музыканту перелистывать страницы нот ногами, не отрывая рук от инструмента",
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRNLQ4X-cavQym4ufl0f5HlzPG3xgAHXc7UKD5n7tuGBg&s=10",
                null, null
        );

        saveIfMissing(
                "Посмотри фильм \"Интерстеллар\"",
                "Когда засуха, пыльные бури и вымирание растений приводят человечество к продовольственному кризису, коллектив исследователей и учёных отправляется сквозь червоточину (которая предположительно соединяет области пространства-времени через большое расстояние) в путешествие, чтобы превзойти прежние ограничения для космических путешествий человека и найти планету с подходящими для человечества условиями",
                "entertainment", 120, "home",
                null, null,
                "Сценарий основан на научных работах физика-теоретика Кипа Торна",
                null,
                "https://www.kinopoisk.ru/film/258687/?socialAlias=MjEzMzcyMTM%3D",
                null
        );

        migrateOldFavorites();
    }

    private void migrateOldFavorites() {
        favoriteRepository.findAll().forEach(favorite -> {
            if (!favorite.hasSnapshot() && favorite.getActivity() != null) {
                favorite.copyFromActivity(favorite.getActivity());
                favoriteRepository.save(favorite);
            }
        });
    }

    private void saveIfMissing(
            String title,
            String description,
            String category,
            int duration,
            String location,
            String instructions,
            String benefit,
            String interestingFact,
            String imageUrl,
            String linkUrl,
            String videoUrl) {

        String sourceKey = "builtin:" + title;

        Activity activity = activityRepository.findBySourceKey(sourceKey).orElse(null);

        // Одноразово подхватываем уже существующие 28 системных занятий
        // по названию и присваиваем им стабильный sourceKey.
        if (activity == null) {
            activity = activityRepository.findByTitle(title).orElse(null);
        }

        if (activity == null) {
            activity = new Activity();
        }

        activity.setSourceKey(sourceKey);
        activity.setTitle(title);
        activity.setDescription(description);
        activity.setCategory(category);
        activity.setDuration(duration);
        activity.setLocation(location);
        activity.setInstructions(instructions);
        activity.setBenefit(benefit);
        activity.setInterestingFact(interestingFact);
        activity.setImageUrl(imageUrl);
        activity.setLinkUrl(linkUrl);
        activity.setVideoUrl(videoUrl);

        activityRepository.save(activity);
    }

}