package com.example.dailyactivity;

import com.example.dailyactivity.model.SpyLocation;
import com.example.dailyactivity.model.SpyRoom;
import com.example.dailyactivity.repository.SpyLocationRepository;
import com.example.dailyactivity.service.SpyRoomService;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class SpyGameController {

    private final SimpMessagingTemplate messagingTemplate;
    private final SpyRoomService roomService;
    private final SpyLocationRepository locationRepo;
    private final Random random = new Random();

    public SpyGameController(
            SimpMessagingTemplate messagingTemplate,
            SpyRoomService roomService,
            SpyLocationRepository locationRepo
    ) {
        this.messagingTemplate = messagingTemplate;
        this.roomService = roomService;
        this.locationRepo = locationRepo;
    }

    // СОЗДАНИЕ КОМНАТЫ

    @MessageMapping("/spy/create")
    public void createRoom(
            @Payload Map<String, String> payload,
            SimpMessageHeaderAccessor header
    ) {
        String sessionId = header.getSessionId();
        String playerId = payload.get("playerId");
        String name = payload.get("name");

        if (sessionId == null ||
                playerId == null ||
                playerId.isBlank() ||
                name == null ||
                name.isBlank()) {
            return;
        }

        SpyRoom room = roomService.createRoom(sessionId, playerId, name);

        sendToSession(sessionId, "/queue/spy", Map.of(
                "type", "room_created",
                "roomId", room.getRoomId(),
                "playerId", playerId,
                "isHost", true,
                "players", getPlayersList(room)
        ));
    }

    // ВХОД ИЛИ ВОЗВРАЩЕНИЕ В КОМНАТУ

    @MessageMapping("/spy/{roomId}/join")
    public void joinRoom(
            @DestinationVariable String roomId,
            @Payload Map<String, String> payload,
            SimpMessageHeaderAccessor header
    ) {
        String sessionId = header.getSessionId();
        String playerId = payload.get("playerId");
        String name = payload.get("name");

        SpyRoom room = roomService.getRoom(roomId);

        if (room == null) {
            sendToSession(sessionId, "/queue/spy", Map.of(
                    "type", "error",
                    "message", "Комната не найдена"
            ));
            return;
        }

        if (sessionId == null ||
                playerId == null ||
                playerId.isBlank() ||
                name == null ||
                name.isBlank()) {
            return;
        }

        SpyRoom.Player player = roomService.reconnectPlayer(
                room,
                playerId,
                sessionId,
                name
        );

        boolean isHost = roomService.isHost(room, playerId);

        Map<String, Object> response = new HashMap<>();

        response.put("type", "joined");
        response.put("roomId", roomId);
        response.put("playerId", playerId);
        response.put("isHost", isHost);
        response.put("players", getPlayersList(room));

        sendToSession(sessionId, "/queue/spy", response);

        // Если игра уже началась — возвращаем прежнюю карточку
        if (room.isGameStarted()) {
            sendRoleToPlayer(room, player);
        }

        broadcastRoomState(room, "player_joined");
    }

    // НАЧАЛО ИГРЫ

    @MessageMapping("/spy/{roomId}/start")
    public void startGame(
            @DestinationVariable String roomId,
            SimpMessageHeaderAccessor header
    ) {
        SpyRoom room = roomService.getRoom(roomId);

        if (room == null) {
            return;
        }

        String sessionId = header.getSessionId();

        // Проверяем, что запрос отправил действующий создатель
        SpyRoom.Player host = room.getPlayers().stream()
                .filter(p -> p.online && sessionId != null
                        && sessionId.equals(p.sessionId))
                .findFirst()
                .orElse(null);

        if (host == null ||
                !roomService.isHost(room, host.playerId)) {

            System.out.println("SPY START: нет прав на запуск");
            return;
        }

        // Берём только подключённых игроков
        List<SpyRoom.Player> onlinePlayers = room.getPlayers().stream()
                .filter(p -> p.online && p.sessionId != null)
                .collect(Collectors.toList());

        if (onlinePlayers.size() < 3) {
            sendToSession(
                    sessionId,
                    "/queue/spy",
                    Map.of(
                            "type", "error",
                            "message", "Для начала игры нужно минимум 3 игрока"
                    )
            );
            return;
        }

        // Получаем доступные локации
        List<SpyLocation> locations = locationRepo.findByActiveTrue();

        if (locations.isEmpty()) {
            sendToSession(
                    sessionId,
                    "/queue/spy",
                    Map.of(
                            "type", "error",
                            "message", "Нет доступных локаций"
                    )
            );
            return;
        }

        // Каждый запуск выбираем нового случайного шпиона
        SpyRoom.Player spy = onlinePlayers.get(
                random.nextInt(onlinePlayers.size())
        );

        // И новую локацию
        SpyLocation location = locations.get(
                random.nextInt(locations.size())
        );

        // Обновляем состояние игры
        room.setCurrentSpyPlayerId(spy.playerId);
        room.setCurrentLocationName(location.getName());
        room.setGameStarted(true);

        long timerEndTime = System.currentTimeMillis() + 180_000;
        room.setTimerEndTime(timerEndTime);

        // Раздаём новые карточки всем подключённым игрокам
        for (SpyRoom.Player player : onlinePlayers) {
            sendRoleToPlayer(room, player);
        }

        broadcastRoomState(room, "game_started");

        System.out.println(
                "SPY GAME STARTED: room=" + roomId
                        + ", spy=" + spy.name
                        + ", location=" + location.getName()
        );
    }

    // ВЫХОД ИЗ КОМНАТЫ

    @MessageMapping("/spy/{roomId}/leave")
    public void leaveRoom(
            @DestinationVariable String roomId,
            SimpMessageHeaderAccessor header
    ) {
        String sessionId = header.getSessionId();

        if (sessionId != null) {
            removePlayerFromRoom(roomId, sessionId);
        }
    }

    // АВТОМАТИЧЕСКОЕ ОТКЛЮЧЕНИЕ

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();

        if (sessionId == null) {
            return;
        }

        for (SpyRoom room : roomService.getAllRooms()) {
            boolean exists = room.getPlayers().stream()
                    .anyMatch(p -> sessionId.equals(p.sessionId));

            if (exists) {
                removePlayerFromRoom(room.getRoomId(), sessionId);
            }
        }
    }

    // УДАЛЕНИЕ / ОТКЛЮЧЕНИЕ ИГРОКА

    private void removePlayerFromRoom(String roomId, String sessionId) {

        SpyRoom room = roomService.markPlayerOffline(roomId, sessionId);

        if (room == null) {
            return;
        }

        broadcastRoomState(room, "player_left");
    }

    // ОТПРАВКА КАРТОЧКИ ИГРОКУ

    private void sendRoleToPlayer(
            SpyRoom room,
            SpyRoom.Player player
    ) {
        if (!player.online || player.sessionId == null) {
            return;
        }

        boolean isSpy = player.playerId.equals(
                room.getCurrentSpyPlayerId()
        );

        Map<String, Object> message = new HashMap<>();

        message.put("type", "role_assigned");
        message.put("isSpy", isSpy);
        message.put("location", isSpy ? "???" : room.getCurrentLocationName());
        message.put("timerEndTime", room.getTimerEndTime());
        message.put("isHost", roomService.isHost(room, player.playerId));
        message.put("players", getPlayersList(room));

        sendToSession(player.sessionId, "/queue/spy", message);
    }

    // ОБНОВЛЕНИЕ СОСТОЯНИЯ КОМНАТЫ

    private void broadcastRoomState(SpyRoom room, String type) {

        Map<String, Object> message = new HashMap<>();

        message.put("type", type);
        message.put("players", getPlayersList(room));
        message.put("hostPlayerId", room.getHostPlayerId());
        message.put("gameStarted", room.isGameStarted());

        messagingTemplate.convertAndSend(
                "/topic/spy/" + room.getRoomId(),
                (Object) message
        );
    }

    // СПИСОК ИГРОКОВ

    private List<Map<String, Object>> getPlayersList(SpyRoom room) {

        return room.getPlayers().stream()
                .map(player -> {
                    Map<String, Object> data = new HashMap<>();

                    data.put("playerId", player.playerId);
                    data.put("name", player.name);
                    data.put("online", player.online);
                    data.put("isHost", roomService.isHost(room, player.playerId));

                    return data;
                })
                .collect(Collectors.toList());
    }

    // ОТПРАВКА ПЕРСОНАЛЬНОГО СООБЩЕНИЯ

    private void sendToSession(
            String sessionId,
            String destination,
            Object payload
    ) {
        SimpMessageHeaderAccessor headers =
                SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);

        headers.setSessionId(sessionId);
        headers.setLeaveMutable(true);

        messagingTemplate.convertAndSendToUser(
                sessionId,
                destination,
                payload,
                headers.getMessageHeaders()
        );
    }
}