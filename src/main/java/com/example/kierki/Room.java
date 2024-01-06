package com.example.kierki;

import java.io.Serializable;
import java.util.*;

public class Room implements Serializable {

    private int idRoom;
    private int amountOfPlayers;
    private String roomName;
    private ArrayList<String> players;

    private ArrayList<Integer> clientsID;

    private boolean gameInProgress;

    private int round;

    public Card getFirstCardOnTable() {
        return firstCardOnTable;
    }

    public void setFirstCardOnTable(Card firstCardOnTable) {
        this.firstCardOnTable = firstCardOnTable;
    }

    private Card firstCardOnTable;

    private ArrayList<Card> deck;

    private HashMap<Integer, Integer> points;

    private int turn;

    private HashMap<Integer, Card> actualPlay;

    public Room(String roomName, int idRoom) {
        this.roomName = roomName;
        this.players = new ArrayList<>();
        this.clientsID = new ArrayList<>();
        this.gameInProgress = false;
        this.amountOfPlayers = 0;
        this.idRoom = idRoom;
        this.points = new HashMap<>();
        this.round = 2;
        this.firstCardOnTable = new Card("XXX", 99);
        this.actualPlay = new HashMap<>();
        initializeDeck();
    }

    public ArrayList<Card> getDeck() {
        return deck;
    }

    public void setDeck(ArrayList<Card> deck) {
        this.deck = deck;
    }

    public void setActualCard(int clientID, Card card) {
        actualPlay.put(clientID, card);
    }

    public HashMap<Integer, Integer> getPoints() {
        return points;
    }

    public void setPoints(int clientID, int clientPoints) {
        int currentPoints = points.get(clientID);
        currentPoints += clientPoints;
        points.put(clientID, currentPoints);
    }

    public void nextRound() {
        round++;
    }

    public int getRound() {
        return round;
    }

    public void initializePoints() {
        for (int i = 0; i < 4; i++) {
            points.put(clientsID.get(i), 0);
        }
    }

    public HashMap<Integer, Card> getActualPlay() {
        return actualPlay;
    }

    public Card getActualCard(int clientID) {
        return actualPlay.get(clientID);
    }

    public void resetActualCards() {
        actualPlay.clear();
    }

    public int checkActualPlay() {
        return actualPlay.size();
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

    public void initializeDeck() {
        deck = new ArrayList<>();

        String[] symbols = {"Hearts", "Diamonds", "Clubs", "Spades"};
        int[] values = {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14};

        for (String symbol : symbols) {
            for (int value : values) {
                deck.add(new Card(symbol, value));
            }
        }
    }

    public void shuffleDeck() {
        Collections.shuffle(deck);
        System.out.println("Deck shuffled.");
    }

    public void dealCards() {
        int currentClientIndex = 0;

        for (Card card : deck) {
            int currentClientID = clientsID.get(currentClientIndex);
            card.setClientID(currentClientID);
            currentClientIndex = (currentClientIndex + 1) % amountOfPlayers;
        }
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
    public void nextTurnNumbered(int clientID){
        turn = clientsID.indexOf(clientID);
    }

    public void addPlayer(String nickname, int clientID) {
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
            if (card.getClientID() == clientID) {
                clientsCards.add(card);
            }
        }
        return clientsCards;
    }

    public ArrayList<String> getPlayers() {
        return players;
    }

    //    public void setDeck(ArrayList<Card> deck) {
//        this.deck = deck;
//    }
}