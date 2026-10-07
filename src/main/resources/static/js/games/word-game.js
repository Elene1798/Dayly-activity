document.addEventListener("DOMContentLoaded", () => {

    const game = document.querySelector("[data-word-game]");

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

    const word = game.dataset.answer;

    let selectedLetters = [];


    function shuffle(array) {
        return [...array].sort(() => Math.random() - 0.5);
    }


    function renderLetters() {

        lettersContainer.innerHTML = "";

        shuffle(word.split("")).forEach((letter, index) => {

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
            result.className =
                "word-game-result success";

            checkButton.disabled = true;

            /*
             * Передаём собранное слово на сервер.
             *
             * Сервер дополнительно проверит ответ
             * самостоятельно.
             */
            submittedAnswer.value = answer;

            setTimeout(() => {

                completeForm.submit();

            }, 700);

        } else {

            result.textContent =
                "Попробуй ещё раз";

            result.className =
                "word-game-result error";
        }
    });


    renderLetters();
});