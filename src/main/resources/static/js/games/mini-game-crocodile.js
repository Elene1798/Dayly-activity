function initializeCrocodileGame(gameElement) {

    const activityId = gameElement.dataset.activityId;

    const setup = gameElement.querySelector(".crocodile-setup");
    const playersSetup = gameElement.querySelector(".crocodile-players-setup");
    const play = gameElement.querySelector(".crocodile-play");
    const results = gameElement.querySelector(".crocodile-results");

    const playerCountSelect =
        gameElement.querySelector(".crocodile-player-count");

    const difficultySelect =
        gameElement.querySelector(".crocodile-difficulty");

    const nextButton =
        gameElement.querySelector(".crocodile-next-button");

    const playerInputs =
        gameElement.querySelector(".crocodile-player-inputs");

    const startButton =
        gameElement.querySelector(".crocodile-start-button");

    const roundTypeElement =
        gameElement.querySelector(".crocodile-round-type");

    const playerNameElement =
        gameElement.querySelector(".crocodile-player-name");

    const timerElement =
        gameElement.querySelector(".crocodile-timer");

    const taskHiddenElement =
        gameElement.querySelector(".crocodile-task-hidden");

    const showTaskButton =
        gameElement.querySelector(".crocodile-show-task-button");

    const taskElement =
        gameElement.querySelector(".crocodile-task");

    const actionsElement =
        gameElement.querySelector(".crocodile-game-actions");

    const guessedButton =
        gameElement.querySelector(".crocodile-guessed-button");

    const skipButton =
        gameElement.querySelector(".crocodile-skip-button");

    const scoreboard =
        gameElement.querySelector(".crocodile-scoreboard");

    const restartButton =
        gameElement.querySelector(".crocodile-restart-button");

    const saveMessage =
        gameElement.querySelector(".crocodile-save-message");


    const ROUND_TYPES = [
        {
            id: "GESTURES",
            title: "🤸 Жесты",
            time: 60
        },
        {
            id: "MIMIC",
            title: "😐 Словами",
            time: 60
        },
        {
            id: "SLOW",
            title: "🐢 Медленно",
            time: 60
        },
        {
            id: "FAST",
            title: "⚡ 15 секунд",
            time: 15
        },
        {
            id: "SITUATION",
            title: "🎭 Ситуация",
            time: 60
        }
    ];


    let players = [];
    let rounds = [];

    let difficulty = "MEDIUM";

    let currentRoundIndex = 0;
    let currentPlayerIndex = 0;

    let currentTask = null;

    let timerInterval = null;
    let remainingSeconds = 60;


    /*
     * Перемешивание массива.
     */
    function shuffle(array) {
        const result = [...array];

        for (let i = result.length - 1; i > 0; i--) {
            const j = Math.floor(Math.random() * (i + 1));

            [result[i], result[j]] =
                [result[j], result[i]];
        }

        return result;
    }


    /*
     * Показать первый экран.
     */
    function showSetup() {

        setup.style.display = "block";
        playersSetup.style.display = "none";
        play.style.display = "none";
        results.style.display = "none";

        saveMessage.textContent = "";

        currentRoundIndex = 0;
        currentPlayerIndex = 0;
        currentTask = null;

        stopTimer();
    }


    /*
     * Шаг 1 → ввод имён.
     */
    nextButton.addEventListener("click", () => {

        const count =
            Number(playerCountSelect.value);

        playerInputs.innerHTML = "";

        for (let i = 0; i < count; i++) {

            const wrapper =
                document.createElement("div");

            wrapper.className =
                "crocodile-player-input";

            const label =
                document.createElement("label");

            label.textContent =
                `Игрок ${i + 1}`;

            const input =
                document.createElement("input");

            input.type = "text";
            input.className =
                "crocodile-player-name-input";

            input.placeholder =
                `Имя игрока ${i + 1}`;

            input.maxLength = 50;

            input.autocomplete = "off";

            wrapper.appendChild(label);
            wrapper.appendChild(input);

            playerInputs.appendChild(wrapper);
        }

        setup.style.display = "none";
        playersSetup.style.display = "block";
    });


    /*
     * Шаг 2 → начало игры.
     */
    startButton.addEventListener("click", () => {

        const inputs =
            playerInputs.querySelectorAll(
                ".crocodile-player-name-input"
            );

        players = [];

        inputs.forEach((input, index) => {

            let name =
                input.value.trim();

            if (!name) {
                name = `Игрок ${index + 1}`;
            }

            players.push({
                name: name,
                score: 0,
                usedTaskIds: new Set()
            });
        });

        if (players.length < 2) {
            return;
        }

        difficulty =
            difficultySelect.value;

        rounds = shuffle(ROUND_TYPES);

        currentRoundIndex = 0;
        currentPlayerIndex = 0;

        startRound();
    });


    /*
     * Начало хода.
     */
    function startRound() {

        setup.style.display = "none";
        playersSetup.style.display = "none";
        play.style.display = "block";
        results.style.display = "none";

        const round =
            rounds[currentRoundIndex];

        const player =
            players[currentPlayerIndex];

        roundTypeElement.textContent =
            round.title;

        playerNameElement.textContent =
            `Ход игрока: ${player.name}`;

        remainingSeconds =
            round.time;

        updateTimer();

        taskHiddenElement.style.display = "block";
        showTaskButton.style.display = "inline-block";

        taskElement.style.display = "none";
        taskElement.textContent = "";

        actionsElement.style.display = "none";

        currentTask = null;

        stopTimer();
    }


    /*
     * Игрок нажал «Показать задание».
     */
    showTaskButton.addEventListener("click", async () => {

        console.log("КРОКОДИЛ: нажата кнопка «Показать задание»");

        showTaskButton.disabled = true;
        showTaskButton.textContent = "Загрузка...";

        try {

            console.log("КРОКОДИЛ: вызываем getNextTask()");

            const task = await getNextTask();

            console.log(
                "КРОКОДИЛ: getNextTask() вернул:",
                task
            );

            if (!task) {

                console.log(
                    "КРОКОДИЛ: подходящих заданий больше нет"
                );

                taskHiddenElement.textContent =
                    "Нет доступных заданий для выбранной сложности.";

                taskHiddenElement.style.display = "block";

                showTaskButton.style.display = "none";

                return;
            }

            currentTask = task;

            console.log(
                "КРОКОДИЛ: показываем задание:",
                task.content
            );

            taskHiddenElement.style.display = "none";

            taskElement.textContent = task.content;
            taskElement.classList.remove(
                "crocodile-task-short",
                "crocodile-task-medium",
                "crocodile-task-long"
            );

            const taskLength = task.content.length;

            if (taskLength <= 15) {
                taskElement.classList.add("crocodile-task-short");
            } else if (taskLength <= 40) {
                taskElement.classList.add("crocodile-task-medium");
            } else {
                taskElement.classList.add("crocodile-task-long");
            }

            taskElement.style.display = "flex";

            actionsElement.style.display = "flex";

            showTaskButton.style.display = "none";

            startTimer();

        } catch (error) {

            console.error(
                "КРОКОДИЛ: ошибка при получении задания:",
                error
            );

            taskHiddenElement.textContent =
                "Не удалось загрузить задание. Попробуй ещё раз.";

            taskHiddenElement.style.display = "block";

            showTaskButton.style.display = "inline-block";

        } finally {

            showTaskButton.disabled = false;
            showTaskButton.textContent =
                "👀 Показать задание";
        }
    });


    /*
     * Получить задание.
     *
     * Задание не должно повторяться
     * у одного и того же игрока.
     */
    async function getNextTask() {

        const miniGameId = getMiniGameId();

        if (!miniGameId) {
            throw new Error("Не удалось определить ID мини-игры");
        }

        const roundType =
            rounds[currentRoundIndex].id;

        const url =
            `/mini-game/${miniGameId}/crocodile-tasks`
            + `?activityId=${encodeURIComponent(activityId)}`
            + `&difficulty=${encodeURIComponent(difficulty)}`
            + `&roundType=${encodeURIComponent(roundType)}`;

        console.log("КРОКОДИЛ: отправляем запрос:", url);

        const controller = new AbortController();

        const timeoutId =
            setTimeout(() => {
                controller.abort();
            }, 5000);

        let response;

        try {

            response = await fetch(url, {
                method: "GET",
                signal: controller.signal
            });

        } catch (error) {

            if (error.name === "AbortError") {
                throw new Error(
                    "Сервер не ответил за 5 секунд"
                );
            }

            throw error;

        } finally {

            clearTimeout(timeoutId);
        }

        console.log(
            "КРОКОДИЛ: сервер ответил:",
            response.status
        );

        if (!response.ok) {
            throw new Error(
                `Ошибка сервера: ${response.status}`
            );
        }

        const tasks = await response.json();

        console.log(
            "КРОКОДИЛ: JSON успешно разобран:",
            tasks
        );

        if (!Array.isArray(tasks)) {
            throw new Error("Сервер вернул не массив заданий");
        }

        const player =
            players[currentPlayerIndex];

        const availableTasks =
            tasks.filter(task =>
                !player.usedTaskIds.has(task.id)
            );

        console.log(
            "КРОКОДИЛ: доступных заданий:",
            availableTasks.length
        );

        if (availableTasks.length === 0) {
            return null;
        }

        const task =
            availableTasks[
                Math.floor(
                    Math.random() * availableTasks.length
                )
                ];

        player.usedTaskIds.add(task.id);

        return task;
    }


    /*
     * Получаем ID мини-игры.
     */
    function getMiniGameId() {

        const activity =
            gameElement.closest(".activity-mini-game");

        if (!activity) {
            return null;
        }

        return activity.dataset.miniGameId;
    }


    /*
     * Таймер.
     */
    function startTimer() {

        stopTimer();

        timerInterval =
            setInterval(() => {

                remainingSeconds--;

                updateTimer();

                if (remainingSeconds <= 0) {

                    stopTimer();

                    nextTurn();
                }

            }, 1000);
    }


    function stopTimer() {

        if (timerInterval !== null) {

            clearInterval(timerInterval);

            timerInterval = null;
        }
    }


    function updateTimer() {

        const minutes =
            Math.floor(remainingSeconds / 60);

        const seconds =
            remainingSeconds % 60;

        timerElement.textContent =
            String(minutes).padStart(2, "0")
            + ":"
            + String(seconds).padStart(2, "0");
    }


    /*
     * «Угадано».
     */
    guessedButton.addEventListener("click", () => {

        stopTimer();

        players[currentPlayerIndex].score++;

        nextTurn();
    });


    /*
     * «Пропустить».
     */
    skipButton.addEventListener("click", () => {

        stopTimer();

        nextTurn();
    });


    /*
     * Переход к следующему игроку.
     */
    function nextTurn() {

        currentPlayerIndex++;

        /*
         * Все игроки закончили текущий раунд.
         */
        if (currentPlayerIndex >= players.length) {

            currentPlayerIndex = 0;

            currentRoundIndex++;

            /*
             * Все пять раундов закончены.
             */
            if (currentRoundIndex >= rounds.length) {

                finishGame();

                return;
            }
        }

        startRound();
    }


    /*
     * Финал игры.
     */
    async function finishGame() {

        stopTimer();

        play.style.display = "none";
        results.style.display = "block";

        launchConfetti();

        /*
         * Сортируем игроков по количеству очков.
         */
        const sortedPlayers =
            [...players].sort(
                (a, b) => b.score - a.score
            );

        const winner =
            sortedPlayers[0];

        /*
         * Очищаем предыдущий результат.
         */
        scoreboard.innerHTML = "";

        /*
         * Победитель.
         */
        const winnerBlock =
            document.createElement("div");

        winnerBlock.className =
            "crocodile-winner";

        winnerBlock.innerHTML = `
        <div class="crocodile-winner-title">
            🏆 Победитель
        </div>

        <div class="crocodile-winner-name">
            ${escapeHtml(winner.name)}
        </div>

        <div class="crocodile-winner-score">
            ${winner.score} ${getPointsWord(winner.score)}
        </div>
    `;

        scoreboard.appendChild(winnerBlock);


        /*
         * Заголовок таблицы результатов.
         */
        const scoreboardTitle =
            document.createElement("div");

        scoreboardTitle.className =
            "crocodile-scoreboard-title";

        scoreboardTitle.textContent =
            "Результаты игры";

        scoreboard.appendChild(scoreboardTitle);


        /*
         * Результаты всех игроков.
         */
        sortedPlayers.forEach((player, index) => {

            const row =
                document.createElement("div");

            row.className =
                "crocodile-score-row";

            const place =
                document.createElement("span");

            place.className =
                "crocodile-score-place";

            place.textContent =
                `${index + 1}.`;

            const name =
                document.createElement("span");

            name.className =
                "crocodile-score-name";

            name.textContent =
                player.name;

            const score =
                document.createElement("span");

            score.className =
                "crocodile-score-value";

            score.textContent =
                `${player.score} ${getPointsWord(player.score)}`;

            row.appendChild(place);
            row.appendChild(name);
            row.appendChild(score);

            scoreboard.appendChild(row);
        });


        /*
         * Сохраняем прохождение.
         */
        await saveCompletion();
    }

    function getPointsWord(number) {

        const lastTwo =
            number % 100;

        const last =
            number % 10;

        if (lastTwo >= 11 && lastTwo <= 14) {
            return "очков";
        }

        if (last === 1) {
            return "очко";
        }

        if (last >= 2 && last <= 4) {
            return "очка";
        }

        return "очков";
    }


    function escapeHtml(value) {

        const div =
            document.createElement("div");

        div.textContent = value;

        return div.innerHTML;
    }


    /*
     * Сохраняем факт прохождения игры.
     *
     * Сама игра при этом остаётся доступной.
     */
    async function saveCompletion() {

        const csrfInput =
            gameElement.querySelector(
                ".crocodile-csrf-token"
            );

        const formData =
            new FormData();

        formData.append(
            "activityId",
            activityId
        );

        if (csrfInput) {

            formData.append(
                csrfInput.name,
                csrfInput.value
            );
        }

        try {

            const response =
                await fetch(
                    "/mini-game/complete-crocodile-inline",
                    {
                        method: "POST",
                        body: formData
                    }
                );

            const result =
                await response.text();

            if (result === "SUCCESS") {

                saveMessage.textContent =
                    "🎉 Игра завершена! ";
            }

            else if (result === "AUTH_REQUIRED") {

                saveMessage.textContent =
                    "🔐 Войди в аккаунт, чтобы сохранить прохождение";
            }

            else {

                saveMessage.textContent =
                    "Результат игры показан, но сохранить его не удалось";
            }

        } catch (error) {

            console.error(error);

            saveMessage.textContent =
                "Результат игры показан, но сохранить его не удалось.";
        }
    }

    function launchConfetti() {

        const colors = [
            "#667eea",
            "#764ba2",
            "#f4d35e",
            "#43d17b",
            "#ff6b9d",
            "#4ecdc4"
        ];

        const count = 90;

        for (let i = 0; i < count; i++) {

            const piece =
                document.createElement("div");

            piece.className =
                "crocodile-confetti";

            piece.style.left =
                Math.random() * 100 + "vw";

            piece.style.backgroundColor =
                colors[
                    Math.floor(
                        Math.random() * colors.length
                    )
                    ];

            piece.style.animationDelay =
                Math.random() * 0.8 + "s";

            piece.style.animationDuration =
                (2.5 + Math.random() * 2) + "s";

            piece.style.transform =
                `rotate(${Math.random() * 360}deg)`;

            document.body.appendChild(piece);

            setTimeout(() => {
                piece.remove();
            }, 5000);
        }
    }

    /*
     * Играть заново.
     */
    restartButton.addEventListener("click", () => {

        players = [];
        rounds = [];

        currentRoundIndex = 0;
        currentPlayerIndex = 0;

        currentTask = null;

        playerInputs.innerHTML = "";

        showSetup();
    });


    /*
     * Первый экран.
     */
    showSetup();
}