package com.example.dailyactivity.service;

import com.example.dailyactivity.model.SpyRoom;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SpyRoomService {

    private final Map<String, SpyRoom> rooms = new ConcurrentHashMap<>();

    public SpyRoom createRoom(String sessionId, String playerId, String playerName) {

        String roomId;

        do {
            roomId = UUID.randomUUID()
                    .toString()
                    .substring(0, 6)
                    .toUpperCase();
        } while (rooms.containsKey(roomId));

        SpyRoom room = new SpyRoom(roomId);

        SpyRoom.Player player =
                new SpyRoom.Player(playerId, sessionId, playerName);

        room.getPlayers().add(player);
        room.setHostPlayerId(playerId);

        rooms.put(roomId, room);

        return room;
    }

    public SpyRoom getRoom(String roomId) {
        return rooms.get(roomId);
    }

    public Collection<SpyRoom> getAllRooms() {
        return rooms.values();
    }

    public void removeRoom(String roomId) {
        rooms.remove(roomId);
    }

    /**
     * Возвращает существующего игрока или добавляет нового.
     * При возвращении обновляет его WebSocket sessionId.
     */
    public SpyRoom.Player reconnectPlayer(
            SpyRoom room,
            String playerId,
            String sessionId,
            String name
    ) {
        synchronized (room) {

            SpyRoom.Player player = room.getPlayers().stream()
                    .filter(p -> p.playerId.equals(playerId))
                    .findFirst()
                    .orElse(null);

            if (player == null) {
                player = new SpyRoom.Player(playerId, sessionId, name);
                room.getPlayers().add(player);
            } else {
                player.sessionId = sessionId;
                player.name = name;
                player.online = true;
            }

            return player;
        }
    }

    /**
     * Помечает игрока отключённым.
     * Если создатель вышел, передаёт права другому участнику.
     * Если никто не остался онлайн, удаляет комнату.
     */
    public SpyRoom markPlayerOffline(String roomId, String sessionId) {

        SpyRoom room = rooms.get(roomId);

        if (room == null) {
            return null;
        }

        synchronized (room) {

            SpyRoom.Player player = room.getPlayers().stream()
                    .filter(p -> sessionId.equals(p.sessionId))
                    .findFirst()
                    .orElse(null);

            if (player == null) {
                return room;
            }

            player.online = false;
            player.sessionId = null;

            // Передача прав создателя
            if (player.playerId.equals(room.getHostPlayerId())) {

                SpyRoom.Player newHost = room.getPlayers().stream()
                        .filter(p -> p.online)
                        .findFirst()
                        .orElse(null);

                room.setHostPlayerId(
                        newHost == null ? null : newHost.playerId
                );
            }

            // Если никто не остался онлайн — удаляем комнату
            boolean nobodyOnline = room.getPlayers().stream()
                    .noneMatch(p -> p.online);

            if (nobodyOnline) {
                rooms.remove(roomId, room);

                System.out.println("SPY ROOM REMOVED: " + roomId);

                return null;
            }

            return room;
        }
    }

    public boolean isHost(SpyRoom room, String playerId) {
        return playerId != null &&
                playerId.equals(room.getHostPlayerId());
    }
}