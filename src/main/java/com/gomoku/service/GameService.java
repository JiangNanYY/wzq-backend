package com.gomoku.service;

import com.gomoku.model.Move;
import com.gomoku.model.Room;
import com.gomoku.model.User;
import org.springframework.stereotype.Service;

@Service
public class GameService {

    public boolean makeMove(Room room, Move move) {
        if (!room.getGameState().equals("playing")) {
            return false;
        }
        
        if (!room.getCurrentTurn().equals(move.getColor())) {
            return false;
        }
        
        int[][] board = room.getBoard();
        if (board[move.getY()][move.getX()] != 0) {
            return false;
        }
        
        // Place the piece
        board[move.getY()][move.getX()] = move.getColor().equals("black") ? 1 : 2;
        
        // Check winner
        if (checkWin(board, move.getX(), move.getY(), move.getColor())) {
            room.setGameState("ended");
            User winner = room.getPlayers().stream()
                    .filter(p -> p.getId().equals(move.getPlayerId()))
                    .findFirst()
                    .orElse(null);
            room.setWinner(winner);
        } else {
            // Switch turn
            room.setCurrentTurn(move.getColor().equals("black") ? "white" : "black");
        }
        
        return true;
    }

    private boolean checkWin(int[][] board, int x, int y, String color) {
        int player = color.equals("black") ? 1 : 2;
        int[][] directions = {
            {1, 0},   // Horizontal
            {0, 1},   // Vertical
            {1, 1},   // Diagonal
            {1, -1}   // Anti-diagonal
        };
        
        for (int[] dir : directions) {
            int count = 1;
            
            // Check in positive direction
            for (int i = 1; i < 5; i++) {
                int nx = x + dir[0] * i;
                int ny = y + dir[1] * i;
                if (nx >= 0 && nx < 15 && ny >= 0 && ny < 15 && board[ny][nx] == player) {
                    count++;
                } else {
                    break;
                }
            }
            
            // Check in negative direction
            for (int i = 1; i < 5; i++) {
                int nx = x - dir[0] * i;
                int ny = y - dir[1] * i;
                if (nx >= 0 && nx < 15 && ny >= 0 && ny < 15 && board[ny][nx] == player) {
                    count++;
                } else {
                    break;
                }
            }
            
            if (count >= 5) {
                return true;
            }
        }
        
        return false;
    }

    public void resetGame(Room room) {
        room.setGameState("waiting");
        room.setCurrentTurn("black");
        room.setWinner(null);
        room.setBoard(new int[15][15]);
        room.getPlayers().forEach(p -> {
            p.setReady(false);
            p.setColor(null);
            p.setPlayer(false);
        });
    }
}
