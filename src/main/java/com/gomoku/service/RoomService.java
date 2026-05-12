package com.gomoku.service;

import com.gomoku.model.Room;
import com.gomoku.model.User;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RoomService {
    
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    
    public Room createRoom() {
        String roomId = UUID.randomUUID().toString().substring(0, 8);
        Room room = new Room(roomId);
        rooms.put(roomId, room);
        return room;
    }
    
    public Room getRoom(String roomId) {
        return rooms.get(roomId);
    }
    
    public boolean roomExists(String roomId) {
        return rooms.containsKey(roomId);
    }
    
    public void joinRoom(String roomId, User user) {
        Room room = rooms.get(roomId);
        if (room == null) {
            return;
        }
        
        // Check if user already exists
        boolean exists = room.getPlayers().stream().anyMatch(u -> u.getId().equals(user.getId())) ||
                        room.getSpectators().stream().anyMatch(u -> u.getId().equals(user.getId()));
        if (exists) {
            return;
        }
        
        user.setPlayer(false);
        user.setReady(false);
        
        room.getSpectators().add(user);
    }
    
    public void leaveRoom(String roomId, String userId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            return;
        }
        
        room.getPlayers().removeIf(u -> u.getId().equals(userId));
        room.getSpectators().removeIf(u -> u.getId().equals(userId));
        
        // If room is empty, remove it
        if (room.getPlayers().isEmpty() && room.getSpectators().isEmpty()) {
            rooms.remove(roomId);
        }
    }
    
    public void startGame(String roomId, String userId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            return;
        }
        
        // Try to find the user in spectators
        User user = room.getSpectators().stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .orElse(null);
        
        if (user != null && room.getPlayers().size() < 2) {
            room.getSpectators().remove(user);
            user.setPlayer(true);
            user.setReady(true);
            user.setColor(room.getPlayers().isEmpty() ? "black" : "white");
            room.getPlayers().add(user);
            
            // If we have 2 players, start the game
            if (room.getPlayers().size() == 2) {
                // 重置棋盘和winner
                int[][] board = new int[15][15];
                for (int i = 0; i < 15; i++) {
                    for (int j = 0; j < 15; j++) {
                        board[i][j] = 0;
                    }
                }
                room.setBoard(board);
                room.setWinner(null);
                room.setGameState("playing");
            } else {
                room.setGameState("waiting");
            }
        }
    }
    
    public void makeMove(String roomId, String userId, int x, int y) {
        Room room = rooms.get(roomId);
        if (room == null || !"playing".equals(room.getGameState())) {
            return;
        }
        
        // Find current player
        User player = room.getPlayers().stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .orElse(null);
        
        if (player == null || !player.getColor().equals(room.getCurrentTurn())) {
            return;
        }
        
        // Check if cell is empty
        if (room.getBoard()[y][x] != 0) {
            return;
        }
        
        // Make move
        int piece = "black".equals(player.getColor()) ? 1 : 2;
        room.getBoard()[y][x] = piece;
        
        // Check winner
        if (checkWin(room.getBoard(), x, y, piece)) {
            room.setGameState("ended");
            room.setWinner(player);
            // 把玩家移回spectators，但保留winner信息，让前端能正确判断身份
            List<User> players = new ArrayList<>(room.getPlayers());
            for (User p : players) {
                p.setPlayer(false);
                p.setReady(false);
                p.setColor(null);
                room.getSpectators().add(p);
            }
            room.getPlayers().clear();
        } else {
            // Switch turn
            room.setCurrentTurn("black".equals(room.getCurrentTurn()) ? "white" : "black");
        }
    }
    
    public void restartGame(String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            return;
        }
        
        // Reset board
        int[][] board = new int[15][15];
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                board[i][j] = 0;
            }
        }
        room.setBoard(board);
        
        // Move players back to spectators to let them rejoin
        List<User> players = new ArrayList<>(room.getPlayers());
        for (User player : players) {
            player.setPlayer(false);
            player.setReady(false);
            player.setColor(null);
            room.getSpectators().add(player);
        }
        room.getPlayers().clear();
        
        room.setCurrentTurn("black");
        room.setWinner(null);
        room.setGameState("waiting");
    }
    
    private boolean checkWin(int[][] board, int x, int y, int piece) {
        // Directions: horizontal, vertical, two diagonals
        int[][] directions = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};
        
        for (int[] dir : directions) {
            int count = 1;
            
            // Check positive direction
            for (int i = 1; i < 5; i++) {
                int nx = x + dir[0] * i;
                int ny = y + dir[1] * i;
                if (nx < 0 || nx >= 15 || ny < 0 || ny >= 15 || board[ny][nx] != piece) {
                    break;
                }
                count++;
            }
            
            // Check negative direction
            for (int i = 1; i < 5; i++) {
                int nx = x - dir[0] * i;
                int ny = y - dir[1] * i;
                if (nx < 0 || nx >= 15 || ny < 0 || ny >= 15 || board[ny][nx] != piece) {
                    break;
                }
                count++;
            }
            
            if (count >= 5) {
                return true;
            }
        }
        
        return false;
    }
}
