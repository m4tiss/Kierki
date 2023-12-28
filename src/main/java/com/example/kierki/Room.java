package com.example.kierki;

import java.util.ArrayList;
import java.util.List;

public class Room {
    private String roomName;
    private int amountOfClients;
    private ArrayList<String> players;
    private boolean gameInProgress;

    public Room(String roomName) {
        this.roomName = roomName;
        this.players = new ArrayList<>();
        this.gameInProgress = false;
    }
}