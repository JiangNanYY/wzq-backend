package com.gomoku.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gomoku.model.Room;
import com.gomoku.model.User;
import com.gomoku.service.RoomService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RoomWebSocketHandler extends TextWebSocketHandler {

    private final RoomService roomService;
    private final ObjectMapper objectMapper;
    // 房间ID -> 连接会话映射
    private final Map<String, Map<String, WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    public RoomWebSocketHandler(RoomService roomService) {
        this.roomService = roomService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String roomId = getRoomId(session);

        roomSessions.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>());
        roomSessions.get(roomId).put(session.getId(), session);

        broadcastRoom(roomId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String roomId = getRoomId(session);
        String payload = message.getPayload();
        
        Map<String, Object> data = objectMapper.readValue(payload, Map.class);
        String action = (String) data.get("action");
        String userId = (String) data.get("userId");

        switch (action) {
            case "join":
                String nickname = (String) data.get("nickname");
                roomService.joinRoom(roomId, new User(userId, nickname));
                break;
            case "leave":
                roomService.leaveRoom(roomId, userId);
                break;
            case "start":
                roomService.startGame(roomId, userId);
                break;
            case "move":
                // Handle number types properly
                Object xObj = data.get("x");
                Object yObj = data.get("y");
                int x = xObj instanceof Integer ? (Integer) xObj : (int) (long) xObj;
                int y = yObj instanceof Integer ? (Integer) yObj : (int) (long) yObj;
                roomService.makeMove(roomId, userId, x, y);
                break;
            case "restart":
                roomService.restartGame(roomId);
                break;
        }

        broadcastRoom(roomId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String roomId = getRoomId(session);
        Map<String, WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions != null) {
            sessions.remove(session.getId());
            if (sessions.isEmpty()) {
                roomSessions.remove(roomId);
            }
        }
    }

    private String getRoomId(WebSocketSession session) {
        return (String) session.getAttributes().get("roomId");
    }

    private void broadcastRoom(String roomId) {
        Room room = roomService.getRoom(roomId);
        if (room == null) return;

        Map<String, WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions == null) return;

        try {
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("type", "roomUpdate");
            messageMap.put("room", room);
            
            // Check if game ended for victory animation
            if ("ended".equals(room.getGameState()) && room.getWinner() != null) {
                messageMap.put("type", "gameEnd");
            }
            
            String json = objectMapper.writeValueAsString(messageMap);
            TextMessage message = new TextMessage(json);

            for (WebSocketSession session : sessions.values()) {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(message);
                    }
                } catch (IOException e) {
                    // Connection closed - remove it
                    try {
                        sessions.remove(session.getId());
                    } catch (Exception ex) {
                        // Ignore
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
