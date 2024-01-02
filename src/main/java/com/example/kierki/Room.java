package com.example.kierki;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Room implements Serializable {

    private int idRoom;
    private int amountOfPlayers;
    private String roomName;
    private ArrayList<String> players;

    private ArrayList<Integer> clientsID;

    private boolean gameInProgress;
    private ArrayList<Card> deck;

    private int turn;

    public Room(String roomName, int idRoom) {
        this.roomName = roomName;
        this.players = new ArrayList<>();
        this.clientsID = new ArrayList<>();
        this.gameInProgress = false;
        this.amountOfPlayers = 0;
        this.idRoom = idRoom;
        initializeDeck();
    }

    public ArrayList<Integer> getClientsID() {
        return clientsID;
    }

    public String getRoomName() {
        return roomName;
    }

    public int getAmountOfPlayers() {
        return amountOfPlayers;
    }

    public int getIdRoom() {
        return idRoom;
    }

    private void initializeDeck() {
        deck = new ArrayList<>();

        String[] symbols = {"Hearts", "Diamonds", "Clubs", "Spades"};
        int[] values = {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14};

        for (String symbol : symbols) {
            for (int value : values) {
                deck.add(new Card(symbol, value));
            }
        }
//        displayDeck();
    }

    public void shuffleDeck() {
        Collections.shuffle(deck);
        System.out.println("Deck shuffled.");
//        displayDeck();
    }

    public void dealCards(){
        int currentClientIndex = 0;

        for (Card card : deck) {
            int currentClientID = clientsID.get(currentClientIndex);
            card.setClientID(currentClientID);
            currentClientIndex = (currentClientIndex + 1) % amountOfPlayers;
        }

//        System.out.println("Cards dealt to players.");
//        displayDeck();
    }

    public void randomTurn() {
        Random random = new Random();
        turn = random.nextInt(4);
        System.out.println("Randomly generated turn: " + turn);
    }

    public void nextTurn() {
        turn = (turn + 1) % 4;
        System.out.println("Next turn: " + turn);
    }
    public void addPlayer(String nickname,int clientID) {
        players.add(nickname);
        amountOfPlayers++;
        clientsID.add(clientID);
    }

    public void setGameInProgress(Boolean bool) {
        gameInProgress = bool;
    }

    public void displayDeck() {
        for (Card card : deck) {
            card.displayCard();
        }
    }

    public int getTurn() {
        return turn;
    }
    public ArrayList<Card> getCardsFromClientID(int clientID) {
        ArrayList<Card> clientsCards = new ArrayList<>();
        for (Card card : deck) {
            if(card.getClientID()==clientID){
                clientsCards.add(card);
            }
        }
        return clientsCards;
    }

//    public void setDeck(ArrayList<Card> deck) {
//        this.deck = deck;
//    }
}