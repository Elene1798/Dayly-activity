function initializeSudokuGame(game) {
    const boardEl = game.querySelector("[data-sudoku-board]");
    const resultEl = game.querySelector("[data-sudoku-result]");
    const mistakesEl = game.querySelector("[data-sudoku-mistakes]");
    const timeEl = game.querySelector("[data-sudoku-time]");
    const newBtn = game.querySelector("[data-sudoku-new]");
    const diffButtons = game.querySelectorAll("[data-difficulty]");
    const bestEl = game.querySelector("[data-sudoku-best]");
    const hintBtn = game.querySelector("[data-sudoku-hint]");

    let currentDifficulty = "EASY";
    let solution = [];        // готовое решение
    let puzzle = [];          // начальная сетка (с нулями)
    let mistakes = 0;
    let seconds = 0;
    let timer = null;
    let started = false;

    if (bestEl) {
        fetch("/mini-game/sudoku/record", { credentials: "same-origin" })
            .then(r => r.text())
            .then(v => {
                const n = parseInt(v, 10);
                if (!isNaN(n) && n > 0) {
                    const m = Math.floor(n / 60);
                    const s = n % 60;
                    bestEl.textContent = `${m}:${String(s).padStart(2, "0")}`;
                }
            })
            .catch(() => {});
    }

    // ---------- Генерация судоку ----------

    function generateSolved() {
        const grid = Array.from({ length: 9 }, () => Array(9).fill(0));
        solve(grid);
        return grid;
    }

    function solve(grid) {
        for (let row = 0; row < 9; row++) {
            for (let col = 0; col < 9; col++) {
                if (grid[row][col] === 0) {
                    const nums = shuffle([1,2,3,4,5,6,7,8,9]);
                    for (const num of nums) {
                        if (isValid(grid, row, col, num)) {
                            grid[row][col] = num;
                            if (solve(grid)) return true;
                            grid[row][col] = 0;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    function isValid(grid, row, col, num) {
        for (let i = 0; i < 9; i++) {
            if (grid[row][i] === num || grid[i][col] === num) return false;
        }
        const boxRow = Math.floor(row / 3) * 3;
        const boxCol = Math.floor(col / 3) * 3;
        for (let r = boxRow; r < boxRow + 3; r++) {
            for (let c = boxCol; c < boxCol + 3; c++) {
                if (grid[r][c] === num) return false;
            }
        }
        return true;
    }

    function shuffle(arr) {
        for (let i = arr.length - 1; i > 0; i--) {
            const j = Math.floor(Math.random() * (i + 1));
            [arr[i], arr[j]] = [arr[j], arr[i]];
        }
        return arr;
    }

    // ---------- Удаление клеток ----------

    function removeCells(solved, count) {
        const grid = solved.map(row => [...row]);
        let removed = 0;

        while (removed < count) {
            const row = Math.floor(Math.random() * 9);
            const col = Math.floor(Math.random() * 9);

            if (grid[row][col] !== 0) {
                grid[row][col] = 0;
                removed++;
            }
        }

        return grid;
    }

    // ---------- Рендер ----------

    function buildBoard(puzzleGrid) {
        boardEl.innerHTML = "";

        for (let row = 0; row < 9; row++) {
            for (let col = 0; col < 9; col++) {
                const cell = document.createElement("div");
                cell.className = "sudoku-cell";

                if (col % 3 === 2 && col !== 8) cell.classList.add("sudoku-cell--right-border");
                if (row % 3 === 2 && row !== 8) cell.classList.add("sudoku-cell--bottom-border");

                const value = puzzleGrid[row][col];

                if (value !== 0) {
                    cell.textContent = value;
                    cell.classList.add("sudoku-cell--fixed");
                } else {
                    const input = document.createElement("input");
                    input.type = "number";
                    input.min = "1";
                    input.max = "9";
                    input.maxLength = 1;
                    input.dataset.row = row;
                    input.dataset.col = col;

                    input.addEventListener("input", () => {
                        if (input.value.length > 1) {
                            input.value = input.value.slice(0, 1);
                        }
                        validateInput(input);
                    });

                    cell.appendChild(input);
                }

                boardEl.appendChild(cell);
            }
        }
    }

    // ---------- Валидация ----------

    function validateInput(input) {
        if (!started) startTimer();

        const row = parseInt(input.dataset.row, 10);
        const col = parseInt(input.dataset.col, 10);
        const value = parseInt(input.value, 10);

        // Очищаем ошибки
        boardEl.querySelectorAll(".sudoku-cell--error").forEach(el => el.classList.remove("sudoku-cell--error"));

        if (isNaN(value)) return;

        // Проверяем, что ввод не противоречит правилам
        let hasError = false;

        for (let i = 0; i < 9; i++) {
            if (i !== col) {
                const otherInput = boardEl.querySelector(`input[data-row="${row}"][data-col="${i}"]`);
                if (otherInput && parseInt(otherInput.value, 10) === value) hasError = true;
            }
            if (i !== row) {
                const otherInput = boardEl.querySelector(`input[data-row="${i}"][data-col="${col}"]`);
                if (otherInput && parseInt(otherInput.value, 10) === value) hasError = true;
            }
        }

        const boxRow = Math.floor(row / 3) * 3;
        const boxCol = Math.floor(col / 3) * 3;
        for (let r = boxRow; r < boxRow + 3; r++) {
            for (let c = boxCol; c < boxCol + 3; c++) {
                if ((r !== row || c !== col)) {
                    const otherInput = boardEl.querySelector(`input[data-row="${r}"][data-col="${c}"]`);
                    if (otherInput && parseInt(otherInput.value, 10) === value) hasError = true;
                }
            }
        }

        if (hasError) {
            mistakes++;
            mistakesEl.textContent = mistakes;
            input.classList.add("sudoku-cell--error");
            input.parentElement.classList.add("sudoku-cell--error");
        } else {
            input.parentElement.classList.remove("sudoku-cell--error");
        }

        // Проверка победы
        checkWin();
    }

    function checkWin() {
        const inputs = boardEl.querySelectorAll("input");

        for (const input of inputs) {
            if (input.value === "") return;
        }

        // Все заполнены — проверяем корректность
        if (isSolved()) {
            stopTimer();
            resultEl.textContent = `🎉 Судоку решено! Время: ${timeEl.textContent}`;
            resultEl.className = "sudoku-result success";

            submitRecord(seconds, currentDifficulty);
        }
    }

    function isSolved() {
        // Проверяем, что каждая строка/столбец/квадрат содержит 1–9
        const grid = Array.from({ length: 9 }, () => Array(9).fill(0));

        // Заполняем из DOM
        const fixedCells = boardEl.querySelectorAll(".sudoku-cell--fixed");
        let fixedIndex = 0;
        for (let r = 0; r < 9; r++) {
            for (let c = 0; c < 9; c++) {
                const input = boardEl.querySelector(`input[data-row="${r}"][data-col="${c}"]`);
                if (input) {
                    grid[r][c] = parseInt(input.value, 10) || 0;
                } else {
                    grid[r][c] = parseInt(fixedCells[fixedIndex++].textContent, 10);
                }
            }
        }

        // Проверка
        for (let i = 0; i < 9; i++) {
            const row = new Set();
            const col = new Set();
            for (let j = 0; j < 9; j++) {
                if (grid[i][j] < 1 || grid[i][j] > 9) return false;
                if (row.has(grid[i][j])) return false;
                row.add(grid[i][j]);
                if (col.has(grid[j][i])) return false;
                col.add(grid[j][i]);
            }
        }

        return true;
    }

    // ---------- Таймер ----------

    function startTimer() {
        if (started) return;
        started = true;
        timer = setInterval(() => {
            seconds++;
            const m = Math.floor(seconds / 60);
            const s = seconds % 60;
            timeEl.textContent = `${m}:${String(s).padStart(2, "0")}`;
        }, 1000);
    }

    function stopTimer() {
        if (timer) clearInterval(timer);
        timer = null;
    }

    // ---------- Старт игры ----------

    function startGame() {
        stopTimer();
        seconds = 0;
        mistakes = 0;
        started = false;
        timeEl.textContent = "0:00";
        mistakesEl.textContent = "0";
        resultEl.textContent = "";
        resultEl.className = "sudoku-result";

        solution = generateSolved();

        const removeCount = {
            EASY: 30,
            MEDIUM: 45,
            HARD: 55
        }[currentDifficulty];

        puzzle = removeCells(solution, removeCount);
        buildBoard(puzzle);
    }

    // ---------- Кнопки сложности ----------

    function updateDiffButtons() {
        diffButtons.forEach(btn => {
            btn.classList.toggle("active", btn.dataset.difficulty === currentDifficulty);
        });
    }

    diffButtons.forEach(btn => {
        btn.addEventListener("click", () => {
            currentDifficulty = btn.dataset.difficulty;
            updateDiffButtons();
            startGame();
        });
    });

    // ---------- Кнопка «Играть ещё» ----------

    newBtn.addEventListener("click", () => {
        startGame();
    });

    // ---------- Старт ----------

    updateDiffButtons();
    startGame();

    async function submitRecord(time, difficulty) {
        const csrfMeta = document.querySelector('meta[name="_csrf"]');
        const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');

        const token = csrfMeta ? csrfMeta.content : null;
        const header = csrfHeaderMeta ? csrfHeaderMeta.content : null;

        const formData = new FormData();
        formData.append("time", time);
        formData.append("difficulty", difficulty);

        const headers = {};
        if (token && header) headers[header] = token;

        try {
            await fetch("/mini-game/sudoku/save-record", {
                method: "POST",
                headers,
                body: formData,
                credentials: "same-origin"
            });
        } catch (e) {
            console.error(e);
        }
    }

    hintBtn.addEventListener("click", () => {
        useHint();
    });


    function useHint() {
        // Ищем все пустые инпуты
        const emptyInputs = Array.from(boardEl.querySelectorAll("input"))
            .filter(input => input.value === "");

        if (emptyInputs.length === 0) {
            resultEl.textContent = "Все ячейки уже заполнены!";
            resultEl.className = "sudoku-result";
            return;
        }

        // Выбираем случайный пустой инпут
        const randomInput = emptyInputs[Math.floor(Math.random() * emptyInputs.length)];

        const row = parseInt(randomInput.dataset.row, 10);
        const col = parseInt(randomInput.dataset.col, 10);

        // Берём правильное значение из solution
        const correctValue = solution[row][col];

        // Заполняем
        randomInput.value = correctValue;

        // Подсвечиваем как подсказку
        randomInput.parentElement.classList.add("sudoku-cell--hint");
        randomInput.parentElement.classList.remove("sudoku-cell--error");

        // Помечаем как fixed (уже нельзя изменить)
        randomInput.disabled = true;

        // Проверяем победу
        checkWin();
    }
}