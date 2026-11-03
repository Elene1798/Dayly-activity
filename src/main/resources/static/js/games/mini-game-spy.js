
function initializeSpyGame(game) {
    if (!game) return;

    const lobby = game.querySelector('[data-spy-lobby]');
    const play = game.querySelector('[data-spy-play]');
    const status = game.querySelector('[data-spy-status]');
    const playStatus = game.querySelector('[data-spy-play-status]');
    const roleEl = game.querySelector('[data-spy-role]');
    const roomCodeEl = game.querySelector('[data-spy-room-code]');
    const timerEl = game.querySelector('[data-spy-timer]');
    const playersListEl = game.querySelector('[data-spy-players-list]');
    const cardEl = game.querySelector('[data-spy-card]');
    const cardRoleEl = game.querySelector('[data-spy-card-role]');
    const cardLocationEl = game.querySelector('[data-spy-card-location]');
    const startControls = game.querySelector('[data-spy-start-controls]');
    const copyCodeButton = game.querySelector('[data-spy-copy-code]');
    const copyFeedback = game.querySelector('[data-spy-copy-feedback]');

    let stompClient = null;
    let roomId = null;
    let myRole = null;
    let isHost = false;
    let timerInterval = null;
    let currentPlayerId = null;
    let roomSubscription = null;

    // =====================================
    // PLAYER ID
    // =====================================

    function createPlayerId() {
        if (window.crypto && crypto.randomUUID) {
            return crypto.randomUUID();
        }

        return 'player-' + Date.now() + '-' +
            Math.random().toString(36).substring(2, 12);
    }

    function getPlayerId(roomCode) {
        const key = 'spyPlayerId_' + roomCode;
        let playerId = localStorage.getItem(key);

        if (!playerId) {
            playerId = createPlayerId();
            localStorage.setItem(key, playerId);
        }

        return playerId;
    }

    // =====================================
    // COPY ROOM CODE
    // =====================================

    if (copyCodeButton) {
        copyCodeButton.addEventListener('click', async function () {
            const code = roomCodeEl.textContent.trim();

            if (!code || code === '—') return;

            try {
                if (navigator.clipboard && window.isSecureContext) {
                    await navigator.clipboard.writeText(code);
                } else {
                    const temporaryInput = document.createElement('textarea');
                    temporaryInput.value = code;
                    temporaryInput.setAttribute('readonly', '');
                    temporaryInput.style.position = 'fixed';
                    temporaryInput.style.left = '-9999px';

                    document.body.appendChild(temporaryInput);
                    temporaryInput.select();

                    const copied = document.execCommand('copy');
                    temporaryInput.remove();

                    if (!copied) {
                        throw new Error('Не удалось скопировать код');
                    }
                }

                if (copyFeedback) {
                    copyFeedback.textContent = '✓ Код скопирован!';
                }

                copyCodeButton.classList.add('copied');

            } catch (error) {
                console.error('Ошибка копирования:', error);

                if (copyFeedback) {
                    copyFeedback.textContent = 'Не удалось скопировать код';
                }
            }

            setTimeout(function () {
                if (copyFeedback) {
                    copyFeedback.textContent = '';
                }

                copyCodeButton.classList.remove('copied');
            }, 2500);
        });
    }

    // =====================================
    // WEBSOCKET CONNECTION
    // =====================================

    function connect(onConnected) {
        if (stompClient && stompClient.connected) {
            if (onConnected) onConnected();
            return;
        }

        const socket = new SockJS('/ws-draw');
        stompClient = Stomp.over(socket);
        stompClient.debug = null;

        stompClient.connect({}, function () {
            stompClient.subscribe('/user/queue/spy', function (msg) {
                handlePersonalMessage(JSON.parse(msg.body));
            });

            if (onConnected) onConnected();

        }, function (error) {
            status.textContent = 'Ошибка подключения. Обнови страницу.';
            console.error('SPY CONNECTION ERROR:', error);
        });
    }

    // =====================================
    // PERSONAL MESSAGES
    // =====================================

    function handlePersonalMessage(data) {
        console.log('SPY PERSONAL MESSAGE:', data);

        switch (data.type) {

            case 'room_created':
                roomId = data.roomId;
                currentPlayerId = data.playerId;
                isHost = true;

                localStorage.setItem(
                    'spyPlayerId_' + roomId,
                    currentPlayerId
                );

                enterRoom(data);
                break;

            case 'joined':
                roomId = data.roomId;
                currentPlayerId = data.playerId;
                isHost = data.isHost;

                localStorage.setItem(
                    'spyPlayerId_' + roomId,
                    currentPlayerId
                );

                enterRoom(data);
                break;

            case 'role_assigned':
                myRole = data.isSpy ? 'ШПИОН' : 'Мирный житель';

                roleEl.textContent = myRole;
                cardEl.hidden = false;

                cardRoleEl.textContent = data.isSpy
                    ? '🕵️ ВЫ ШПИОН'
                    : '📍 ВЫ ЗНАЕТЕ ЛОКАЦИЮ';

                const cardSymbol =
                    game.querySelector('[data-spy-card-symbol]');

                const cardHint =
                    game.querySelector('[data-spy-card-hint]');

                if (cardSymbol) {
                    cardSymbol.textContent = data.isSpy ? '🕵️' : '🌍';
                }

                if (cardHint) {
                    cardHint.textContent = data.isSpy
                        ? 'Внимательно слушай других игроков и постарайся вычислить секретную локацию!'
                        : 'Не выдавай локацию слишком очевидно. Попробуй вычислить шпиона!';
                }

                cardLocationEl.textContent = data.isSpy
                    ? 'Попробуйте понять, где вы'
                    : data.location;

                if (data.isHost !== undefined) {
                    isHost = data.isHost;
                    startControls.hidden = !isHost;
                }

                startTimer(data.timerEndTime);
                break;

            case 'error':
                status.textContent = data.message;
                break;
        }
    }

    // =====================================
    // ENTER ROOM
    // =====================================

    function enterRoom(data) {
        lobby.hidden = true;
        play.hidden = false;

        roomCodeEl.textContent = roomId;

        if (roomSubscription) {
            roomSubscription.unsubscribe();
            roomSubscription = null;
        }

        roomSubscription = stompClient.subscribe(
            '/topic/spy/' + roomId,
            function (msg) {
                handleRoomMessage(JSON.parse(msg.body));
            }
        );

        startControls.hidden = !isHost;

        if (isHost) {
            playStatus.textContent = 'Ты создатель комнаты 👑';
        } else {
            playStatus.textContent = 'Ожидаем начала игры...';
        }

        if (data && data.players) {
            updatePlayers(data.players);
        }

        cardEl.hidden = true;
        roleEl.textContent = 'Ожидание...';
    }

    // =====================================
    // UPDATE PLAYERS
    // =====================================

    function updatePlayers(players) {
        playersListEl.textContent = players.map(player => {
            let label = player.name;

            if (!player.online) {
                label += ' (не в сети)';
            }

            if (player.isHost) {
                label += ' 👑';
            }

            return label;
        }).join(', ') || 'Никого нет';

        const onlineCount = players.filter(
            player => player.online
        ).length;

        playStatus.textContent =
            'Игроков в комнате: ' + onlineCount;

        const currentPlayer = players.find(
            player => player.playerId === currentPlayerId
        );

        if (currentPlayer) {
            isHost = currentPlayer.isHost;
            startControls.hidden = !isHost;
        }
    }

    // =====================================
    // ROOM MESSAGES
    // =====================================

    function handleRoomMessage(data) {
        console.log('SPY ROOM MESSAGE:', data);

        if (
            data.type === 'player_joined' ||
            data.type === 'player_left' ||
            data.type === 'room_state' ||
            data.type === 'game_started'
        ) {
            if (data.players) {
                updatePlayers(data.players);
            }

            if (data.gameStarted === true) {
                playStatus.textContent = 'Игра началась!';
            }
        }
    }

    // =====================================
    // TIMER
    // =====================================

    function startTimer(endTime) {
        stopTimer();

        if (!endTime) return;

        function update() {
            const left = Math.max(
                0,
                Math.floor((endTime - Date.now()) / 1000)
            );

            timerEl.textContent = left;

            if (left <= 0) {
                stopTimer();
                playStatus.textContent = 'Время вышло!';
            }
        }

        update();
        timerInterval = setInterval(update, 1000);
    }

    function stopTimer() {
        if (timerInterval) {
            clearInterval(timerInterval);
        }

        timerInterval = null;
    }

    // =====================================
    // CREATE ROOM
    // =====================================

    game.querySelector('[data-spy-create]')
        .addEventListener('click', function () {

            const name = game.querySelector('[data-spy-name]')
                .value.trim();

            if (!name) {
                status.textContent = 'Введите имя';
                return;
            }

            status.textContent = 'Создаём комнату...';

            connect(function () {
                const playerId = createPlayerId();

                stompClient.send(
                    '/app/spy/create',
                    {},
                    JSON.stringify({
                        name: name,
                        playerId: playerId
                    })
                );
            });
        });

    // =====================================
    // JOIN / REJOIN ROOM
    // =====================================

    game.querySelector('[data-spy-join]')
        .addEventListener('click', function () {

            const name = game.querySelector('[data-spy-join-name]')
                .value.trim();

            const code = game.querySelector('[data-spy-code-input]')
                .value.trim().toUpperCase();

            if (!name) {
                status.textContent = 'Введи своё имя';
                game.querySelector('[data-spy-join-name]').focus();
                return;
            }

            if (!code) {
                status.textContent = 'Введи код комнаты';
                game.querySelector('[data-spy-code-input]').focus();
                return;
            }

            status.textContent = 'Подключаемся...';

            connect(function () {
                currentPlayerId = getPlayerId(code);

                stompClient.send(
                    '/app/spy/' + code + '/join',
                    {},
                    JSON.stringify({
                        name: name,
                        playerId: currentPlayerId
                    })
                );
            });
        });

    // =====================================
    // START GAME
    // =====================================

    game.querySelector('[data-spy-start]')
        .addEventListener('click', function () {

            if (!isHost) {
                playStatus.textContent =
                    'Только создатель может начать игру';
                return;
            }

            if (stompClient && stompClient.connected && roomId) {
                stompClient.send(
                    '/app/spy/' + roomId + '/start',
                    {},
                    JSON.stringify({})
                );
            }
        });

    // =====================================
    // EXIT ROOM
    // =====================================

    game.querySelector('[data-spy-exit]')
        .addEventListener('click', function () {

            if (stompClient && stompClient.connected && roomId) {
                stompClient.send(
                    '/app/spy/' + roomId + '/leave',
                    {},
                    JSON.stringify({})
                );

                stompClient.disconnect();
            }

            if (roomSubscription) {
                roomSubscription = null;
            }

            stopTimer();

            lobby.hidden = false;
            play.hidden = true;
            cardEl.hidden = true;

            roomId = null;
            myRole = null;
            isHost = false;
            currentPlayerId = null;

            playersListEl.textContent = 'Никого нет';
            roleEl.textContent = 'Ожидание...';
            roomCodeEl.textContent = '—';
            timerEl.textContent = '180';

            startControls.hidden = true;

            if (copyFeedback) {
                copyFeedback.textContent = '';
            }

            status.textContent = 'Ты вышел из комнаты';

            // ВАЖНО:
            // playerId в localStorage не удаляем.
            // Благодаря этому можно вернуться в прежнюю комнату.
        });
}

// =====================================
// AUTO INITIALIZATION
// =====================================

document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('[data-spy-game]').forEach(function (game) {
        initializeSpyGame(game);
    });
});
