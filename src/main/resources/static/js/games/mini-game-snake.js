function initializeSnakeGame(game) {
    if (!game) return;

    const canvas = game.querySelector("[data-snake-canvas]");
    const ctx = canvas.getContext("2d");

    const scoreEl = game.querySelector("[data-snake-score]");
    const bestEl = game.querySelector("[data-snake-best]");
    const resultEl = game.querySelector("[data-snake-result]");
    const startBtn = game.querySelector("[data-snake-start]");
    const restartBtn = game.querySelector("[data-snake-restart]");

    // Настройки
    const settingsBtn = game.querySelector("[data-snake-settings-btn]");
    const settingsMenu = game.querySelector("[data-snake-settings-menu]");
    const settingsClose = game.querySelector("[data-snake-settings-close]");
    const dpad = game.querySelector("[data-snake-dpad]");
    const controlInputs = game.querySelectorAll('input[name="snake-control-mobile"]');

    let controlMode = localStorage.getItem("snakeControlMode") || "swipe";

    function applyControlMode() {
        // Показываем крестовину ТОЛЬКО если выбран режим "buttons"
        if (dpad) {
            if (controlMode === "buttons") {
                dpad.hidden = false;
                dpad.style.display = "flex";
            } else {
                dpad.hidden = true;
                dpad.style.display = "none";
            }
        }
        controlInputs.forEach(input => {
            input.checked = input.value === controlMode;
        });
    }

    applyControlMode();

    // Асинхронно запрашиваем настройки с сервера (если пользователь авторизован)
    fetch("/mini-game/snake/settings", { credentials: "same-origin" })
        .then(r => r.text())
        .then(mode => {
            if (mode && mode !== "AUTH_REQUIRED" && mode !== "swipe" || (mode === "swipe" && controlMode !== "swipe")) {
                // Если на сервере другое значение, обновляем
                if (["swipe", "buttons", "tap-sides"].includes(mode)) {
                    controlMode = mode;
                    localStorage.setItem("snakeControlMode", controlMode);
                    applyControlMode();
                }
            }
        })
        .catch(() => {});

    if (settingsBtn) {
        settingsBtn.addEventListener("click", () => {
            settingsMenu.hidden = !settingsMenu.hidden;
        });
    }
    if (settingsClose) {
        settingsClose.addEventListener("click", () => {
            settingsMenu.hidden = true;
        });
    }
    controlInputs.forEach(input => {
        input.addEventListener("change", (e) => {
            controlMode = e.target.value;
            localStorage.setItem("snakeControlMode", controlMode);
            applyControlMode();

            const formData = new FormData();
            formData.append("controlMode", controlMode);

            // Достаем CSRF-токен
            const csrfInput = game.querySelector(".snake-csrf-token");
            if (csrfInput) {
                formData.append(csrfInput.name, csrfInput.value);
            }

            fetch("/mini-game/snake/settings", {
                method: "POST",
                body: formData,
                credentials: "same-origin"
            }).catch(() => {});
        });
    });

    // Увеличенные ячейки для мобилок
    const isMobile = window.innerWidth <= 600;
    const CELL = isMobile ? 32 : 20;

    canvas.width = isMobile ? 288 : 400;
    canvas.height = isMobile ? 288 : 400;

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
            { x: Math.floor(COLS / 2), y: Math.floor(ROWS / 2) },
            { x: Math.floor(COLS / 2) - 1, y: Math.floor(ROWS / 2) },
            { x: Math.floor(COLS / 2) - 2, y: Math.floor(ROWS / 2) }
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
        ctx.fillStyle = "#f8f2ff";
        ctx.fillRect(0, 0, canvas.width, canvas.height);

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

        if (food) {
            ctx.font = `${CELL - 6}px serif`;
            ctx.textAlign = "center";
            ctx.textBaseline = "middle";
            ctx.fillText(food.emoji, food.x * CELL + CELL / 2, food.y * CELL + CELL / 2);
        }

        snake.forEach((seg, i) => {
            const isHead = i === 0;
            ctx.fillStyle = isHead ? "#6040a8" : "#8053e8";
            ctx.fillRect(seg.x * CELL + 1, seg.y * CELL + 1, CELL - 2, CELL - 2);

            if (isHead) {
                ctx.fillStyle = "#fff";
                ctx.beginPath();
                ctx.arc(seg.x * CELL + CELL / 2, seg.y * CELL + CELL / 2, CELL / 6, 0, Math.PI * 2);
                ctx.fill();
            }
        });
    }

    function tick() {
        direction = nextDirection;
        const head = { x: snake[0].x + direction.x, y: snake[0].y + direction.y };

        if (head.x < 0 || head.x >= COLS || head.y < 0 || head.y >= ROWS) {
            return endGame();
        }

        if (snake.some(s => s.x === head.x && s.y === head.y)) {
            return endGame();
        }

        snake.unshift(head);

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
        const csrfInput = game.querySelector(".snake-csrf-token");
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
        if (direction.x === -nx && direction.y === -ny) return;
        nextDirection = { x: nx, y: ny };
    }

    // Управление с клавиатуры (всегда работают и WASD, и стрелки)
    // Управление с клавиатуры (работает всегда и для WASD, и для стрелок)
    window.addEventListener("keydown", (e) => {
        if (!isRunning) return;

        // Используем e.code для физических клавиш (W, A, S, D)
        // и e.key для стрелок, чтобы работало независимо от раскладки
        const code = e.code;

        switch (code) {
            case "ArrowUp": case "KeyW":
                changeDirection(0, -1); e.preventDefault(); break;
            case "ArrowDown": case "KeyS":
                changeDirection(0, 1); e.preventDefault(); break;
            case "ArrowLeft": case "KeyA":
                changeDirection(-1, 0); e.preventDefault(); break;
            case "ArrowRight": case "KeyD":
                changeDirection(1, 0); e.preventDefault(); break;
        }
    });

    // Свайпы (только если выбран режим swipe)
    let touchStart = null;
    canvas.addEventListener("touchstart", (e) => {
        if (controlMode !== "swipe") return;
        const t = e.touches[0];
        touchStart = { x: t.clientX, y: t.clientY };
    }, { passive: true });

    canvas.addEventListener("touchend", (e) => {
        if (controlMode !== "swipe" || !touchStart || !isRunning) return;
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

    // Нажатия на стороны экрана (только если выбран режим tap-sides)
    canvas.addEventListener("click", (e) => {
        if (controlMode !== "tap-sides" || !isRunning) return;
        const rect = canvas.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;
        const dx = x - rect.width / 2;
        const dy = y - rect.height / 2;

        if (Math.abs(dx) > Math.abs(dy)) {
            changeDirection(dx > 0 ? 1 : -1, 0);
        } else {
            changeDirection(0, dy > 0 ? 1 : -1);
        }
    });

    // Экранные кнопки (крестовина)
    if (dpad) {
        dpad.querySelectorAll("[data-snake-dir]").forEach(btn => {
            btn.addEventListener("click", (e) => {
                e.preventDefault();
                if (!isRunning) return;
                const dir = btn.dataset.snakeDir;
                switch (dir) {
                    case "up": changeDirection(0, -1); break;
                    case "down": changeDirection(0, 1); break;
                    case "left": changeDirection(-1, 0); break;
                    case "right": changeDirection(1, 0); break;
                }
            });
        });
    }

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
    reset();
}