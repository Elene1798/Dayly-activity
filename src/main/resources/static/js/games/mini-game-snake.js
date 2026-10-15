function initializeSnakeGame(game) {
    if (!game) return;

    const canvas = game.querySelector("[data-snake-canvas]");
    const ctx = canvas.getContext("2d");

    const scoreEl = game.querySelector("[data-snake-score]");
    const bestEl = game.querySelector("[data-snake-best]");
    const resultEl = game.querySelector("[data-snake-result]");
    const startBtn = game.querySelector("[data-snake-start]");
    const restartBtn = game.querySelector("[data-snake-restart]");

    const csrfInput = game.querySelector(".snake-csrf-token");

    const CELL = 20;
    const COLS = canvas.width / CELL;
    const ROWS = canvas.height / CELL;

    let snake = [];
    let direction = { x: 1, y: 0 };
    let nextDirection = { x: 1, y: 0 };
    let food = null;
    let score = 3;
    let bestScore = 0;
    let gameLoop = null;
    let speed = 150;
    let isRunning = false;

    const FOOD_EMOJIS = ["🍎", "🍓", "🍒", "🍇", "🍄", "🍊", "🍋", "🥝"];

    // Загружаем рекорд
    fetch("/mini-game/snake/record", { credentials: "same-origin" })
        .then(r => r.text())
        .then(v => {
            const n = parseInt(v, 10);
            if (!isNaN(n)) {
                bestScore = n;
                bestEl.textContent = bestScore;
            }
        })
        .catch(() => {});

    function reset() {
        snake = [
            { x: 5, y: 10 },
            { x: 4, y: 10 },
            { x: 3, y: 10 }
        ];
        direction = { x: 1, y: 0 };
        nextDirection = { x: 1, y: 0 };
        score = 3;
        speed = 150;
        scoreEl.textContent = score;
        resultEl.textContent = "";
        spawnFood();
        draw();
    }

    function spawnFood() {
        let attempts = 0;
        while (attempts < 100) {
            const f = {
                x: Math.floor(Math.random() * COLS),
                y: Math.floor(Math.random() * ROWS),
                emoji: FOOD_EMOJIS[Math.floor(Math.random() * FOOD_EMOJIS.length)]
            };
            if (!snake.some(s => s.x === f.x && s.y === f.y)) {
                food = f;
                return;
            }
            attempts++;
        }
    }

    function draw() {
        // фон
        ctx.fillStyle = "#f8f2ff";
        ctx.fillRect(0, 0, canvas.width, canvas.height);

        // сетка
        ctx.strokeStyle = "rgba(96, 64, 168, 0.08)";
        ctx.lineWidth = 1;
        for (let i = 0; i <= COLS; i++) {
            ctx.beginPath();
            ctx.moveTo(i * CELL, 0);
            ctx.lineTo(i * CELL, canvas.height);
            ctx.stroke();
        }
        for (let i = 0; i <= ROWS; i++) {
            ctx.beginPath();
            ctx.moveTo(0, i * CELL);
            ctx.lineTo(canvas.width, i * CELL);
            ctx.stroke();
        }

        // еда
        if (food) {
            ctx.font = `${CELL - 4}px serif`;
            ctx.textAlign = "center";
            ctx.textBaseline = "middle";
            ctx.fillText(
                food.emoji,
                food.x * CELL + CELL / 2,
                food.y * CELL + CELL / 2
            );
        }

        // змейка
        snake.forEach((seg, i) => {
            const isHead = i === 0;
            ctx.fillStyle = isHead ? "#6040a8" : "#8053e8";
            ctx.fillRect(
                seg.x * CELL + 1,
                seg.y * CELL + 1,
                CELL - 2,
                CELL - 2
            );

            if (isHead) {
                ctx.fillStyle = "#fff";
                ctx.beginPath();
                ctx.arc(
                    seg.x * CELL + CELL / 2,
                    seg.y * CELL + CELL / 2,
                    3,
                    0,
                    Math.PI * 2
                );
                ctx.fill();
            }
        });
    }

    function tick() {
        direction = nextDirection;

        const head = {
            x: snake[0].x + direction.x,
            y: snake[0].y + direction.y
        };

        // столкновение со стеной
        if (head.x < 0 || head.x >= COLS || head.y < 0 || head.y >= ROWS) {
            return endGame();
        }

        // столкновение с хвостом
        if (snake.some(s => s.x === head.x && s.y === head.y)) {
            return endGame();
        }

        snake.unshift(head);

        // съели еду
        if (food && head.x === food.x && head.y === food.y) {
            score++;
            scoreEl.textContent = score;
            spawnFood();
            if (speed > 70) speed -= 3;
        } else {
            snake.pop();
        }

        draw();
    }

    function startLoop() {
        if (gameLoop) clearInterval(gameLoop);
        gameLoop = setInterval(tick, speed);
    }

    function endGame() {
        clearInterval(gameLoop);
        gameLoop = null;
        isRunning = false;

        resultEl.textContent = `🐍 Игра окончена! Длина змейки: ${score}`;
        restartBtn.hidden = false;

        if (score > bestScore) {
            bestScore = score;
            bestEl.textContent = bestScore;
            saveRecord(score);
        }
    }

    function saveRecord(value) {
        const formData = new FormData();
        formData.append("score", value);
        if (csrfInput) {
            formData.append(csrfInput.name, csrfInput.value);
        }

        fetch("/mini-game/snake/save-record", {
            method: "POST",
            body: formData,
            credentials: "same-origin"
        }).catch(() => {});
    }

    function changeDirection(nx, ny) {
        // нельзя разворачиваться на 180
        if (direction.x === -nx && direction.y === -ny) return;
        nextDirection = { x: nx, y: ny };
    }

    document.addEventListener("keydown", (e) => {
        if (!isRunning) return;
        switch (e.key) {
            case "ArrowUp": case "w": case "W":
                changeDirection(0, -1); e.preventDefault(); break;
            case "ArrowDown": case "s": case "S":
                changeDirection(0, 1); e.preventDefault(); break;
            case "ArrowLeft": case "a": case "A":
                changeDirection(-1, 0); e.preventDefault(); break;
            case "ArrowRight": case "d": case "D":
                changeDirection(1, 0); e.preventDefault(); break;
        }
    });

    // свайпы
    let touchStart = null;
    canvas.addEventListener("touchstart", (e) => {
        const t = e.touches[0];
        touchStart = { x: t.clientX, y: t.clientY };
    }, { passive: true });

    canvas.addEventListener("touchend", (e) => {
        if (!touchStart || !isRunning) return;
        const t = e.changedTouches[0];
        const dx = t.clientX - touchStart.x;
        const dy = t.clientY - touchStart.y;

        if (Math.abs(dx) > Math.abs(dy)) {
            changeDirection(dx > 0 ? 1 : -1, 0);
        } else {
            changeDirection(0, dy > 0 ? 1 : -1);
        }
        touchStart = null;
    }, { passive: true });

    function startGame() {
        reset();
        isRunning = true;
        startBtn.hidden = true;
        restartBtn.hidden = true;
        resultEl.textContent = "";
        startLoop();
    }

    startBtn.addEventListener("click", startGame);
    restartBtn.addEventListener("click", startGame);

    // начальная отрисовка
    reset();
}