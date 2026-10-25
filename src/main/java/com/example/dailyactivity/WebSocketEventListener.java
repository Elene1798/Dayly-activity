package com.example.dailyactivity;

import com.example.dailyactivity.service.DrawRoomService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;

@Component
public class WebSocketEventListener {

    private final DrawRoomService roomService;
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketEventListener(DrawRoomService roomService,
                                  SimpMessagingTemplate messagingTemplate) {
        this.roomService = roomService;
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        String roomId = roomService.leaveAllRooms(sessionId);

        if (roomId != null) {
            messagingTemplate.convertAndSend(
                    "/topic/draw/" + roomId,
                    (Object) Map.of("type", "player_left", "sessionId", sessionId)
            );
        }
    }
}