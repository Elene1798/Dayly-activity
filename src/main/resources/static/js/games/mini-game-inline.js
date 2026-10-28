document.addEventListener("DOMContentLoaded", () => {

    const gameCards = document.querySelectorAll(".activity-mini-game");

    gameCards.forEach(card => {

        const button = card.querySelector(".activity-mini-game-button");
        const content = card.querySelector(".activity-mini-game-content");

        const gameId = card.dataset.miniGameId;
        const activityId = card.dataset.activityId;

        if (!button || !content || !gameId || !activityId) {
            return;
        }

        button.addEventListener("click", async () => {

            button.disabled = true;
            button.textContent = "Загрузка...";

            try {
                const response = await fetch(
                    `/mini-game/${gameId}/content?activityId=${activityId}`,
                    { credentials: "same-origin" }
                );

                if (!response.ok) {
                    throw new Error("Не удалось загрузить игру");
                }

                const html = await response.text();
                content.innerHTML = html;

                const wordGame = content.querySelector("[data-word-game]");
                if (wordGame) {
                    initializeWordGame(content);
                }

                const memoryGame = content.querySelector("[data-memory-game]");
                if (memoryGame && typeof initializeMemoryGame === "function") {
                    initializeMemoryGame(memoryGame);
                }

                const crocodileGame = content.querySelector("[data-crocodile-game]");
                if (crocodileGame) {
                    initializeCrocodileGame(crocodileGame);
                }

                button.style.display = "none";

            } catch (error) {
                console.error(error);

                button.disabled = false;
                button.textContent = "🎮 Начать игру";

                content.innerHTML =
                    "<p>Не удалось загрузить игру. Попробуй ещё раз.</p>";
            }
        });
    });


    // =====================================================
    // ИГРА «СОБЕРИ СЛОВО»
    // =====================================================

    window.__guestPassedWordTaskIds =
        window.__guestPassedWordTaskIds || [];

    window.initializeWordGame = function (container) {

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

        const lettersContainer = game.querySelector(".word-game-letters");
        const answerContainer = game.querySelector(".word-game-answer");
        let checkButton = game.querySelector(".word-game-check");
        const result = game.querySelector(".word-game-result");
        const completeForm = game.querySelector(".word-game-complete-form");
        const submittedAnswer = game.querySelector(".word-game-submitted-answer");

        if (
            !lettersContainer ||
            !answerContainer ||
            !checkButton ||
            !result ||
            !completeForm ||
            !submittedAnswer
        ) {
            console.error("В игре «Собери слово» отсутствуют необходимые элементы");
            return;
        }

        const word = game.dataset.answer;

        if (!word) {
            console.error("Не задан правильный ответ data-answer");
            return;
        }

        let selectedLetters = [];
        let isSubmitting = false;

        function shuffle(array) {
            return [...array].sort(() => Math.random() - 0.5);
        }

        const letters = shuffle(
            word.split("").map((letter, index) => ({
                letter,
                index
            }))
        );

        function renderLetters() {
            lettersContainer.innerHTML = "";

            letters.forEach(item => {
                const button = document.createElement("button");

                button.type = "button";
                button.className = "word-game-letter";
                button.textContent = item.letter;

                button.disabled = selectedLetters.some(
                    selected => selected.index === item.index
                );

                button.addEventListener("click", () => {
                    if (
                        selectedLetters.length >= word.length ||
                        selectedLetters.some(
                            selected => selected.index === item.index
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

        function renderAnswer() {
            answerContainer.innerHTML = "";

            selectedLetters.forEach((item, position) => {
                const button = document.createElement("button");

                button.type = "button";
                button.className = "word-game-selected-letter";
                button.textContent = item.letter;

                button.addEventListener("click", () => {
                    selectedLetters.splice(position, 1);
                    renderLetters();
                    renderAnswer();
                });

                answerContainer.appendChild(button);
            });
        }

        function showNextWordButton() {
            const nextButton = checkButton.cloneNode(true);

            nextButton.disabled = false;
            nextButton.textContent = "Играть ещё";
            checkButton.replaceWith(nextButton);
            checkButton = nextButton;

            nextButton.addEventListener("click", async () => {
                const host = game.closest(".standalone-mini-game-content");
                const card = game.closest(".mini-game-card");

                if (!host || !card) {
                    console.error("Не найден контейнер следующей игры");
                    return;
                }

                const nextGameId =
                    card.dataset.currentGameId || card.dataset.miniGameId;

                if (!nextGameId) {
                    console.error("Не найден ID мини-игры");
                    return;
                }

                nextButton.disabled = true;
                nextButton.textContent = "Загрузка...";

                try {
                    const excludeParam = window.__guestPassedWordTaskIds.length
                        ? "?excludeTaskIds=" +
                        window.__guestPassedWordTaskIds.join(",")
                        : "";

                    const response = await fetch(
                        `/mini-game/${nextGameId}/content${excludeParam}`,
                        { credentials: "same-origin" }
                    );

                    if (!response.ok) {
                        throw new Error("Не удалось загрузить следующую игру");
                    }

                    const html = await response.text();

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

        checkButton.addEventListener("click", () => {

            if (isSubmitting) {
                return;
            }

            const answer = selectedLetters
                .map(item => item.letter)
                .join("");

            if (answer.toLowerCase() !== word.toLowerCase()) {
                result.textContent = "Попробуй ещё раз";
                result.className = "word-game-result error";
                return;
            }

            result.textContent = "🎉 Правильно! Сохраняем результат...";
            result.className = "word-game-result success";
            checkButton.disabled = true;

            submittedAnswer.value = answer;
            isSubmitting = true;

            setTimeout(async () => {

                try {
                    const formData = new FormData(completeForm);

                    const isStandaloneWordGame =
                        game.dataset.standalone === "true";

                    const endpoint = isStandaloneWordGame
                        ? "/mini-game/complete-word-standalone"
                        : "/mini-game/complete-inline";

                    const response = await fetch(endpoint, {
                        method: "POST",
                        body: formData,
                        credentials: "same-origin"
                    });

                    const status = (await response.text()).trim();

                    if (!response.ok) {
                        throw new Error(
                            `Ошибка сохранения: HTTP ${response.status}; ${status}`
                        );
                    }

                    if (status === "SUCCESS") {

                        result.textContent = "🎉 Задание выполнено!";
                        result.className = "word-game-result success";
                        completeForm.remove();

                        // Кнопка "Играть ещё" появляется ТОЛЬКО в отдельной мини-игре
                        if (isStandaloneWordGame) {
                            const currentTaskId = formData.get("taskId");

                            if (
                                currentTaskId &&
                                !window.__guestPassedWordTaskIds.includes(
                                    String(currentTaskId)
                                )
                            ) {
                                window.__guestPassedWordTaskIds.push(
                                    String(currentTaskId)
                                );
                            }

                            showNextWordButton();
                        }

                    } else if (status === "AUTH_REQUIRED") {

                        result.textContent =
                            "🎉 Задание выполнено! Войди в аккаунт, чтобы сохранить прогресс.";
                        result.className = "word-game-result success";
                        completeForm.remove();

                        // Даже гостю даем кнопку "Играть ещё"
                        if (isStandaloneWordGame) {
                            const currentTaskId = formData.get("taskId");

                            if (
                                currentTaskId &&
                                !window.__guestPassedWordTaskIds.includes(
                                    String(currentTaskId)
                                )
                            ) {
                                window.__guestPassedWordTaskIds.push(
                                    String(currentTaskId)
                                );
                            }

                            showNextWordButton();
                        }

                    } else {

                        console.error("Неожиданный ответ сервера:", status);

                        result.textContent =
                            "Не удалось сохранить результат. Попробуй ещё раз.";
                        result.className = "word-game-result error";

                        checkButton.disabled = false;
                        isSubmitting = false;
                    }

                } catch (error) {

                    console.error("Ошибка сохранения результата игры:", error);

                    result.textContent =
                        "Не удалось сохранить результат. Попробуй ещё раз.";
                    result.className = "word-game-result error";

                    checkButton.disabled = false;
                    isSubmitting = false;
                }

            }, 300);
        });

        renderLetters();
    };

});