document.addEventListener("DOMContentLoaded", () => {

    const gameCards =
        document.querySelectorAll(".activity-mini-game");


    gameCards.forEach(card => {

        const button =
            card.querySelector(".activity-mini-game-button");

        const content =
            card.querySelector(".activity-mini-game-content");

        const gameId =
            card.dataset.miniGameId;

        const activityId =
            card.dataset.activityId;


        if (!button || !content || !gameId || !activityId) {
            return;
        }


        button.addEventListener("click", async () => {

            button.disabled = true;
            button.textContent = "Загрузка...";


            try {

                const response = await fetch(
                    `/mini-game/${gameId}/content?activityId=${activityId}`
                );


                if (!response.ok) {
                    throw new Error("Не удалось загрузить игру");
                }


                const html =
                    await response.text();


                /*
                 * Вставляем игру внутрь
                 * существующей карточки активности.
                 */

                content.innerHTML = html;


                /*
                 * =========================
                 * WORD
                 * =========================
                 */

                const wordGame =
                    content.querySelector("[data-word-game]");
                if (wordGame) {
                    initializeWordGame(content);
                }


                /*
                 * =========================
                 * MEMORY
                 * =========================
                 */

                const memoryGame =
                    content.querySelector("[data-memory-game]");
                if (memoryGame && typeof initializeMemoryGame === 'function') {
                    initializeMemoryGame(memoryGame);
                }

                const crocodileGame = content.querySelector("[data-crocodile-game]");
                if (crocodileGame) initializeCrocodileGame(crocodileGame);
                /*
                 * После загрузки игры
                 * кнопка больше не нужна.
                 */

                button.style.display = "none";


            } catch (error) {

                console.error(error);


                button.disabled = false;
                button.textContent = "🎮 Начать игру";


                content.innerHTML =
                    "<p>Не удалось загрузить игру. Попробуй ещё раз</p>";
            }

        });

    });


    /*
     * =====================================================
     * WORD GAME
     * =====================================================
     */


    window.initializeWordGame = function(container) {

        if (!container) {
            console.error("Не передан контейнер игры «Собери слово»");
            return;
        }

        const game = container.matches("[data-word-game]")
            ? container
            : container.querySelector("[data-word-game]");

        if (!game) {
            console.error("Не найден элемент [data-word-game]");
            return;
        }


        const lettersContainer =
            game.querySelector(".word-game-letters");

        const answerContainer =
            game.querySelector(".word-game-answer");

        const checkButton =
            game.querySelector(".word-game-check");

        const result =
            game.querySelector(".word-game-result");

        const completeForm =
            game.querySelector(".word-game-complete-form");

        const submittedAnswer =
            game.querySelector(".word-game-submitted-answer");


        const word =
            game.dataset.answer;


        let selectedLetters = [];


        const letters = shuffle(
            word.split("").map((letter, index) => ({
                letter: letter,
                index: index
            }))
        );


        function shuffle(array) {

            return [...array]
                .sort(() => Math.random() - 0.5);

        }


        /*
         * Отрисовка доступных букв.
         */

        function renderLetters() {

            lettersContainer.innerHTML = "";


            letters.forEach(item => {

                const button =
                    document.createElement("button");


                button.type = "button";

                button.className =
                    "word-game-letter";

                button.textContent =
                    item.letter;


                const isSelected =
                    selectedLetters.some(
                        selected =>
                            selected.index === item.index
                    );


                button.disabled =
                    isSelected;


                button.addEventListener("click", () => {

                    if (
                        selectedLetters.length >=
                        word.length
                    ) {
                        return;
                    }


                    if (
                        selectedLetters.some(
                            selected =>
                                selected.index === item.index
                        )
                    ) {
                        return;
                    }


                    selectedLetters.push(item);


                    renderLetters();
                    renderAnswer();

                });


                lettersContainer.appendChild(button);

            });

        }


        /*
         * Отрисовка собранного слова.
         */

        function renderAnswer() {

            answerContainer.innerHTML = "";


            selectedLetters.forEach(
                (item, position) => {

                    const button =
                        document.createElement("button");


                    button.type = "button";

                    button.className =
                        "word-game-selected-letter";

                    button.textContent =
                        item.letter;


                    button.addEventListener(
                        "click",
                        () => {

                            selectedLetters.splice(
                                position,
                                1
                            );


                            renderLetters();
                            renderAnswer();

                        }
                    );


                    answerContainer.appendChild(
                        button
                    );

                }
            );

        }

        // Хранилище пройденных taskId для гостя (в рамках страницы)
        window.__guestPassedWordTaskIds = window.__guestPassedWordTaskIds || [];

        function showNextWordButton() {
            checkButton.textContent = "Играть ещё";
            checkButton.disabled = false;

            const nextButton = checkButton.cloneNode(true);
            checkButton.replaceWith(nextButton);

            nextButton.addEventListener("click", async () => {
                const host = game.closest(".standalone-mini-game-content");
                const card = game.closest(".mini-game-card");

                if (!host || !card) return;

                const gameId = card.dataset.currentGameId
                    || card.dataset.miniGameId;

                nextButton.disabled = true;
                nextButton.textContent = "Загрузка...";

                try {
                    // Передаём список уже пройденных слов, чтобы гость не получал их снова
                    const excludeParam = window.__guestPassedWordTaskIds.length
                        ? "?excludeTaskIds=" + window.__guestPassedWordTaskIds.join(",")
                        : "";

                    const response = await fetch(
                        `/mini-game/${gameId}/content${excludeParam}`,
                        { credentials: "same-origin" }
                    );

                    if (!response.ok) {
                        throw new Error("Не удалось загрузить игру");
                    }

                    const html = await response.text();

                    // Если сервер вернул пустой фрагмент — все слова пройдены
                    if (!html.includes("data-word-game")) {
                        host.innerHTML =
                            '<p class="mini-games-completed">Все игры пройдены!</p>';
                        return;
                    }

                    host.innerHTML = html;

                    const nextGame = host.querySelector("[data-word-game]");

                    if (nextGame) {
                        window.initializeWordGame(nextGame);
                    } else {
                        host.innerHTML =
                            '<p class="mini-games-completed">Все игры пройдены!</p>';
                    }
                } catch (error) {
                    console.error(error);
                    nextButton.disabled = false;
                    nextButton.textContent = "Повторить";
                }
            });
        }

        checkButton.addEventListener(
            "click",
            () => {

                const answer =
                    selectedLetters
                        .map(item => item.letter)
                        .join("");


                if (
                    answer.toLowerCase() ===
                    word.toLowerCase()
                ) {

                    result.textContent =
                        "🎉 Правильно!";

                    result.className =
                        "word-game-result success";

                    checkButton.disabled =
                        true;


                    submittedAnswer.value =
                        answer;


                    setTimeout(
                        async () => {

                            try {

                                const formData =
                                    new FormData(
                                        completeForm
                                    );

                                const gameElement = container.matches("[data-word-game]")
                                    ? container
                                    : container.querySelector("[data-word-game]");

                                const isStandaloneWordGame =
                                    gameElement?.dataset.standalone === "true";

                                const endpoint = isStandaloneWordGame
                                    ? "/mini-game/complete-word-standalone"
                                    : "/mini-game/complete-inline";

                                console.log("Игра отдельно:", isStandaloneWordGame);
                                console.log("Адрес сохранения:", endpoint);

                                const response = await fetch(endpoint, {
                                    method: "POST",
                                    body: formData
                                });


                                const status =
                                    await response.text();

                                console.log("Статус HTTP:", response.status);
                                console.log("Ответ сервера:", status);
                                console.log("Отправленный taskId:", formData.get("taskId"));
                                console.log("Отправленный answer:", formData.get("answer"));


                                if (status === "SUCCESS") {
                                    result.textContent = "🎉 Задание выполнено!";
                                    result.className = "word-game-result success";
                                    completeForm.remove();

                                    if (isStandaloneWordGame) {
                                        const currentTaskId = formData.get("taskId");
                                        if (currentTaskId
                                            && !window.__guestPassedWordTaskIds.includes(currentTaskId)) {
                                            window.__guestPassedWordTaskIds.push(currentTaskId);
                                        }
                                        showNextWordButton();
                                    } else {
                                        checkButton.disabled = true;
                                    }
                                } else if (status === "AUTH_REQUIRED") {
                                    // Гость тоже должен иметь возможность играть дальше
                                    result.textContent = "🎉 Правильно!";
                                    result.className = "word-game-result success";
                                    completeForm.remove();

                                    if (isStandaloneWordGame) {
                                        const currentTaskId = formData.get("taskId");
                                        if (currentTaskId
                                            && !window.__guestPassedWordTaskIds.includes(currentTaskId)) {
                                            window.__guestPassedWordTaskIds.push(currentTaskId);
                                        }
                                        showNextWordButton();
                                    } else {
                                        checkButton.disabled = true;
                                    }
                                } else if (status === "ALREADY_COMPLETED") {
                                    result.textContent = "Это слово уже пройдено. Попробуй следующее!";
                                    result.className = "word-game-result success";
                                    completeForm.remove();

                                    if (isStandaloneWordGame) {
                                        showNextWordButton();
                                    } else {
                                        checkButton.disabled = true;
                                    }
                                } else if (
                                    status ===
                                    "AUTH_REQUIRED"
                                ) {

                                    result.textContent =
                                        "Войди в аккаунт, чтобы сохранить результат";

                                    result.className =
                                        "word-game-result error";


                                    checkButton.disabled =
                                        false;


                                } else {

                                    result.textContent =
                                        "Не удалось сохранить результат";

                                    result.className =
                                        "word-game-result error";


                                    checkButton.disabled =
                                        false;

                                }


                            } catch (error) {

                                console.error(error);


                                result.textContent =
                                    "Не удалось сохранить результат";

                                result.className =
                                    "word-game-result error";


                                checkButton.disabled =
                                    false;

                            }

                        },
                        700
                    );


                } else {

                    result.textContent =
                        "Попробуй ещё раз";

                    result.className =
                        "word-game-result error";

                }

            }
        );


        renderLetters();

    }

});