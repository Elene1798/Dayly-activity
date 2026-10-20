function initializePuzzleGame(game) {
    if (!game) return;

    const board = game.querySelector('[data-puzzle-board]');
    const movesEl = game.querySelector('[data-puzzle-moves]');
    const timeEl = game.querySelector('[data-puzzle-time]');
    const resultEl = game.querySelector('[data-puzzle-result]');
    const shuffleBtn = game.querySelector('[data-puzzle-shuffle]');
    const nextBtn = game.querySelector('[data-puzzle-next]');
    const sizeButtons = game.querySelectorAll('[data-size-btn]');

    const puzzleId = game.dataset.puzzleId;
    const imageUrl = game.dataset.image;
    const miniGameId = game.closest('[data-mini-game-id]')?.dataset.miniGameId;

    let size = parseInt(game.dataset.size, 10) || 3;

    if (!board || !imageUrl) {
        console.error('Puzzle: нет board или image');
        return;
    }

    let total = size * size;

    let tiles = [];
    let moves = 0;
    let seconds = 0;
    let timer = null;
    let started = false;
    let dragged = null;

    // ---------- Показать картинку ----------

    const previewBtn = game.querySelector('[data-puzzle-preview]');
    const previewModal = game.querySelector('[data-puzzle-preview-modal]');
    const previewCloseBtns = game.querySelectorAll('[data-puzzle-preview-close]');

    if (previewBtn && previewModal) {
        previewBtn.addEventListener('click', () => {
            previewModal.hidden = false;
        });

        previewCloseBtns.forEach(btn => {
            btn.addEventListener('click', () => {
                previewModal.hidden = true;
            });
        });

        // Escape для закрытия
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && !previewModal.hidden) {
                previewModal.hidden = true;
            }
        });
    }

    // ---------- Построение поля ----------

    function buildBoard() {
        board.style.gridTemplateColumns = `repeat(${size}, 1fr)`;
        board.innerHTML = '';
        total = size * size;

        for (let i = 0; i < total; i++) {
            const cell = document.createElement('div');
            cell.className = 'puzzle-cell';
            cell.dataset.position = i;

            const tile = document.createElement('div');
            tile.className = 'puzzle-tile';
            tile.draggable = true;
            tile.dataset.position = i;

            cell.appendChild(tile);
            attachTileEvents(tile);

            board.appendChild(cell);
        }
    }

    function attachTileEvents(tile) {
        tile.addEventListener('dragstart', (e) => {
            if (!started) startTimer();
            dragged = parseInt(tile.dataset.position, 10);
            tile.classList.add('puzzle-tile--dragging');
            e.dataTransfer.effectAllowed = 'move';
            e.dataTransfer.setData('text/plain', String(dragged));
        });

        tile.addEventListener('dragend', () => {
            tile.classList.remove('puzzle-tile--dragging');
            board.querySelectorAll('.puzzle-cell').forEach(c =>
                c.classList.remove('puzzle-cell--hover')
            );
        });

        attachTouchEvents(tile);
    }

    function renderTiles() {
        const cells = board.querySelectorAll('.puzzle-cell');

        cells.forEach((cell, position) => {
            const pieceIndex = tiles[position];
            const tile = cell.querySelector('.puzzle-tile');

            tile.dataset.position = position;
            tile.dataset.piece = pieceIndex;

            const row = Math.floor(pieceIndex / size);
            const col = pieceIndex % size;

            const xPercent = (col / (size - 1)) * 100;
            const yPercent = (row / (size - 1)) * 100;

            tile.style.backgroundImage = `url('${imageUrl}')`;
            tile.style.backgroundSize = `${size * 100}% ${size * 100}%`;
            tile.style.backgroundPosition = `${xPercent}% ${yPercent}%`;
        });
    }

    // ---------- Swap ----------

    function swap(fromPos, toPos) {
        if (fromPos === toPos) return;

        [tiles[fromPos], tiles[toPos]] = [tiles[toPos], tiles[fromPos]];

        moves++;
        movesEl.textContent = moves;

        renderTiles();

        if (isSolved()) finishGame();
    }

    function isSolved() {
        for (let i = 0; i < total; i++) {
            if (tiles[i] !== i) return false;
        }
        return true;
    }

    // ---------- Drag & drop ----------

    board.addEventListener('dragover', (e) => {
        const cell = e.target.closest('.puzzle-cell');
        if (!cell) return;
        e.preventDefault();
        board.querySelectorAll('.puzzle-cell').forEach(c =>
            c.classList.remove('puzzle-cell--hover')
        );
        cell.classList.add('puzzle-cell--hover');
    });

    board.addEventListener('dragleave', (e) => {
        const cell = e.target.closest('.puzzle-cell');
        if (cell) cell.classList.remove('puzzle-cell--hover');
    });

    board.addEventListener('drop', (e) => {
        const cell = e.target.closest('.puzzle-cell');
        if (!cell) return;
        e.preventDefault();

        const toPos = parseInt(cell.dataset.position, 10);
        if (dragged !== null && dragged !== toPos) {
            swap(dragged, toPos);
        }
        dragged = null;
        board.querySelectorAll('.puzzle-cell').forEach(c =>
            c.classList.remove('puzzle-cell--hover')
        );
    });

    // ---------- Touch ----------

    let touchTile = null;
    let touchStartX = 0;
    let touchStartY = 0;
    let touchClone = null;
    let touchFromPos = null;

    function attachTouchEvents(tile) {
        tile.addEventListener('touchstart', (e) => {
            if (e.touches.length !== 1) return;
            e.preventDefault();
            if (!started) startTimer();

            const touch = e.touches[0];
            touchTile = tile;
            touchFromPos = parseInt(tile.dataset.position, 10);
            touchStartX = touch.clientX;
            touchStartY = touch.clientY;

            const rect = tile.getBoundingClientRect();
            touchClone = tile.cloneNode(true);
            touchClone.classList.add('puzzle-tile--touch-clone');
            touchClone.style.position = 'fixed';
            touchClone.style.left = rect.left + 'px';
            touchClone.style.top = rect.top + 'px';
            touchClone.style.width = rect.width + 'px';
            touchClone.style.height = rect.height + 'px';
            touchClone.style.pointerEvents = 'none';
            touchClone.style.zIndex = '9999';
            touchClone.style.opacity = '0.85';
            document.body.appendChild(touchClone);

            tile.style.opacity = '0.3';
        }, { passive: false });

        tile.addEventListener('touchmove', (e) => {
            if (!touchTile || !touchClone) return;
            e.preventDefault();

            const touch = e.touches[0];
            const dx = touch.clientX - touchStartX;
            const dy = touch.clientY - touchStartY;

            const rect = touchTile.getBoundingClientRect();
            touchClone.style.left = (rect.left + dx) + 'px';
            touchClone.style.top = (rect.top + dy) + 'px';

            const el = document.elementFromPoint(touch.clientX, touch.clientY);
            const cell = el ? el.closest('.puzzle-cell') : null;
            board.querySelectorAll('.puzzle-cell').forEach(c =>
                c.classList.remove('puzzle-cell--hover')
            );
            if (cell) cell.classList.add('puzzle-cell--hover');
        }, { passive: false });

        tile.addEventListener('touchend', (e) => {
            if (!touchTile) return;
            e.preventDefault();

            const touch = e.changedTouches[0];
            const el = document.elementFromPoint(touch.clientX, touch.clientY);
            const cell = el ? el.closest('.puzzle-cell') : null;

            if (cell) {
                const toPos = parseInt(cell.dataset.position, 10);
                if (touchFromPos !== null && toPos !== touchFromPos) {
                    swap(touchFromPos, toPos);
                }
            }

            if (touchClone) { touchClone.remove(); touchClone = null; }
            touchTile.style.opacity = '';
            touchTile = null;
            touchFromPos = null;
            board.querySelectorAll('.puzzle-cell').forEach(c =>
                c.classList.remove('puzzle-cell--hover')
            );
        }, { passive: false });
    }

    // ---------- Перемешать ----------

    function shuffle() {
        do {
            for (let i = tiles.length - 1; i > 0; i--) {
                const j = Math.floor(Math.random() * (i + 1));
                [tiles[i], tiles[j]] = [tiles[j], tiles[i]];
            }
        } while (isSolved());
    }

    // ---------- Таймер ----------

    function startTimer() {
        if (started) return;
        started = true;
        timer = setInterval(() => {
            seconds++;
            const m = Math.floor(seconds / 60);
            const s = seconds % 60;
            timeEl.textContent = `${m}:${String(s).padStart(2, '0')}`;
        }, 1000);
    }

    function stopTimer() {
        if (timer) { clearInterval(timer); timer = null; }
    }

    function resetState() {
        stopTimer();
        seconds = 0;
        timeEl.textContent = '0:00';
        moves = 0;
        movesEl.textContent = 0;
        started = false;
        resultEl.textContent = '';
        resultEl.className = 'puzzle-result';
        board.style.pointerEvents = '';
    }

    // ---------- Победа ----------

    function finishGame() {
        stopTimer();
        resultEl.textContent = `🎉 Собрано! Ходов: ${moves}, время: ${timeEl.textContent}`;
        resultEl.className = 'puzzle-result success';
        board.style.pointerEvents = 'none';
    }

    // ---------- Кнопки сложности ----------

    function updateSizeButtons() {
        sizeButtons.forEach(btn => {
            const s = parseInt(btn.dataset.sizeBtn, 10);
            btn.classList.toggle('active', s === size);
        });
    }

    sizeButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const newSize = parseInt(btn.dataset.sizeBtn, 10);
            if (newSize === size) return;

            size = newSize;
            total = size * size;

            resetState();
            buildBoard();
            tiles = Array.from({ length: total }, (_, i) => i);
            shuffle();
            renderTiles();
            updateSizeButtons();
        });
    });

    // ---------- Перемешать ----------

    shuffleBtn.addEventListener('click', () => {
        resetState();
        buildBoard();
        tiles = Array.from({ length: total }, (_, i) => i);
        shuffle();
        renderTiles();
    });

    // ---------- Следующий пазл ----------

    nextBtn.addEventListener('click', () => {
        if (!miniGameId) {
            console.warn('Не найден miniGameId');
            return;
        }

        nextBtn.disabled = true;
        nextBtn.textContent = 'Загрузка...';

        const url = `/mini-game/${miniGameId}/puzzle/next?currentPuzzleId=${puzzleId}&size=${size}`;

        fetch(url, { credentials: 'same-origin' })
            .then(r => {
                if (!r.ok) throw new Error('Не удалось загрузить следующий пазл');
                return r.text();
            })
            .then(html => {
                // Заменяем содержимое всего блока
                const wrapper = game.parentElement;
                if (wrapper) {
                    wrapper.innerHTML = html;

                    const newGame = wrapper.querySelector('[data-puzzle-game]');
                    if (newGame) {
                        initializePuzzleGame(newGame);
                    }
                }
            })
            .catch(err => {
                console.error(err);
                alert('Не удалось загрузить следующий пазл');
                nextBtn.disabled = false;
                nextBtn.textContent = '⏭ Следующий пазл';
            });
    });

    // ---------- Старт ----------

    buildBoard();
    tiles = Array.from({ length: total }, (_, i) => i);
    shuffle();
    renderTiles();
    updateSizeButtons();
}