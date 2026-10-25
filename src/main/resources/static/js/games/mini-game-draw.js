function initializeDrawGame(game) {
    if (!game || game.dataset.drawInitialized === 'true') return;
    game.dataset.drawInitialized = 'true';

    const lobby = game.querySelector('[data-draw-lobby]');
    const play = game.querySelector('[data-draw-play]');
    const status = game.querySelector('[data-draw-status]');
    const playStatus = game.querySelector('[data-draw-play-status]');
    const roleEl = game.querySelector('[data-draw-role]');
    const roomCodeEl = game.querySelector('[data-draw-room-code]');
    const timerEl = game.querySelector('[data-draw-timer]');
    const wordBanner = game.querySelector('[data-draw-word]');
    const wordText = game.querySelector('[data-draw-word-text]');
    const canvas = game.querySelector('[data-draw-canvas]');

    const painterControls = game.querySelector('[data-draw-painter-controls]');
    const guessControls = game.querySelector('[data-draw-guess-controls]');
    const guessInput = game.querySelector('[data-draw-guess-input]');

    const createButton = game.querySelector('[data-draw-create]');
    const joinButton = game.querySelector('[data-draw-join]');
    const codeInput = game.querySelector('[data-draw-code-input]');
    const clearButton = game.querySelector('[data-draw-clear]');
    const eraserButton = game.querySelector('[data-draw-eraser]');
    const pauseButton = game.querySelector('[data-draw-pause]');
    const pauseIcon = game.querySelector('[data-draw-pause-icon]');
    const skipButton = game.querySelector('[data-draw-skip]');
    const guessButton = game.querySelector('[data-draw-guess-submit]');
    const copyButton = game.querySelector('[data-draw-copy]');
    const nextButton = game.querySelector('[data-draw-next]');
    const exitButton = game.querySelector('[data-draw-exit]');

    if (!canvas || !lobby || !play || !status || !playStatus) {
        console.error(
            'Draw and Guess: не найдены обязательные элементы HTML.'
        );
        return;
    }

    const ctx = canvas.getContext('2d');

    if (!ctx) {
        console.error(
            'Draw and Guess: не удалось создать контекст canvas.'
        );
        return;
    }

    // Единое внутреннее разрешение холста.
    const CANVAS_W = 1000;
    const CANVAS_H = 600;

    canvas.width = CANVAS_W;
    canvas.height = CANVAS_H;
    canvas.style.touchAction = 'none';

    let stompClient = null;
    let roomId = null;
    let myRole = null;

    let drawing = false;
    let eraserEnabled = false;
    let gamePaused = false;

    let timerInterval = null;
    let timeLeft = 90;

    let roomSubscription = null;
    let connecting = false;
    let localLastPoint = null;
    let remoteLastPoint = null;
    let subscribedRoomId = null;
    let isRoomCreator = false;

    // --------------------------------------------------
    // CANVAS
    // --------------------------------------------------

    function clearCanvasLocal() {
        ctx.save();
        ctx.globalCompositeOperation = 'source-over';
        ctx.fillStyle = '#ffffff';
        ctx.fillRect(0, 0, CANVAS_W, CANVAS_H);
        ctx.restore();

        localLastPoint = null;
        remoteLastPoint = null;
        drawing = false;
    }

    clearCanvasLocal();

    function getCanvasPoint(e) {
        const rect = canvas.getBoundingClientRect();

        if (!rect.width || !rect.height) {
            return null;
        }

        return {
            x: Math.max(
                0,
                Math.min(1, (e.clientX - rect.left) / rect.width)
            ),
            y: Math.max(
                0,
                Math.min(1, (e.clientY - rect.top) / rect.height)
            )
        };
    }

    function drawSegment(from, to, useEraser = false) {
        ctx.save();

        ctx.globalCompositeOperation = useEraser
            ? 'destination-out'
            : 'source-over';

        ctx.strokeStyle = '#382273';
        ctx.lineWidth = useEraser ? 24 : 4;
        ctx.lineCap = 'round';
        ctx.lineJoin = 'round';

        ctx.beginPath();
        ctx.moveTo(from.x * CANVAS_W, from.y * CANVAS_H);
        ctx.lineTo(to.x * CANVAS_W, to.y * CANVAS_H);
        ctx.stroke();

        ctx.restore();
    }

    // Рисование художником на своём экране.
    function drawLocal(point, isStart) {
        if (!point) return;

        if (isStart || !localLastPoint) {
            localLastPoint = point;
            return;
        }

        drawSegment(localLastPoint, point, eraserEnabled);
        localLastPoint = point;
    }

    // Отображение штрихов, полученных от другого игрока.
    function drawRemote(data) {
        if (gamePaused) return;

        if (
            typeof data.x !== 'number' ||
            typeof data.y !== 'number' ||
            !Number.isFinite(data.x) ||
            !Number.isFinite(data.y)
        ) {
            return;
        }

        const point = {
            x: Math.max(0, Math.min(1, data.x)),
            y: Math.max(0, Math.min(1, data.y))
        };

        if (data.start === true || !remoteLastPoint) {
            remoteLastPoint = point;
            return;
        }

        drawSegment(
            remoteLastPoint,
            point,
            data.eraser === true
        );

        remoteLastPoint = point;
    }

    // --------------------------------------------------
    // TIMER
    // --------------------------------------------------

    function stopTimer() {
        if (timerInterval !== null) {
            clearInterval(timerInterval);
        }

        timerInterval = null;
    }

    function startTimer(seconds) {
        stopTimer();

        timeLeft = Math.max(0, Number(seconds) || 90);

        if (timerEl) {
            timerEl.textContent = Math.ceil(timeLeft);
        }

        if (gamePaused || timeLeft <= 0) {
            return;
        }

        let lastTick = Date.now();

        timerInterval = setInterval(function () {
            if (gamePaused) {
                stopTimer();
                return;
            }

            const now = Date.now();
            const elapsedSeconds = (now - lastTick) / 1000;

            lastTick = now;
            timeLeft = Math.max(0, timeLeft - elapsedSeconds);

            if (timerEl) {
                timerEl.textContent = Math.ceil(timeLeft);
            }

            if (timeLeft <= 0) {
                stopTimer();
                drawing = false;
                localLastPoint = null;
                remoteLastPoint = null;
            }
        }, 200);
    }

    // --------------------------------------------------
    // PAUSE BUTTON UI
    // --------------------------------------------------

    function updatePauseUI() {
        if (pauseButton) {
            pauseButton.disabled = !roomId || !myRole;

            pauseButton.setAttribute(
                'aria-pressed',
                String(gamePaused)
            );

            pauseButton.setAttribute(
                'aria-label',
                gamePaused
                    ? 'Продолжить игру'
                    : 'Поставить игру на паузу'
            );

            pauseButton.title = gamePaused
                ? 'Продолжить'
                : 'Пауза';
        }

        if (pauseIcon) {
            pauseIcon.textContent = gamePaused ? '▶' : 'Ⅱ';
        }
    }

    // --------------------------------------------------
    // ROLE UI
    // --------------------------------------------------

    function updateRoleUI() {
        if (roleEl) {
            roleEl.textContent =
                myRole === 'painter'
                    ? '🎨 Художник'
                    : myRole === 'guesser'
                        ? '🕵️ Угадывающий'
                        : '';
        }

        if (painterControls) {
            painterControls.hidden = myRole !== 'painter';
        }

        if (guessControls) {
            guessControls.hidden = myRole !== 'guesser';
        }

        if (wordBanner) {
            wordBanner.hidden = myRole !== 'painter';
        }

        if (copyButton) {
            copyButton.hidden = !isRoomCreator;
        }

        updatePauseUI();
    }

    // --------------------------------------------------
    // WEBSOCKET CONNECTION
    // --------------------------------------------------

    function connect(onConnected) {
        if (connecting) return;

        if (stompClient && stompClient.connected) {
            onConnected();
            return;
        }

        connecting = true;
        status.textContent = 'Подключаемся к серверу...';

        const socket = new SockJS('/ws-draw');

        stompClient = Stomp.over(socket);
        stompClient.debug = null;

        stompClient.connect(
            {},
            function () {
                connecting = false;

                stompClient.subscribe(
                    '/user/queue/draw',
                    function (msg) {
                        try {
                            handlePersonalMessage(JSON.parse(msg.body));
                        } catch (error) {
                            console.error(
                                'Ошибка личного сообщения:',
                                error
                            );
                        }
                    }
                );

                onConnected();
            },
            function (error) {
                connecting = false;
                status.textContent =
                    'Ошибка подключения. Обнови страницу.';

                console.error('WebSocket connection error:', error);
            }
        );
    }

    // --------------------------------------------------
    // PERSONAL MESSAGES
    // --------------------------------------------------

    function handlePersonalMessage(data) {
        switch (data.type) {
            case 'room_created':
                isRoomCreator = true;
                roomId = data.roomId;
                myRole = data.role;

                enterRoom();
                updateRoleUI();
                break;

            case 'joined':
                isRoomCreator = false;
                roomId = data.roomId;
                myRole = data.role;

                enterRoom();
                updateRoleUI();
                break;

            case 'role_changed':
                myRole = data.role;

                drawing = false;
                localLastPoint = null;
                remoteLastPoint = null;

                if (wordText) {
                    wordText.textContent = '';
                }

                updateRoleUI();
                break;

            case 'word':
                if (
                    myRole === 'painter' &&
                    wordText &&
                    wordBanner
                ) {
                    wordText.textContent = data.word || '';
                    wordBanner.hidden = false;
                }
                break;

            case 'left':
                if (roomId !== null) {
                    resetToLobby('Ты вышел из комнаты.');
                }
                break;

            case 'error':
                status.textContent =
                    data.message || 'Произошла ошибка.';

                playStatus.textContent =
                    data.message || 'Произошла ошибка.';

                // Если присоединение не удалось, возвращаемся в лобби.
                if (lobby && !lobby.hidden && roomId) {
                    roomId = null;
                    subscribedRoomId = null;
                    myRole = null;
                    isRoomCreator = false;
                    gamePaused = false;

                    if (roomSubscription) {
                        roomSubscription.unsubscribe();
                        roomSubscription = null;
                    }

                    updateRoleUI();
                }
                break;
        }
    }

    // --------------------------------------------------
    // ENTER ROOM
    // --------------------------------------------------

    function enterRoom() {
        if (!roomId || !stompClient || !stompClient.connected) {
            status.textContent =
                'Не удалось подключиться к комнате.';
            return;
        }

        lobby.hidden = true;
        play.hidden = false;

        if (roomCodeEl) {
            roomCodeEl.textContent = roomId;
        }

        updateRoleUI();

        // Не подписываемся на одну комнату повторно.
        if (subscribedRoomId !== roomId) {
            if (roomSubscription) {
                roomSubscription.unsubscribe();
            }

            subscribedRoomId = roomId;

            roomSubscription = stompClient.subscribe(
                '/topic/draw/' + roomId,
                function (msg) {
                    try {
                        handleRoomMessage(JSON.parse(msg.body));
                    } catch (error) {
                        console.error(
                            'Ошибка сообщения комнаты:',
                            error
                        );
                    }
                }
            );
        }

        status.textContent = '';
    }

    // --------------------------------------------------
    // ROOM MESSAGES
    // --------------------------------------------------

    function handleRoomMessage(data) {
        switch (data.type) {
            case 'room_state':
                if (data.painterConnected && data.guesserConnected) {
                    playStatus.textContent = 'Оба игрока на месте!';
                } else {
                    playStatus.textContent =
                        'Ожидаем второго игрока...';
                }
                break;

            case 'round_started':
                gamePaused = false;
                eraserEnabled = false;

                if (eraserButton) {
                    eraserButton.setAttribute('aria-pressed', 'false');
                    eraserButton.textContent = '🩹 Ластик';
                }

                if (nextButton) {
                    nextButton.hidden = true;
                    nextButton.disabled = false;
                }

                clearCanvasLocal();
                startTimer(data.duration || 90);

                playStatus.textContent = 'Раунд начался!';
                updateRoleUI();
                break;

            case 'stroke':
                // Художник рисует локально.
                // Угадывающий получает штрихи через WebSocket.
                if (myRole === 'guesser') {
                    drawRemote(data);
                }
                break;

            case 'clear':
                clearCanvasLocal();
                break;

            case 'guess_result':
                if (data.correct) {
                    playStatus.textContent = '✅ Верно!';
                } else if (myRole === 'guesser' && !gamePaused) {
                    playStatus.textContent = '❌ Попробуй ещё';
                }
                break;

            case 'round_paused':
                gamePaused = true;
                stopTimer();

                if (typeof data.remainingSeconds === 'number') {
                    timeLeft = data.remainingSeconds;
                }

                if (timerEl) {
                    timerEl.textContent = Math.ceil(timeLeft);
                }

                drawing = false;
                localLastPoint = null;
                remoteLastPoint = null;

                playStatus.textContent = '⏸ Игра на паузе.';
                updatePauseUI();
                break;

            case 'round_resumed':
                gamePaused = false;

                if (typeof data.remainingSeconds === 'number') {
                    timeLeft = data.remainingSeconds;
                }

                playStatus.textContent = '▶ Игра продолжается!';
                startTimer(timeLeft);
                updatePauseUI();
                break;

            case 'round_won':
                gamePaused = false;
                stopTimer();

                drawing = false;
                localLastPoint = null;
                remoteLastPoint = null;

                playStatus.textContent =
                    '🎉 Угадано! Слово: ' + (data.word || '');

                if (nextButton) {
                    nextButton.hidden = false;
                    nextButton.disabled = false;
                }

                updatePauseUI();
                break;

            case 'round_skipped':
                gamePaused = false;
                stopTimer();

                drawing = false;
                clearCanvasLocal();

                playStatus.textContent =
                    'Слово пропущено: ' + (data.word || '');

                updatePauseUI();
                break;

            case 'player_left':
                gamePaused = false;
                stopTimer();

                drawing = false;
                localLastPoint = null;
                remoteLastPoint = null;

                playStatus.textContent = 'Игрок отключился.';
                updatePauseUI();
                break;

            case 'error':
                playStatus.textContent =
                    data.message || 'Ошибка игры.';
                break;
        }
    }

    // --------------------------------------------------
    // RESET TO LOBBY
    // --------------------------------------------------

    function resetToLobby(message) {
        stopTimer();

        drawing = false;
        gamePaused = false;
        eraserEnabled = false;

        localLastPoint = null;
        remoteLastPoint = null;

        if (roomSubscription) {
            roomSubscription.unsubscribe();
            roomSubscription = null;
        }

        roomId = null;
        myRole = null;
        subscribedRoomId = null;
        isRoomCreator = false;

        if (lobby) lobby.hidden = false;
        if (play) play.hidden = true;

        if (roomCodeEl) {
            roomCodeEl.textContent = '—';
        }

        if (wordText) {
            wordText.textContent = '';
        }

        if (wordBanner) {
            wordBanner.hidden = true;
        }

        if (timerEl) {
            timerEl.textContent = '90';
        }

        timeLeft = 90;

        if (nextButton) {
            nextButton.hidden = true;
            nextButton.disabled = false;
        }

        if (guessInput) {
            guessInput.value = '';
        }

        if (eraserButton) {
            eraserButton.setAttribute('aria-pressed', 'false');
            eraserButton.textContent = '🩹 Ластик';
        }

        clearCanvasLocal();
        updateRoleUI();

        status.textContent =
            message ||
            'Можно создать комнату или присоединиться к другой.';

        playStatus.textContent = '';
    }

    // --------------------------------------------------
    // SEND DRAWING STROKES
    // --------------------------------------------------

    function sendStroke(point, isStart) {
        if (
            gamePaused ||
            myRole !== 'painter' ||
            !roomId ||
            !stompClient ||
            !stompClient.connected
        ) {
            return;
        }

        stompClient.send(
            '/app/draw/' + roomId + '/stroke',
            {},
            JSON.stringify({
                type: 'stroke',
                x: point.x,
                y: point.y,
                start: Boolean(isStart),
                eraser: eraserEnabled
            })
        );
    }

    // --------------------------------------------------
    // CANVAS POINTER EVENTS
    // --------------------------------------------------

    canvas.addEventListener('pointerdown', function (e) {
        if (
            gamePaused ||
            myRole !== 'painter' ||
            !roomId
        ) {
            return;
        }

        e.preventDefault();

        const point = getCanvasPoint(e);
        if (!point) return;

        drawing = true;
        localLastPoint = null;

        if (canvas.setPointerCapture) {
            canvas.setPointerCapture(e.pointerId);
        }

        drawLocal(point, true);
        sendStroke(point, true);
    });

    canvas.addEventListener('pointermove', function (e) {
        if (
            gamePaused ||
            !drawing ||
            myRole !== 'painter'
        ) {
            return;
        }

        e.preventDefault();

        const point = getCanvasPoint(e);
        if (!point) return;

        drawLocal(point, false);
        sendStroke(point, false);
    });

    function finishDrawing(e) {
        drawing = false;
        localLastPoint = null;

        if (
            e &&
            canvas.hasPointerCapture &&
            canvas.hasPointerCapture(e.pointerId)
        ) {
            canvas.releasePointerCapture(e.pointerId);
        }
    }

    canvas.addEventListener('pointerup', finishDrawing);
    canvas.addEventListener('pointercancel', finishDrawing);

    canvas.addEventListener('lostpointercapture', function () {
        drawing = false;
        localLastPoint = null;
    });

    // --------------------------------------------------
    // CREATE ROOM
    // --------------------------------------------------

    if (createButton) {
        createButton.addEventListener('click', function () {
            if (connecting || roomId) return;

            status.textContent = 'Создаём комнату...';

            connect(function () {
                stompClient.send(
                    '/app/draw/create',
                    {},
                    JSON.stringify({})
                );
            });
        });
    }

    // --------------------------------------------------
    // JOIN ROOM
    // --------------------------------------------------

    if (joinButton) {
        joinButton.addEventListener('click', function () {
            const code = codeInput
                ? codeInput.value.trim().toUpperCase()
                : '';

            if (!code) {
                status.textContent = 'Введи код комнаты';
                return;
            }

            if (connecting || roomId) return;

            status.textContent = 'Подключаемся к комнате...';

            connect(function () {
                // Подписываемся до отправки запроса на вход.
                roomId = code;

                if (roomSubscription) {
                    roomSubscription.unsubscribe();
                    roomSubscription = null;
                }

                subscribedRoomId = roomId;

                roomSubscription = stompClient.subscribe(
                    '/topic/draw/' + roomId,
                    function (msg) {
                        try {
                            handleRoomMessage(JSON.parse(msg.body));
                        } catch (error) {
                            console.error(
                                'Ошибка сообщения комнаты:',
                                error
                            );
                        }
                    }
                );

                stompClient.send(
                    '/app/draw/' + roomId + '/join',
                    {},
                    JSON.stringify({})
                );
            });
        });
    }

    // --------------------------------------------------
    // CLEAR CANVAS
    // --------------------------------------------------

    if (clearButton) {
        clearButton.addEventListener('click', function () {
            if (
                gamePaused ||
                myRole !== 'painter' ||
                !roomId ||
                !stompClient ||
                !stompClient.connected
            ) {
                return;
            }

            clearCanvasLocal();

            stompClient.send(
                '/app/draw/' + roomId + '/clear',
                {},
                JSON.stringify({})
            );
        });
    }

    // --------------------------------------------------
    // ERASER
    // --------------------------------------------------

    if (eraserButton) {
        eraserButton.addEventListener('click', function () {
            if (
                gamePaused ||
                myRole !== 'painter' ||
                !roomId
            ) {
                return;
            }

            eraserEnabled = !eraserEnabled;

            eraserButton.setAttribute(
                'aria-pressed',
                String(eraserEnabled)
            );

            eraserButton.textContent = eraserEnabled
                ? '✏️ Карандаш'
                : '🩹 Ластик';

            playStatus.textContent = eraserEnabled
                ? 'Ластик включён.'
                : 'Карандаш включён.';
        });
    }

    // --------------------------------------------------
    // PAUSE / RESUME
    // --------------------------------------------------

    if (pauseButton) {
        pauseButton.addEventListener('click', function () {
            if (
                !roomId ||
                !myRole ||
                !stompClient ||
                !stompClient.connected
            ) {
                return;
            }

            // Состояние меняет сервер и сообщает об этом обоим игрокам.
            // Не переключаем gamePaused локально до ответа сервера.
            stompClient.send(
                '/app/draw/' + roomId + '/pause',
                {},
                JSON.stringify({})
            );
        });
    }

    // --------------------------------------------------
    // SKIP WORD
    // --------------------------------------------------

    if (skipButton) {
        skipButton.addEventListener('click', function () {
            if (
                gamePaused ||
                myRole !== 'painter' ||
                !stompClient ||
                !stompClient.connected ||
                !roomId
            ) {
                return;
            }

            stompClient.send(
                '/app/draw/' + roomId + '/skip',
                {},
                JSON.stringify({})
            );
        });
    }

    // --------------------------------------------------
    // NEXT ROUND
    // --------------------------------------------------

    if (nextButton) {
        nextButton.addEventListener('click', function () {
            if (
                !roomId ||
                !stompClient ||
                !stompClient.connected ||
                nextButton.disabled
            ) {
                return;
            }

            nextButton.disabled = true;
            playStatus.textContent = 'Запускаем следующий раунд...';

            stompClient.send(
                '/app/draw/' + roomId + '/next',
                {},
                JSON.stringify({})
            );
        });
    }

    // --------------------------------------------------
    // COPY ROOM CODE
    // --------------------------------------------------

    if (copyButton) {
        copyButton.addEventListener('click', async function () {
            const code = roomCodeEl?.textContent?.trim();

            if (!code || code === '—') {
                playStatus.textContent =
                    'Код комнаты ещё не получен.';
                return;
            }

            try {
                if (navigator.clipboard && window.isSecureContext) {
                    await navigator.clipboard.writeText(code);
                } else {
                    const temporaryInput =
                        document.createElement('textarea');

                    temporaryInput.value = code;
                    temporaryInput.style.position = 'fixed';
                    temporaryInput.style.opacity = '0';

                    document.body.appendChild(temporaryInput);
                    temporaryInput.select();

                    const copied = document.execCommand('copy');
                    temporaryInput.remove();

                    if (!copied) {
                        throw new Error('Не удалось скопировать код');
                    }
                }

                playStatus.textContent =
                    '✅ Код комнаты скопирован: ' + code;
            } catch (error) {
                console.error('Ошибка копирования кода:', error);

                playStatus.textContent =
                    'Не удалось скопировать. Код комнаты: ' + code;
            }
        });
    }

    // --------------------------------------------------
    // EXIT ROOM
    // --------------------------------------------------

    if (exitButton) {
        exitButton.addEventListener('click', function () {
            if (!roomId) {
                resetToLobby();
                return;
            }

            const leavingRoomId = roomId;

            if (stompClient && stompClient.connected) {
                stompClient.send(
                    '/app/draw/' + leavingRoomId + '/leave',
                    {},
                    JSON.stringify({})
                );
            }

            resetToLobby(
                'Ты вышел из комнаты. Можно создать новую.'
            );
        });
    }

    // --------------------------------------------------
    // GUESS WORD
    // --------------------------------------------------

    if (guessButton) {
        guessButton.addEventListener('click', submitGuess);
    }

    if (guessInput) {
        guessInput.addEventListener('keydown', function (e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                submitGuess();
            }
        });
    }

    function submitGuess() {
        const text = guessInput ? guessInput.value.trim() : '';

        if (
            !text ||
            gamePaused ||
            myRole !== 'guesser' ||
            !stompClient ||
            !stompClient.connected ||
            !roomId
        ) {
            return;
        }

        stompClient.send(
            '/app/draw/' + roomId + '/guess',
            {},
            JSON.stringify({ text: text })
        );

        guessInput.value = '';
    }

    // Начальное состояние кнопок.
    updateRoleUI();
}