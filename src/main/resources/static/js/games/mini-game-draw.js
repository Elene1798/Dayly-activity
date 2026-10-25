
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
    const skipButton = game.querySelector('[data-draw-skip]');
    const guessButton = game.querySelector('[data-draw-guess-submit]');
    const copyButton = game.querySelector('[data-draw-copy]');
    const nextButton = game.querySelector('[data-draw-next]');
    const exitButton = game.querySelector('[data-draw-exit]');

    if (copyButton) {
        copyButton.addEventListener('click', async function () {
            const code = roomCodeEl?.textContent?.trim();

            if (!code || code === '—') {
                playStatus.textContent = 'Код комнаты ещё не получен.';
                return;
            }

            try {
                if (navigator.clipboard && window.isSecureContext) {
                    await navigator.clipboard.writeText(code);
                } else {
                    // Запасной вариант для браузеров без Clipboard API.
                    const temporaryInput = document.createElement('textarea');
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

                playStatus.textContent = '✅ Код комнаты скопирован: ' + code;
            } catch (error) {
                console.error('Ошибка копирования кода:', error);
                playStatus.textContent =
                    'Не удалось скопировать. Код комнаты: ' + code;
            }
        });
    }

    if (!canvas || !lobby || !play || !status || !playStatus) {
        console.error('Draw and Guess: не найдены обязательные элементы HTML.');
        return;
    }

    const ctx = canvas.getContext('2d');

    if (!ctx) {
        console.error('Draw and Guess: не удалось создать контекст canvas.');
        return;
    }

    // Единое внутреннее разрешение для обоих смартфонов.
    const CANVAS_W = 1000;
    const CANVAS_H = 600;

    canvas.width = CANVAS_W;
    canvas.height = CANVAS_H;
    canvas.style.touchAction = 'none';

    let stompClient = null;
    let roomId = null;
    let myRole = null;
    let drawing = false;
    let timerInterval = null;
    let timeLeft = 90;
    let roomSubscription = null;
    let connecting = false;
    let localLastPoint = null;
    let remoteLastPoint = null;
    let subscribedRoomId = null;
    let isRoomCreator = false;

    function clearCanvasLocal() {
        ctx.save();
        ctx.fillStyle = '#ffffff';
        ctx.fillRect(0, 0, CANVAS_W, CANVAS_H);
        ctx.restore();

        localLastPoint = null;
        remoteLastPoint = null;
        drawing = false;
    }

    clearCanvasLocal();

    // Возвращает нормализованные координаты от 0 до 1.
    function getCanvasPoint(e) {
        const rect = canvas.getBoundingClientRect();

        if (!rect.width || !rect.height) {
            return null;
        }

        const x = (e.clientX - rect.left) / rect.width;
        const y = (e.clientY - rect.top) / rect.height;

        return {
            x: Math.max(0, Math.min(1, x)),
            y: Math.max(0, Math.min(1, y))
        };
    }

    function drawSegment(from, to) {
        ctx.save();
        ctx.strokeStyle = '#382273';
        ctx.lineWidth = 4;
        ctx.lineCap = 'round';
        ctx.lineJoin = 'round';

        ctx.beginPath();
        ctx.moveTo(from.x * CANVAS_W, from.y * CANVAS_H);
        ctx.lineTo(to.x * CANVAS_W, to.y * CANVAS_H);
        ctx.stroke();

        ctx.restore();
    }

    // Локальный рисунок художника.
    function drawLocal(point, isStart) {
        if (!point) return;

        if (isStart || !localLastPoint) {
            localLastPoint = point;
            return;
        }

        drawSegment(localLastPoint, point);
        localLastPoint = point;
    }

    // Рисунок, полученный от другого игрока.
    function drawRemote(data) {
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

        drawSegment(remoteLastPoint, point);
        remoteLastPoint = point;
    }

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

                stompClient.subscribe('/user/queue/draw', function (msg) {
                    try {
                        handlePersonalMessage(JSON.parse(msg.body));
                    } catch (error) {
                        console.error('Ошибка личного сообщения:', error);
                    }
                });

                onConnected();
            },
            function (error) {
                connecting = false;
                status.textContent = 'Ошибка подключения. Обнови страницу.';
                console.error('WebSocket connection error:', error);
            }
        );
    }

    function updateRoleUI() {
        if (roleEl) {
            roleEl.textContent =
                myRole === 'painter' ? '🎨 Художник' : '🕵️ Угадывающий';
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
    }

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
                updateRoleUI();

                // При смене роли очищаем прежнее слово и сбрасываем рисование.
                if (wordText) {
                    wordText.textContent = '';
                }

                if (wordBanner) {
                    wordBanner.hidden = myRole !== 'painter';
                }

                drawing = false;
                localLastPoint = null;
                remoteLastPoint = null;
                break;

            case 'word':
                if (myRole === 'painter' && wordText && wordBanner) {
                    wordText.textContent = data.word || '';
                    wordBanner.hidden = false;
                }
                break;

            case 'left':
                // Если мы уже вернулись в лобби по нажатию кнопки,
                // повторно сбрасывать состояние не нужно.
                if (roomId !== null) {
                    resetToLobby('Ты вышел из комнаты.');
                }
                break;

            case 'error':
                status.textContent = data.message || 'Произошла ошибка.';
                playStatus.textContent = data.message || 'Произошла ошибка.';

                // Если вход в комнату не состоялся, разрешаем повторную попытку.
                if (lobby && !lobby.hidden && roomId) {
                    roomId = null;
                    subscribedRoomId = null;
                    myRole = null;
                    isRoomCreator = false;

                    if (roomSubscription) {
                        roomSubscription.unsubscribe();
                        roomSubscription = null;
                    }

                    updateRoleUI();
                }
                break;


        }


    }

    function enterRoom() {
        if (!roomId || !stompClient || !stompClient.connected) {
            status.textContent = 'Не удалось подключиться к комнате.';
            return;
        }

        lobby.hidden = true;
        play.hidden = false;

        // Показываем код комнаты.
        if (roomCodeEl) {
            roomCodeEl.textContent = roomId;
        }

        // Показываем текущую роль.
        updateRoleUI();

        // Подписываемся на комнату только один раз.
        // Здесь нельзя повторно отправлять запрос /join.
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
                        console.error('Ошибка сообщения комнаты:', error);
                    }
                }
            );
        }

        status.textContent = '';
    }

    function handleRoomMessage(data) {
        switch (data.type) {
            case 'room_state':
                if (data.painterConnected && data.guesserConnected) {
                    playStatus.textContent = 'Оба игрока на месте!';
                } else {
                    playStatus.textContent = 'Ожидаем второго игрока...';
                }
                break;

            case 'round_started':
                playStatus.textContent = 'Раунд начался!';
                if (nextButton) {
                    nextButton.hidden = true;
                    nextButton.disabled = false;
                }

                clearCanvasLocal();
                startTimer(data.duration || 90);
                updateRoleUI();
                break;

            case 'stroke':
                // Художник уже рисует локально.
                // Повторно свой же рисунок не отрисовываем.
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
                } else if (myRole === 'guesser') {
                    playStatus.textContent = '❌ Не то. Попробуй ещё.';
                }
                break;

            case 'round_won':
                playStatus.textContent = '🎉 Угадано! Слово: ' + data.word;
                stopTimer();
                drawing = false;

                if (nextButton) {
                    nextButton.hidden = false;
                    nextButton.disabled = false;
                }

                break;

            case 'round_skipped':
                playStatus.textContent = 'Слово пропущено: ' + data.word;
                stopTimer();
                clearCanvasLocal();
                break;

            case 'player_left':
                playStatus.textContent = 'Игрок отключился.';
                stopTimer();
                drawing = false;
                break;

            case 'error':
                playStatus.textContent = data.message || 'Ошибка игры.';
                break;
        }
    }

    function startTimer(seconds) {
        stopTimer();

        timeLeft = seconds;
        if (timerEl) {
            timerEl.textContent = timeLeft;
        }

        timerInterval = setInterval(function () {
            timeLeft--;
            if (timerEl) {
                timerEl.textContent = Math.max(0, timeLeft);
            }

            if (timeLeft <= 0) {
                stopTimer();
                drawing = false;
            }
        }, 1000);
    }

    function stopTimer() {
        if (timerInterval !== null) {
            clearInterval(timerInterval);
        }

        timerInterval = null;
    }


    function resetToLobby(message) {
        stopTimer();

        drawing = false;
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

        if (roomCodeEl) roomCodeEl.textContent = '—';
        if (wordText) wordText.textContent = '';
        if (wordBanner) wordBanner.hidden = true;

        if (timerEl) timerEl.textContent = '90';

        if (nextButton) {
            nextButton.hidden = true;
            nextButton.disabled = false;
        }

        if (guessInput) guessInput.value = '';

        clearCanvasLocal();
        updateRoleUI();

        status.textContent = message || 'Можно создать комнату или присоединиться к другой.';
        playStatus.textContent = '';
    }


    function sendStroke(point, isStart) {
        if (
            myRole !== 'painter' ||
            !roomId ||
            !stompClient ||
            !stompClient.connected
        ) {
            return;
        }

        // ВАЖНО: сервер пересылает объект без изменения,
        // поэтому поле type обязательно для клиентского обработчика.
        stompClient.send(
            '/app/draw/' + roomId + '/stroke',
            {},
            JSON.stringify({
                type: 'stroke',
                x: point.x,
                y: point.y,
                start: Boolean(isStart)
            })
        );
    }

    canvas.addEventListener('pointerdown', function (e) {
        if (myRole !== 'painter' || !roomId) return;

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
        if (!drawing || myRole !== 'painter') return;

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

    joinButton.addEventListener('click', function () {
        const code = codeInput.value.trim().toUpperCase();

        if (!code) {
            status.textContent = 'Введи код комнаты';
            return;
        }

        if (connecting || roomId) return;

        status.textContent = 'Подключаемся к комнате...';

        connect(function () {
            // Сначала подписываемся на комнату,
            // чтобы не пропустить сообщения о начале раунда.
            roomId = code;

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
                        console.error('Ошибка сообщения комнаты:', error);
                    }
                }
            );

            // Только один запрос на вход.
            stompClient.send(
                '/app/draw/' + roomId + '/join',
                {},
                JSON.stringify({})
            );
        });
    });

    if (clearButton) {
        clearButton.addEventListener('click', function () {
            if (myRole !== 'painter') return;

            clearCanvasLocal();

            if (stompClient && stompClient.connected && roomId) {
                stompClient.send(
                    '/app/draw/' + roomId + '/clear',
                    {},
                    JSON.stringify({})
                );
            }
        });
    }

    if (skipButton) {
        skipButton.addEventListener('click', function () {
            if (
                myRole === 'painter' &&
                stompClient &&
                stompClient.connected &&
                roomId
            ) {
                stompClient.send(
                    '/app/draw/' + roomId + '/skip',
                    {},
                    JSON.stringify({})
                );
            }
        });
    }

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


    if (exitButton) {
        exitButton.addEventListener('click', function () {
            if (!roomId) {
                resetToLobby();
                return;
            }

            const leavingRoomId = roomId;

            // Сначала сообщаем серверу, что игрок выходит.
            if (stompClient && stompClient.connected) {
                stompClient.send(
                    '/app/draw/' + leavingRoomId + '/leave',
                    {},
                    JSON.stringify({})
                );
            }

            // Возвращаем игрока в лобби без перезагрузки страницы.
            resetToLobby('Ты вышел из комнаты. Можно создать новую.');
        });
    }


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
        const text = guessInput.value.trim();

        if (
            !text ||
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
}
