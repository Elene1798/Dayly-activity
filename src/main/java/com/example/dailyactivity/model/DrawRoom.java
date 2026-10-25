
package com.example.dailyactivity.model;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DrawRoom {

    private final String roomId;

    private String painterSessionId;
    private String guesserSessionId;
    private String currentWord;
    private boolean paused = false;
    private int remainingSeconds = 90;
    private long roundEndsAtMillis = 0;

    private boolean roundActive = false;

    // Сессии игроков, находящихся в комнате.
    private final Set<String> connectedSessions =
            ConcurrentHashMap.newKeySet();

    // Нормализованные слова, уже использованные в этой комнате.
    private final Set<String> usedWords =
            ConcurrentHashMap.newKeySet();

    // Меняется при запуске и завершении раунда.
    private long roundVersion = 0;

    public DrawRoom(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getPainterSessionId() {
        return painterSessionId;
    }

    public void setPainterSessionId(String id) {
        this.painterSessionId = id;
    }

    public String getGuesserSessionId() {
        return guesserSessionId;
    }

    public void setGuesserSessionId(String id) {
        this.guesserSessionId = id;
    }

    public String getCurrentWord() {
        return currentWord;
    }

    public void setCurrentWord(String word) {
        this.currentWord = word;
    }

    public boolean isRoundActive() {
        return roundActive;
    }

    public void setRoundActive(boolean active) {
        this.roundActive = active;
    }

    public Set<String> getConnectedSessions() {
        return connectedSessions;
    }

    public Set<String> getUsedWords() {
        return usedWords;
    }

    public long nextRoundVersion() {
        return ++roundVersion;
    }

    public long getRoundVersion() {
        return roundVersion;
    }

    public boolean isFull() {
        return painterSessionId != null
                && guesserSessionId != null;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(int remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }

    public long getRoundEndsAtMillis() {
        return roundEndsAtMillis;
    }

    public void setRoundEndsAtMillis(long roundEndsAtMillis) {
        this.roundEndsAtMillis = roundEndsAtMillis;
    }
}
