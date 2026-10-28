
(function () {
    function initializeWordGame(game) {
        if (!game || game.dataset.wordGameInitialized === "true") {
            return;
        }

        const lettersContainer = game.querySelector(".word-game-letters");
        const answerContainer = game.querySelector(".word-game-answer");
        const checkButton = game.querySelector(".word-game-check");
        const result = game.querySelector(".word-game-result");
        const completeForm = game.querySelector(".word-game-complete-form");
        const submittedAnswer = game.querySelector(".word-game-submitted-answer");

        const word = game.dataset.answer;
        const taskId = game.dataset.taskId;

        if (
            !lettersContainer ||
            !answerContainer ||
            !checkButton ||
            !result ||
            !completeForm ||
            !submittedAnswer ||
            !word ||
            !taskId
        ) {
            console.error("Не найдены необходимые элементы игры «Собери слово».");
            return;
        }

        game.dataset.wordGameInitialized = "true";

        let selectedLetters = [];
        let isSubmitting = false;

        function shuffle(array) {
            return [...array].sort(() => Math.random() - 0.5);
        }

        function renderLetters() {
            lettersContainer.innerHTML = "";

            shuffle(word.split("")).forEach((letter, index) => {
                const button = document.createElement("button");

                button.type = "button";
                button.className = "word-game-letter";
                button.textContent = letter;

                button.addEventListener("click", () => {
                    if (isSubmitting || selectedLetters.length >= word.length) {
                        return;
                    }

                    selectedLetters.push({ letter, index });
                    button.disabled = true;

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
                    if (isSubmitting) {
                        return;
                    }

                    selectedLetters.splice(position, 1);

                    renderLetters();
                    renderAnswer();
                });

                answerContainer.appendChild(button);
            });
        }

        function showPlayAgainButton() {
            // 1. Скрываем кнопку "Проверить"
            checkButton.hidden = true;
            checkButton.style.display = "none";

            // 2. Ищем или создаем кнопку "Играть ещё"
            let playAgainButton = game.querySelector(".word-game-play-again");

            if (!playAgainButton) {
                playAgainButton = document.createElement("button");
                playAgainButton.type = "button";
                // Копируем классы кнопки "Проверить" (чтобы она выглядела так же)
                playAgainButton.className = checkButton.className + " word-game-play-again";
                playAgainButton.textContent = "Играть ещё";

                // Вставляем её ПЕРЕД кнопкой "Проверить" (на то же место)
                checkButton.parentNode.insertBefore(playAgainButton, checkButton);
            }

            // 3. Показываем кнопку
            playAgainButton.hidden = false;
            playAgainButton.style.display = "inline-block"; // или block, в зависимости от вашего CSS
            playAgainButton.disabled = false;

            // 4. Логика клика
            playAgainButton.onclick = async () => {
                const card = game.closest(".mini-game-card");

                // Если игра открыта отдельно (не в модалке) — перезагружаем страницу
                if (!card) {
                    window.location.reload();
                    return;
                }

                playAgainButton.disabled = true;

                try {
                    const gameId = card.dataset.miniGameId;
                    const content = card.querySelector(".standalone-mini-game-content");

                    if (!gameId || !content) {
                        throw new Error("Не удалось определить карточку игры.");
                    }

                    const excludedIds = new Set(
                        (card.dataset.excludedTaskIds || "")
                            .split(",")
                            .filter(Boolean)
                    );

                    excludedIds.add(String(taskId));
                    card.dataset.excludedTaskIds = [...excludedIds].join(",");

                    const params = new URLSearchParams();
                    params.set("excludeTaskIds", [...excludedIds].join(","));

                    const response = await fetch(
                        `/mini-game/${gameId}/content?${params.toString()}`,
                        {
                            method: "GET",
                            credentials: "same-origin"
                        }
                    );

                    if (!response.ok) {
                        throw new Error("Не удалось загрузить следующее слово.");
                    }

                    const html = await response.text();

                    // Заменяем содержимое игры на новое
                    content.innerHTML = html;

                    const nextGame = content.querySelector("[data-word-game]");

                    if (nextGame) {
                        initializeWordGame(nextGame);
                    } else {
                        content.innerHTML =
                            '<p class="mini-games-completed">Все слова пройдены!</p>';
                    }
                } catch (error) {
                    console.error("Ошибка загрузки следующего слова:", error);
                    result.textContent =
                        "Не удалось загрузить следующее слово. Попробуй ещё раз.";
                    result.className = "word-game-result error";
                    playAgainButton.disabled = false;
                }
            };
        }

        checkButton.addEventListener("click", async () => {
            if (isSubmitting) {
                return;
            }

            const answer = selectedLetters.map(item => item.letter).join("");

            if (answer.toLowerCase() !== word.toLowerCase()) {
                result.textContent = "Вау, ты молодец, но, к сожалению, слово другое :с";
                result.className = "word-game-result error";
                return;
            }

            // --- СЛОВО СОБРАНО ПРАВИЛЬНО ---

            // 1. СРАЗУ показываем кнопку "Играть ещё" (скрываем "Проверить")
            showPlayAgainButton();

            // 2. Показываем сообщение об успехе
            result.textContent = "🎉 Правильно! Сохраняем результат...";
            result.className = "word-game-result success";

            submittedAnswer.value = answer;

            const activityIdInput = completeForm.querySelector('input[name="activityId"]');
            const activityId = activityIdInput?.value?.trim() || "";
            const isStandalone = !activityId;

            const endpoint = isStandalone
                ? "/mini-game/complete-word-standalone"
                : "/mini-game/complete-inline";

            const formData = new FormData(completeForm);

            if (isStandalone) {
                formData.delete("activityId");
            }

            isSubmitting = true;
            checkButton.disabled = true;

            try {
                const response = await fetch(endpoint, {
                    method: "POST",
                    body: formData,
                    credentials: "same-origin"
                });

                const responseText = (await response.text()).trim();

                if (!response.ok) {
                    throw new Error(`HTTP ${response.status}: ${responseText}`);
                }

                if (responseText === "SUCCESS") {
                    result.textContent = "🎉 Слово собрано, результат сохранён!";
                    result.className = "word-game-result success";
                    return;
                }

                if (responseText === "ALREADY_COMPLETED") {
                    result.textContent = "Это задание уже было засчитано ранее.";
                    result.className = "word-game-result success";
                    return;
                }

                if (responseText === "AUTH_REQUIRED") {
                    result.textContent = "🎉 Слово собрано! Войди в аккаунт, чтобы сохранить прогресс.";
                    result.className = "word-game-result success";
                    return;
                }

                if (responseText === "WRONG") {
                    result.textContent = "Ответ не совпал. Попробуй ещё раз.";
                } else {
                    result.textContent = "Не удалось сохранить результат: " + responseText;
                }

                result.className = "word-game-result error";

            } catch (error) {
                console.error("Ошибка сохранения результата:", error);
                result.textContent = "Слово верное, но не удалось сохранить результат.";
                result.className = "word-game-result error";
            } finally {
                // Разблокируем кнопку "Играть ещё", если она есть
                const playAgainButton = game.querySelector(".word-game-play-again");
                if (playAgainButton) {
                    playAgainButton.disabled = false;
                }
            }
        });

        renderLetters();
    }

    // Делаем функцию доступной для mini-games.html,
    // где игра загружается динамически.
    window.initializeWordGame = initializeWordGame;

    document.addEventListener("DOMContentLoaded", () => {
        document.querySelectorAll("[data-word-game]").forEach(initializeWordGame);
    });
})();
