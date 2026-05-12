package com.gomoku.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    private String id;
    private List<User> players = new ArrayList<>();
    private List<User> spectators = new ArrayList<>();
    private String gameState; // "waiting", "playing", "ended"
    private int[][] board;
    private String currentTurn;
    private User winner;

    public Room(String id) {
        this.id = id;
        this.gameState = "waiting";
        this.currentTurn = "black";
        this.board = new int[15][15];
    }
}
