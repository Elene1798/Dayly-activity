package com.example.dailyactivity;

import com.example.dailyactivity.model.Activity;
import com.example.dailyactivity.repository.ActivityRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ActivityRepository activityRepository;

    public DataInitializer(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Override
    public void run(String... args) {

        if (activityRepository.count() > 0) {
            return;
        }

        activityRepository.save(new Activity(
                "Прогуляйся 30 минут",
                "Выйди на улицу и просто погуляй без конкретной цели",
                "active",
                30,
                "outside"
        ));

        activityRepository.save(new Activity(
                "Сделай короткую тренировку",
                "Попробуй выполнить несколько простых упражнений дома",
                "active",
                20,
                "home"
        ));

        activityRepository.save(new Activity(
                "Покатайся на велосипеде",
                "Выбери новый маршрут и отправляйся на небольшую велосипедную прогулку",
                "active",
                60,
                "outside"
        ));

        activityRepository.save(new Activity(
                "Потанцуй под любимую музыку",
                "Включи несколько любимых песен и просто двигайся в своё удовольствие",
                "active",
                15,
                "home"
        ));

        activityRepository.save(new Activity(
                "Нарисуй картину",
                "Возьми бумагу и любые краски и нарисуй то, что видишь вокруг себя",
                "creative",
                60,
                "anywhere"
        ));

        activityRepository.save(new Activity(
                "Сделай необычную фотографию",
                "Попробуй найти вокруг себя интересный объект и сделать красивую фотографию",
                "creative",
                30,
                "anywhere"
        ));

        activityRepository.save(new Activity(
                "Напиши короткий рассказ",
                "Придумай историю длиной примерно в одну страницу",
                "creative",
                40,
                "anywhere"
        ));

        activityRepository.save(new Activity(
                "Создай коллаж",
                "Собери несколько фотографий или изображений в один красивый коллаж",
                "creative",
                45,
                "home"
        ));

        activityRepository.save(new Activity(
                "Изучи новую тему",
                "Выбери тему, которая тебе интересна, и изучай её в течение 30 минут",
                "learning",
                30,
                "anywhere"
        ));

        activityRepository.save(new Activity(
                "Выучи 10 новых слов",
                "Выбери иностранный язык и выучи десять новых слов",
                "learning",
                20,
                "anywhere"
        ));

        activityRepository.save(new Activity(
                "Посмотри образовательное видео",
                "Найди качественное видео на тему, о которой тебе хочется узнать больше",
                "learning",
                30,
                "anywhere"
        ));

        activityRepository.save(new Activity(
                "Прочитай несколько страниц книги",
                "Возьми книгу, которую давно откладывал, и прочитай хотя бы 20 страниц",
                "learning",
                30,
                "home"
        ));

        activityRepository.save(new Activity(
                "Посмотри хороший фильм",
                "Выбери фильм, который давно хотел посмотреть",
                "entertainment",
                120,
                "home"
        ));

        activityRepository.save(new Activity(
                "Поиграй в игру",
                "Выбери любимую игру и проведи за ней немного времени",
                "entertainment",
                60,
                "friends"
        ));

        activityRepository.save(new Activity(
                "Послушай новый альбом",
                "Найди музыкального исполнителя, которого раньше не слушал",
                "entertainment",
                45,
                "home"
        ));

        activityRepository.save(new Activity(
                "Позвони другу",
                "Позвони человеку, с которым давно не разговаривал",
                "entertainment",
                30,
                "home"
        ));

        System.out.println("Занятия успешно добавлены в базу данных!");
    }
}
