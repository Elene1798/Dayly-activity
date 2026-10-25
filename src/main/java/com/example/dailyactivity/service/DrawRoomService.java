
package com.example.dailyactivity.service;

import com.example.dailyactivity.model.DrawRoom;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DrawRoomService {

    private final Map<String, DrawRoom> rooms =
            new ConcurrentHashMap<>();

    public DrawRoom createRoom(String creatorSessionId) {
        String roomId;

        do {
            roomId = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 6)
                    .toUpperCase();
        } while (rooms.containsKey(roomId));

        DrawRoom room = new DrawRoom(roomId);
        room.getConnectedSessions().add(creatorSessionId);

        rooms.put(roomId, room);

        return room;
    }

    public DrawRoom getRoom(String roomId) {
        if (roomId == null) {
            return null;
        }

        return rooms.get(roomId.toUpperCase());
    }

    public void removeRoom(String roomId) {
        if (roomId != null) {
            rooms.remove(roomId.toUpperCase());
        }
    }

    /**
     * Убирает сессию из всех комнат.
     * Возвращает ID первой найденной комнаты.
     */
    public String leaveAllRooms(String sessionId) {
        if (sessionId == null) {
            return null;
        }

        String firstRoomId = null;

        for (Map.Entry<String, DrawRoom> entry : rooms.entrySet()) {
            DrawRoom room = entry.getValue();

            synchronized (room) {
                boolean wasConnected =
                        room.getConnectedSessions().remove(sessionId);

                if (!wasConnected) {
                    continue;
                }

                if (firstRoomId == null) {
                    firstRoomId = room.getRoomId();
                }

                if (sessionId.equals(room.getPainterSessionId())) {
                    room.setPainterSessionId(null);
                }

                if (sessionId.equals(room.getGuesserSessionId())) {
                    room.setGuesserSessionId(null);
                }

                // Инвалидируем таймер и завершаем раунд.
                room.setRoundActive(false);
                room.nextRoundVersion();
                room.setCurrentWord(null);

                if (room.getConnectedSessions().isEmpty()) {
                    rooms.remove(entry.getKey(), room);
                }
            }
        }

        return firstRoomId;
    }
}
