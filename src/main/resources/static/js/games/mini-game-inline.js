document.addEventListener("DOMContentLoaded", () => {

    const gameCards = document.querySelectorAll(".activity-mini-game");

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

                const html = await response.text();

                content.innerHTML = html;

                button.style.display = "none";

                initializeWordGame(content);

            } catch (error) {

                console.error(error);

                button.disabled = false;
                button.textContent = "🎮 Начать игру";

                content.innerHTML =
                    "<p>Не удалось загрузить игру. Попробуй ещё раз.</p>";
            }
        });
    });


    function initializeWordGame(container) {

        const game =
            container.querySelector("[data-word-game]");

        if (!game) {
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


        function shuffle(array) {
            return [...array]
                .sort(() => Math.random() - 0.5);
        }


        function renderLetters() {

            lettersContainer.innerHTML = "";

            shuffle(word.split(""))
                .forEach((letter, index) => {

                    const button =
                        document.createElement("button");

                    button.type = "button";
                    button.className = "word-game-letter";
                    button.textContent = letter;

                    button.addEventListener("click", () => {

                        if (selectedLetters.length >= word.length) {
                            return;
                        }

                        selectedLetters.push({
                            letter: letter,
                            index: index
                        });

                        button.disabled = true;

                        renderAnswer();
                    });

                    lettersContainer.appendChild(button);
                });
        }


        function renderAnswer() {

            answerContainer.innerHTML = "";

            selectedLetters.forEach((item, position) => {

                const button =
                    document.createElement("button");

                button.type = "button";
                button.className =
                    "word-game-selected-letter";

                button.textContent = item.letter;

                button.addEventListener("click", () => {

                    selectedLetters.splice(position, 1);

                    renderLetters();
                    renderAnswer();
                });

                answerContainer.appendChild(button);
            });
        }


        checkButton.addEventListener("click", () => {

            const answer =
                selectedLetters
                    .map(item => item.letter)
                    .join("");

            if (answer.toLowerCase() === word.toLowerCase()) {
                result.textContent = "🎉 Правильно!";
                result.className = "word-game-result success";
                checkButton.disabled = true;

                submittedAnswer.value = answer;

                setTimeout(async () => {
                    try {
                        const formData = new FormData(completeForm);

                        const response = await fetch("/mini-game/complete-inline", {
                            method: "POST",
                            body: formData
                        });

                        const status = await response.text();

                        if (status === "SUCCESS") {
                            result.textContent = "🎉 Задание выполнено!";
                            result.className = "word-game-result success";

                            completeForm.remove();
                        } else if (status === "AUTH_REQUIRED") {
                            result.textContent = "Войди в аккаунт, чтобы сохранить результат";
                            result.className = "word-game-result error";
                            checkButton.disabled = false;
                        } else {
                            result.textContent = "Не удалось сохранить результат";
                            result.className = "word-game-result error";
                            checkButton.disabled = false;
                        }

                    } catch (error) {
                        console.error(error);

                        result.textContent = "Не удалось сохранить результат";
                        result.className = "word-game-result error";
                        checkButton.disabled = false;
                    }
                }, 700);
            }

            else {

                result.textContent =
                    "Попробуй ещё раз";

                result.className =
                    "word-game-result error";
            }
        });


        renderLetters();
    }
});