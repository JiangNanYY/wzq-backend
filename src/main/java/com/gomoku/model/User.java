package com.gomoku.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String id;
    private String nickname;
    private boolean isPlayer;
    private boolean isReady;
    private String color; // "black" or "white"

    // Simple constructor for joining
    public User(String id, String nickname) {
        this.id = id;
        this.nickname = nickname;
        this.isPlayer = false;
        this.isReady = false;
        this.color = null;
    }
}
