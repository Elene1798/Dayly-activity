function initializeMemoryGame(game) {
    const board = game.querySelector(".memory-game-board");
    const result = game.querySelector(".memory-game-result");
    const sizeButtons = game.querySelectorAll("[data-size-btn]");
    const playAgainBtn = game.querySelector("[data-memory-play-again]");
    const templates = Array.from(game.querySelectorAll(".memory-templates .memory-card"));

    // Группируем по gameId + difficulty
    // Ключ: "gameId_difficulty", значение: Map<taskId, [card, card]>
    const gamesMap = new Map();

    templates.forEach(card => {
        const gameId = card.dataset.gameId;
        const diff = card.dataset.difficulty || "4";
        const key = `${gameId}_${diff}`;

        if (!gamesMap.has(key)) {
            gamesMap.set(key, new Map());
        }
        const pairMap = gamesMap.get(key);
        const id = card.dataset.taskId;
        if (!pairMap.has(id)) {
            pairMap.set(id, []);
        }
        pairMap.get(id).push(card);
    });

    // Определяем, какие размеры доступны (по любой игре)
    const availableSizes = [3, 4, 5, 6].filter(n => {
        for (const [key, pairMap] of gamesMap) {
            if (key.endsWith(`_${n}`) && pairMap.size > 0) {
                return true;
            }
        }
        return false;
    });

    let currentSize = availableSizes.includes(4) ? 4 : (availableSizes[0] || 3);

    // Текущая игра для выбранного размера
    let currentGameId = null;

    let cards = [];
    let firstCard = null;
    let secondCard = null;
    let matchedPairs = 0;
    let lockBoard = true;

    // ---------- Выбор N случайных пар из группы ----------

    // Выбираем случайную игру для текущего размера
    function pickGameForSize(n) {
        const gamesForSize = [];
        for (const [key, pairMap] of gamesMap) {
            if (key.endsWith(`_${n}`) && pairMap.size > 0) {
                const gameId = key.split('_')[0];
                gamesForSize.push({ gameId, pairMap });
            }
        }

        if (gamesForSize.length === 0) return null;

        // Случайная игра
        const randomIndex = Math.floor(Math.random() * gamesForSize.length);
        return gamesForSize[randomIndex];
    }

    function pickPairs(n) {
        const gameData = pickGameForSize(n);
        if (!gameData) return [];

        const { pairMap } = gameData;
        let pairIds = Array.from(pairMap.keys());

        // Fisher-Yates
        for (let i = pairIds.length - 1; i > 0; i--) {
            const j = Math.floor(Math.random() * (i + 1));
            [pairIds[i], pairIds[j]] = [pairIds[j], pairIds[i]];
        }

        return pairIds.flatMap(id => pairMap.get(id));
    }

    // ---------- Построение доски ----------

    function buildBoard(n) {
        board.innerHTML = "";
        const selected = pickPairs(n);

        if (selected.length === 0) {
            result.textContent = `❌ Нет пар для ${n}×${n}`;
            result.className = "memory-game-result error";
            return false;
        }

        // Перемешиваем
        selected.sort(() => Math.random() - 0.5);

        // Клонируем и добавляем
        cards = selected.map(card => {
            const clone = card.cloneNode(true);
            clone.classList.remove("flipped", "matched");
            board.appendChild(clone);
            return clone;
        });

        // Адаптивная сетка
        const count = cards.length;
        let cols = 2;
        if (count >= 12) cols = 4;
        else if (count >= 8) cols = 4;
        else if (count >= 6) cols = 3;

        board.style.gridTemplateColumns = `repeat(${cols}, minmax(0, 1fr))`;

        firstCard = null;
        secondCard = null;
        matchedPairs = 0;
        lockBoard = true;
        result.textContent = "";
        result.className = "memory-game-result";

        return true;
    }

    // ---------- Игровая логика ----------

    function startGame() {
        const ok = buildBoard(currentSize);
        if (!ok) return;

        cards.forEach(card => card.classList.add("flipped"));

        setTimeout(() => {
            cards.forEach(card => card.classList.remove("flipped"));
            lockBoard = false;
        }, 3000);

        playAgainBtn.hidden = true;
    }

    // Делегирование клика
    board.addEventListener("click", (e) => {
        const card = e.target.closest(".memory-card");
        if (!card) return;
        if (lockBoard) return;
        if (card.classList.contains("matched")) return;
        if (card === firstCard) return;

        card.classList.add("flipped");

        if (firstCard === null) {
            firstCard = card;
            return;
        }

        secondCard = card;
        checkPair();
    });

    function checkPair() {
        lockBoard = true;

        const firstId = firstCard.dataset.taskId;
        const secondId = secondCard.dataset.taskId;

        if (firstId === secondId) {
            firstCard.classList.add("matched");
            secondCard.classList.add("matched");
            matchedPairs++;

            firstCard = null;
            secondCard = null;
            lockBoard = false;

            if (matchedPairs === cards.length / 2) {
                finishGame();
            }
        } else {
            setTimeout(() => {
                firstCard.classList.remove("flipped");
                secondCard.classList.remove("flipped");
                firstCard = null;
                secondCard = null;
                lockBoard = false;
            }, 800);
        }
    }

    function finishGame() {
        lockBoard = true;
        result.textContent = "🎉 Все пары найдены!";
        result.className = "memory-game-result success";
        playAgainBtn.hidden = false;
        submitCompletion();
    }

    async function submitCompletion() {
        // ... ваш код отправки на сервер ...
    }

    // ---------- Кнопки ----------

    function updateSizeButtons() {
        sizeButtons.forEach(btn => {
            const n = parseInt(btn.dataset.sizeBtn, 10);
            const enabled = availableSizes.includes(n);

            btn.style.display = enabled ? "" : "none";
            btn.classList.toggle("active", n === currentSize);
        });
    }

    sizeButtons.forEach(btn => {
        btn.addEventListener("click", () => {
            const n = parseInt(btn.dataset.sizeBtn, 10);
            if (n === currentSize || !availableSizes.includes(n)) return;

            currentSize = n;
            updateSizeButtons();
            startGame();
        });
    });

    // «Играть ещё» — пересобирает текущий размер с новыми парами
    playAgainBtn.addEventListener("click", () => {
        startGame();
    });

    // ---------- Старт ----------

    updateSizeButtons();
    startGame();
}