function initializeMemoryGame(game) {

    const board =
        game.querySelector(".memory-game-board");

    const result =
        game.querySelector(".memory-game-result");

    const activityCard =
        game.closest(".activity-mini-game");

    const startButton =
        activityCard?.querySelector(
            ".activity-mini-game-button"
        );

    let cards =
        Array.from(board.querySelectorAll(".memory-card"));

    let firstCard = null;
    let secondCard = null;

    let matchedPairs = 0;
    let lockBoard = true;


    startGame();


    function startGame() {

        firstCard = null;
        secondCard = null;

        matchedPairs = 0;

        lockBoard = true;

        result.textContent = "";


        cards.forEach(card => {

            card.classList.remove(
                "flipped",
                "matched"
            );

        });


        shuffleCards();


        /*
         * В начале показываем все картинки.
         */

        cards.forEach(card => {
            card.classList.add("flipped");
        });


        /*
         * Через 3 секунды закрываем.
         */

        setTimeout(() => {

            cards.forEach(card => {
                card.classList.remove("flipped");
            });

            lockBoard = false;

        }, 3000);
    }


    function shuffleCards() {

        cards.sort(() => Math.random() - 0.5);

        cards.forEach(card => {
            board.appendChild(card);
        });
    }


    cards.forEach(card => {

        card.addEventListener("click", () => {

            if (lockBoard) {
                return;
            }

            if (card.classList.contains("matched")) {
                return;
            }

            if (card === firstCard) {
                return;
            }


            card.classList.add("flipped");


            if (firstCard === null) {

                firstCard = card;

                return;
            }


            secondCard = card;

            checkPair();

        });

    });


    function checkPair() {

        lockBoard = true;

        const firstImage =
            firstCard.dataset.image;

        const secondImage =
            secondCard.dataset.image;


        /*
         * Пара найдена.
         */

        if (firstImage === secondImage) {

            firstCard.classList.add("matched");
            secondCard.classList.add("matched");

            matchedPairs++;

            firstCard = null;
            secondCard = null;

            lockBoard = false;


            if (matchedPairs === cards.length / 2) {

                finishGame();

            }

            return;
        }


        /*
         * Карты разные.
         */

        setTimeout(() => {

            firstCard.classList.remove("flipped");
            secondCard.classList.remove("flipped");

            firstCard = null;
            secondCard = null;

            lockBoard = false;

        }, 800);
    }


    async function finishGame() {

        lockBoard = true;

        result.textContent =
            "🎉 Все пары найдены!";

        result.className =
            "memory-game-result success";


        const activityId =
            game.dataset.activityId;


        const formData = new FormData();

        formData.append(
            "activityId",
            activityId
        );


        const csrfToken =
            game.querySelector(".memory-csrf-token");


        if (csrfToken) {

            formData.append(
                csrfToken.name,
                csrfToken.value
            );

        }


        try {

            const response = await fetch(
                "/mini-game/complete-memory-inline",
                {
                    method: "POST",
                    body: formData
                }
            );


            const status =
                await response.text();

            console.log(
                "MEMORY completion:",
                response.status,
                status
            );


            if (status === "SUCCESS") {

                result.textContent =
                    "🎉 Игра завершена!";

                result.className =
                    "memory-game-result success";

                returnToStartButton();


            } else if (status === "AUTH_REQUIRED") {

                result.textContent =
                    "🎉 Все пары найдены! " +
                    "Войди в аккаунт, чтобы сохранить результат";

                result.className =
                    "memory-game-result success";

                returnToStartButton();


            } else {

                result.textContent =
                    "Не удалось сохранить результат";

                result.className =
                    "memory-game-result error";

                lockBoard = false;
            }

        } catch (error) {

            console.error(error);

            result.textContent =
                "Не удалось сохранить результат";

            result.className =
                "memory-game-result error";

            lockBoard = false;
        }
    }

    function returnToStartButton() {

        setTimeout(() => {

            const content =
                activityCard?.querySelector(
                    ".activity-mini-game-content"
                );

            if (content) {
                content.innerHTML = "";
            }

            if (startButton) {

                startButton.style.display = "inline-block";

                startButton.disabled = false;

                startButton.textContent =
                    "🎮 Начать игру";
            }

        }, 1500);
    }
}