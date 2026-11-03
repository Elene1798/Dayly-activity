package com.example.dailyactivity.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SpyRoom {

    private final String roomId;
    private final List<Player> players = new CopyOnWriteArrayList<>();

    private boolean gameStarted = false;
    private long timerEndTime = 0;

    private String currentSpyPlayerId;
    private String currentLocationName;
    private String hostPlayerId;

    public SpyRoom(String roomId) {
        this.roomId = roomId;
    }

    public static class Player {

        public String playerId;
        public String sessionId;
        public String name;
        public boolean isSpy;
        public boolean online = true;

        public Player(String playerId, String sessionId, String name) {
            this.playerId = playerId;
            this.sessionId = sessionId;
            this.name = name;
        }
    }

    public String getRoomId() {
        return roomId;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    public long getTimerEndTime() {
        return timerEndTime;
    }

    public void setTimerEndTime(long timerEndTime) {
        this.timerEndTime = timerEndTime;
    }

    public String getCurrentSpyPlayerId() {
        return currentSpyPlayerId;
    }

    public void setCurrentSpyPlayerId(String currentSpyPlayerId) {
        this.currentSpyPlayerId = currentSpyPlayerId;
    }

    public String getCurrentLocationName() {
        return currentLocationName;
    }

    public void setCurrentLocationName(String currentLocationName) {
        this.currentLocationName = currentLocationName;
    }

    public String getHostPlayerId() {
        return hostPlayerId;
    }

    public void setHostPlayerId(String hostPlayerId) {
        this.hostPlayerId = hostPlayerId;
    }
}