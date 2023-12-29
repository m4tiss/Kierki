package com.example.kierki;

import java.io.Serializable;
import java.util.ArrayList;

public class Room implements Serializable {
    private int amountOfPlayers;
    private String roomName;
    private ArrayList<String> players;
    private boolean gameInProgress;

    public Room(String roomName) {
        this.roomName = roomName;
        this.players = new ArrayList<>();
        this.gameInProgress = false;
        this.amountOfPlayers = 0;
    }

    public String getRoomName() {
        return roomName;
    }

    public int getAmountOfPlayers() {
        return amountOfPlayers;
    }

    public void addPlayer(String nickname) {
        players.add(nickname);
        amountOfPlayers++;
    }
}