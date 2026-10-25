
package com.example.dailyactivity;

import com.example.dailyactivity.model.DrawRoom;
import com.example.dailyactivity.model.MiniGame;
import com.example.dailyactivity.model.MiniGameTask;
import com.example.dailyactivity.service.DrawRoomService;
import com.example.dailyactivity.service.MiniGameService;
import com.example.dailyactivity.service.MiniGameTaskService;
import jakarta.annotation.PreDestroy;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Controller
public class DrawGameController {

    private static final int ROUND_DURATION_SECONDS = 90;

    private final SimpMessagingTemplate messagingTemplate;
    private final DrawRoomService roomService;
    private final MiniGameService miniGameService;
    private final MiniGameTaskService taskService;
    private final Random random = new Random();

    // Таймеры выполняются на сервере, независимо от браузеров.
    private final ScheduledExecutorService roundScheduler =
            Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread thread = new Thread(
                        runnable,
                        "draw-game-round-timer"
                );
                thread.setDaemon(true);
                return thread;
            });

    public DrawGameController(
            SimpMessagingTemplate messagingTemplate,
            DrawRoomService roomService,
            MiniGameService miniGameService,
            MiniGameTaskService taskService) {

        this.messagingTemplate = messagingTemplate;
        this.roomService = roomService;
        this.miniGameService = miniGameService;
        this.taskService = taskService;
    }

    @PreDestroy
    public void shutdownScheduler() {
        roundScheduler.shutdownNow();
    }

    private String getSessionId(
            SimpMessageHeaderAccessor accessor) {
        return accessor.getSessionId();
    }

    private void sendToSession(
            String sessionId,
            Map<String, ?> payload) {

        if (sessionId == null || sessionId.isBlank()) {
            return;
        }

        SimpMessageHeaderAccessor accessor =
                SimpMessageHeaderAccessor.create(
                        SimpMessageType.MESSAGE
                );

        accessor.setSessionId(sessionId);
        accessor.setLeaveMutable(true);

        MessageHeaders headers = accessor.getMessageHeaders();

        messagingTemplate.convertAndSendToUser(
                sessionId,
                "/queue/draw",
                (Object) payload,
                headers
        );
    }

    private void sendRoomMessage(
            DrawRoom room,
            Map<String, Object> payload) {

        if (room == null || room.getRoomId() == null) {
            return;
        }

        messagingTemplate.convertAndSend(
                "/topic/draw/" + room.getRoomId(),
                (Object) payload
        );
    }

    private Map<String, Object> message(String type) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", type);
        return result;
    }

    @MessageMapping("/draw/create")
    public void createRoom(
            SimpMessageHeaderAccessor headerAccessor) {

        String sessionId = getSessionId(headerAccessor);

        if (sessionId == null || sessionId.isBlank()) {
            return;
        }

        // На случай, если эта сессия раньше была в другой комнате.
        roomService.leaveAllRooms(sessionId);

        DrawRoom room = roomService.createRoom(sessionId);
        room.setPainterSessionId(sessionId);

        Map<String, Object> response = message("room_created");
        response.put("roomId", room.getRoomId());
        response.put("role", "painter");

        sendToSession(sessionId, response);
    }

    @MessageMapping("/draw/{roomId}/join")
    public void joinRoom(
            @DestinationVariable String roomId,
            SimpMessageHeaderAccessor headerAccessor) {

        String sessionId = getSessionId(headerAccessor);

        if (sessionId == null || sessionId.isBlank()) {
            return;
        }

        DrawRoom room = roomService.getRoom(roomId);

        if (room == null) {
            Map<String, Object> error = message("error");
            error.put(
                    "message",
                    "Комната не найдена. Проверь код и попробуй ещё раз."
            );
            sendToSession(sessionId, error);
            return;
        }

        synchronized (room) {
            String role;

            if (sessionId.equals(room.getPainterSessionId())) {
                role = "painter";
            } else if (sessionId.equals(room.getGuesserSessionId())) {
                role = "guesser";
            } else if (room.getGuesserSessionId() == null
                    && room.getConnectedSessions().size() < 2) {
                room.setGuesserSessionId(sessionId);
                role = "guesser";
            } else {
                Map<String, Object> error = message("error");
                error.put("message", "Комната уже заполнена.");
                sendToSession(sessionId, error);
                return;
            }

            room.getConnectedSessions().add(sessionId);

            Map<String, Object> joined = message("joined");
            joined.put("roomId", room.getRoomId());
            joined.put("role", role);
            sendToSession(sessionId, joined);

            sendRoomMessage(room, message("clear"));
            sendRoomState(room);

            if (room.isFull() && !room.isRoundActive()) {
                startRound(room);
            }
        }
    }

    private void sendRoomState(DrawRoom room) {
        Map<String, Object> state = message("room_state");

        state.put(
                "painterConnected",
                room.getPainterSessionId() != null
                        && room.getConnectedSessions().contains(
                        room.getPainterSessionId()
                )
        );

        state.put(
                "guesserConnected",
                room.getGuesserSessionId() != null
                        && room.getConnectedSessions().contains(
                        room.getGuesserSessionId()
                )
        );

        sendRoomMessage(room, state);
    }

    /**
     * Запускает раунд, выбирая ещё не использованное слово.
     * Вызов безопасен и из обработчиков сообщений, и из таймера.
     */
    private void startRound(DrawRoom room) {
        synchronized (room) {
            // Любой предыдущий таймер становится недействительным.
            long version = room.nextRoundVersion();

            if (roomService.getRoom(room.getRoomId()) != room
                    || !room.isFull()) {
                room.setRoundActive(false);
                return;
            }

            List<MiniGame> games =
                    miniGameService.findAllActiveByType(
                            "DRAW_AND_GUESS"
                    );

            if (games == null || games.isEmpty()) {
                room.setRoundActive(false);

                Map<String, Object> error = message("error");
                error.put(
                        "message",
                        "Нет активных игр «Рисуй и угадывай»"
                );
                sendRoomMessage(room, error);
                return;
            }

            /*
             * Собираем уникальные отображаемые слова из всех активных
             * игр этого типа. Если в базе несколько заданий с одинаковым
             * словом, оно всё равно считается одним словом.
             */
            Map<String, String> uniqueWords = new LinkedHashMap<>();

            for (MiniGame game : games) {
                List<MiniGameTask> tasks =
                        taskService.findActiveByGame(game);

                if (tasks == null) {
                    continue;
                }

                for (MiniGameTask task : tasks) {
                    String word = task.getAnswer() != null
                            && !task.getAnswer().isBlank()
                            ? task.getAnswer().trim()
                            : task.getContent();

                    if (word == null || word.isBlank()) {
                        continue;
                    }

                    word = word.trim().replaceAll("\\s+", " ");
                    String key = normalizeWord(word);

                    if (!key.isBlank()) {
                        uniqueWords.putIfAbsent(key, word);
                    }
                }
            }

            if (uniqueWords.isEmpty()) {
                room.setRoundActive(false);

                Map<String, Object> error = message("error");
                error.put("message", "Нет слов для рисования.");
                sendRoomMessage(room, error);
                return;
            }

            List<String> availableKeys = new ArrayList<>();

            String previousKey = normalizeWord(room.getCurrentWord());

            for (String key : uniqueWords.keySet()) {
                if (!room.getUsedWords().contains(key)) {
                    availableKeys.add(key);
                }
            }

// Если остались неиспользованные слова, по возможности
// исключаем последнее показанное слово.
            if (availableKeys.size() > 1) {
                availableKeys.removeIf(key -> key.equals(previousKey));
            }

// Если все слова уже использованы, начинаем новый цикл.
            if (availableKeys.isEmpty()) {
                room.getUsedWords().clear();

                for (String key : uniqueWords.keySet()) {
                    if (!key.equals(previousKey)) {
                        availableKeys.add(key);
                    }
                }

                // Если в базе только одно слово, повтор неизбежен.
                if (availableKeys.isEmpty()) {
                    availableKeys.addAll(uniqueWords.keySet());
                }
            }

            String selectedKey = availableKeys.get(
                    random.nextInt(availableKeys.size())
            );

            String word = uniqueWords.get(selectedKey);

// Запоминаем выбранное слово до начала следующего раунда.
            room.getUsedWords().add(selectedKey);
            room.setCurrentWord(word);
            room.setRoundActive(true);

            Map<String, Object> wordMessage = message("word");
            wordMessage.put("word", word);
            sendToSession(room.getPainterSessionId(), wordMessage);

            Map<String, Object> roundMessage =
                    message("round_started");
            roundMessage.put("duration", ROUND_DURATION_SECONDS);
            sendRoomMessage(room, roundMessage);

            // Сервер сам завершит раунд через 90 секунд.
            roundScheduler.schedule(
                    () -> finishRoundByTimeout(room, version),
                    ROUND_DURATION_SECONDS,
                    TimeUnit.SECONDS
            );
        }
    }

    private String normalizeWord(String word) {
        if (word == null) {
            return "";
        }

        return word.trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }

    /**
     * Вызывается серверным таймером. Старые таймеры ничего не делают,
     * если раунд уже завершился или начался следующий.
     */
    private void finishRoundByTimeout(
            DrawRoom room,
            long expectedVersion) {

        synchronized (room) {
            if (roomService.getRoom(room.getRoomId()) != room
                    || !room.isRoundActive()
                    || room.getRoundVersion() != expectedVersion) {
                return;
            }

            room.setRoundActive(false);
            room.nextRoundVersion();

            Map<String, Object> timeout = message("round_skipped");
            timeout.put("word", room.getCurrentWord());
            timeout.put("timeout", true);
            sendRoomMessage(room, timeout);

            swapRolesAndStartRound(room);
        }
    }

    private void swapRolesAndStartRound(DrawRoom room) {
        synchronized (room) {
            if (!room.isFull()) {
                return;
            }

            String oldPainter = room.getPainterSessionId();
            String oldGuesser = room.getGuesserSessionId();

            room.setPainterSessionId(oldGuesser);
            room.setGuesserSessionId(oldPainter);

            sendRoomMessage(room, message("clear"));

            Map<String, Object> painterRole =
                    message("role_changed");
            painterRole.put("role", "painter");
            sendToSession(oldGuesser, painterRole);

            Map<String, Object> guesserRole =
                    message("role_changed");
            guesserRole.put("role", "guesser");
            sendToSession(oldPainter, guesserRole);

            startRound(room);
        }
    }

    @MessageMapping("/draw/{roomId}/stroke")
    public void handleStroke(
            @DestinationVariable String roomId,
            @Payload Map<String, Object> point,
            SimpMessageHeaderAccessor headerAccessor) {

        String sessionId = getSessionId(headerAccessor);

        if (sessionId == null || point == null) {
            return;
        }

        DrawRoom room = roomService.getRoom(roomId);

        if (room == null) {
            return;
        }

        synchronized (room) {
            if (!room.isRoundActive()
                    || !sessionId.equals(room.getPainterSessionId())) {
                return;
            }

            Double x = decimalNumber(point.get("x"));
            Double y = decimalNumber(point.get("y"));

            if (x == null || y == null) {
                return;
            }

            x = Math.max(0.0, Math.min(1.0, x));
            y = Math.max(0.0, Math.min(1.0, y));

            Map<String, Object> stroke = message("stroke");
            stroke.put("x", x);
            stroke.put("y", y);
            stroke.put(
                    "start",
                    Boolean.TRUE.equals(point.get("start"))
            );

            sendRoomMessage(room, stroke);
        }
    }

    private Double decimalNumber(Object value) {
        if (value == null) {
            return null;
        }

        try {
            double result = value instanceof Number number
                    ? number.doubleValue()
                    : Double.parseDouble(value.toString());

            return Double.isFinite(result) ? result : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @MessageMapping("/draw/{roomId}/clear")
    public void clearCanvas(
            @DestinationVariable String roomId,
            SimpMessageHeaderAccessor headerAccessor) {

        String sessionId = getSessionId(headerAccessor);

        if (sessionId == null) {
            return;
        }

        DrawRoom room = roomService.getRoom(roomId);

        if (room == null) {
            return;
        }

        synchronized (room) {
            if (room.isRoundActive()
                    && sessionId.equals(room.getPainterSessionId())) {
                sendRoomMessage(room, message("clear"));
            }
        }
    }

    @MessageMapping("/draw/{roomId}/guess")
    public void handleGuess(
            @DestinationVariable String roomId,
            @Payload Map<String, String> payload,
            SimpMessageHeaderAccessor headerAccessor) {

        String sessionId = getSessionId(headerAccessor);

        if (sessionId == null) {
            return;
        }

        DrawRoom room = roomService.getRoom(roomId);

        if (room == null) {
            return;
        }

        synchronized (room) {
            if (!room.isRoundActive()
                    || !sessionId.equals(room.getGuesserSessionId())) {
                return;
            }

            String guess = payload == null ? null : payload.get("text");

            boolean correct = guess != null
                    && room.getCurrentWord() != null
                    && guess.trim().equalsIgnoreCase(
                    room.getCurrentWord().trim()
            );

            Map<String, Object> result = message("guess_result");
            result.put("correct", correct);
            result.put("guess", guess == null ? "" : guess);
            sendRoomMessage(room, result);

            if (correct) {
                room.setRoundActive(false);
                room.nextRoundVersion();

                Map<String, Object> won = message("round_won");
                won.put("word", room.getCurrentWord());
                sendRoomMessage(room, won);
            }
        }
    }

    @MessageMapping("/draw/{roomId}/skip")
    public void skipWord(
            @DestinationVariable String roomId,
            SimpMessageHeaderAccessor headerAccessor) {

        String sessionId = getSessionId(headerAccessor);

        if (sessionId == null) {
            return;
        }

        DrawRoom room = roomService.getRoom(roomId);

        if (room == null) {
            return;
        }

        synchronized (room) {
            if (!room.isRoundActive()
                    || !sessionId.equals(room.getPainterSessionId())) {
                return;
            }

            room.setRoundActive(false);
            room.nextRoundVersion();

            Map<String, Object> skipped = message("round_skipped");
            skipped.put("word", room.getCurrentWord());
            sendRoomMessage(room, skipped);

            // При ручном пропуске художник пока остаётся прежним.
            startRound(room);
        }
    }

    @MessageMapping("/draw/{roomId}/next")
    public void nextWord(
            @DestinationVariable String roomId,
            SimpMessageHeaderAccessor headerAccessor) {

        String sessionId = getSessionId(headerAccessor);

        if (sessionId == null || sessionId.isBlank()) {
            return;
        }

        DrawRoom room = roomService.getRoom(roomId);

        if (room == null) {
            return;
        }

        synchronized (room) {
            if (!room.isFull() || room.isRoundActive()) {
                return;
            }

            if (!sessionId.equals(room.getPainterSessionId())
                    && !sessionId.equals(room.getGuesserSessionId())) {
                return;
            }

            swapRolesAndStartRound(room);
        }
    }

    /**
     * Выход игрока из комнаты.
     * Клиент отправляет сообщение на /app/draw/{roomId}/leave.
     */
    @MessageMapping("/draw/{roomId}/leave")
    public void leaveRoom(
            @DestinationVariable String roomId,
            SimpMessageHeaderAccessor headerAccessor) {

        String sessionId = getSessionId(headerAccessor);

        if (sessionId == null || sessionId.isBlank()) {
            return;
        }

        DrawRoom room = roomService.getRoom(roomId);

        if (room != null) {
            synchronized (room) {
                if (room.getConnectedSessions().contains(sessionId)) {
                    room.setRoundActive(false);
                    room.nextRoundVersion();

                    Map<String, Object> left = message("player_left");
                    sendRoomMessage(room, left);

                    room.setCurrentWord(null);
                }
            }
        }

        roomService.leaveAllRooms(sessionId);

        // Уведомляем браузер выходящего игрока, чтобы показать лобби.
        sendToSession(sessionId, message("left"));

        // Если второй игрок остался, он становится художником
        // и может дождаться нового участника.
        DrawRoom remainingRoom = roomService.getRoom(roomId);

        if (remainingRoom != null) {
            synchronized (remainingRoom) {
                if (!remainingRoom.getConnectedSessions().isEmpty()) {
                    String remainingSession = remainingRoom
                            .getConnectedSessions()
                            .iterator()
                            .next();

                    remainingRoom.setPainterSessionId(remainingSession);
                    remainingRoom.setGuesserSessionId(null);
                    remainingRoom.setRoundActive(false);
                    remainingRoom.setCurrentWord(null);

                    Map<String, Object> role =
                            message("role_changed");
                    role.put("role", "painter");
                    sendToSession(remainingSession, role);

                    sendRoomState(remainingRoom);
                }
            }
        }
    }
}
