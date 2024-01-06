package com.example.kierki;

import org.controlsfx.control.tableview2.filter.filtereditor.SouthFilter;

import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import static java.lang.Thread.sleep;

public class Server {
    private static final int PORT = 8888;

    //mapa do trzyamania strumień klientów idKlienta, stream
    private static HashMap<Integer, ObjectOutputStream> outputStreams;

    //mapa do trzymania pokoi idRoom , Room
    private static HashMap<Integer, Room> rooms;

    //mapa do trzymania klient pokój idClient,idRoom,
    private static HashMap<Integer, Integer> clientRooms;

    private static int idRoom;
    private static int clientsId;
    private static HashMap<Integer, Semaphore> roomSemaphores;



    public static void initObjects() {
        outputStreams = new HashMap<>();
        rooms = new HashMap<>();
        clientRooms = new HashMap<>();
        idRoom = 1;
        clientsId = 1;
        rooms.put(idRoom, new Room("Zajaweczka",idRoom));
        idRoom++;
        roomSemaphores =new HashMap<>();
    }

    public static void main(String[] args) {

        initObjects();

        ExecutorService executorService = Executors.newCachedThreadPool();
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, clientsId);
                executorService.execute(clientHandler);
                clientsId++;
            }
        } catch (IOException e) {
            System.out.println("Error with serverSocket");
        } finally {
            executorService.shutdown();
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;
        private final int clientId;
        private String nickname;
        ObjectInputStream in;
        ObjectOutputStream out;

        public ClientHandler(Socket socket, int clientId) {
            this.socket = socket;
            this.clientId = clientId;
        }


        private void sendRooms() throws IOException {
            Set<Map.Entry<Integer, Room>> entrySet = rooms.entrySet();
            out.writeInt(entrySet.size());
            out.flush();

            for (Map.Entry<Integer, Room> entry : entrySet) {
                out.writeInt(entry.getKey());
                out.writeObject(entry.getValue());
                out.flush();
            }
        }



        private void waitOnRoomAndBroadcast() throws IOException, ClassNotFoundException {
            Integer chosenRoom = (Integer) in.readObject();
            clientRooms.put(clientId,chosenRoom);
            Room currentRoom = rooms.get(chosenRoom);
            currentRoom.addPlayer(nickname,clientId);

            roomSemaphores.putIfAbsent(chosenRoom, new Semaphore(0));

            if(currentRoom.getAmountOfPlayers()==4){
                currentRoom.setGameInProgress(Boolean.TRUE);
                currentRoom.shuffleDeck();
                currentRoom.dealCards();
                currentRoom.randomTurn();
            }

            int sendingClientRoom = clientRooms.get(clientId);

            for (Map.Entry<Integer, ObjectOutputStream> entry : outputStreams.entrySet()) {
                int targetClientId = entry.getKey();
                ObjectOutputStream targetOutputStream = entry.getValue();

                if (clientRooms.containsKey(targetClientId) && clientRooms.get(targetClientId) == sendingClientRoom) {
                    targetOutputStream.writeInt(rooms.get(chosenRoom).getAmountOfPlayers());
                    targetOutputStream.flush();
                }
            }
            if(currentRoom.getAmountOfPlayers()==4){
                for (int i = 0; i < 4; i++) {
                    roomSemaphores.get(chosenRoom).release();
                }
            }
        }

        private Room takeCurrentRoom(){
            int idCurrentRoom = clientRooms.get(clientId);
            return rooms.get(idCurrentRoom);
        }
        private void startOfGame() throws IOException {
            int idCurrentRoom = clientRooms.get(clientId);
            try {
                roomSemaphores.get(idCurrentRoom).acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            takeCurrentRoom().initializePoints();
            out.reset();
            out.writeObject(rooms.get(idCurrentRoom));
            out.flush();
        }
        private void broadcastToSameRoomPlayers() throws IOException {
            int sendingClientRoom = clientRooms.get(clientId);

            for (Map.Entry<Integer, ObjectOutputStream> entry : outputStreams.entrySet()) {
                int targetClientId = entry.getKey();
                ObjectOutputStream targetOutputStream = entry.getValue();

                if (clientRooms.containsKey(targetClientId) && clientRooms.get(targetClientId) == sendingClientRoom) {
                    targetOutputStream.reset();
                    targetOutputStream.writeObject(takeCurrentRoom());
                    targetOutputStream.flush();
                }
            }
        }

        private boolean validateCardMove(String chosenSymbol){
            ArrayList<Card> clientDeck = takeCurrentRoom().getCardsFromClientID(clientId);

            String currentSymbol = takeCurrentRoom().getFirstCardOnTable().getSymbol();

            if(!Objects.equals(chosenSymbol, currentSymbol)){
                //Sprawdzenie czy user ma w talii taki kolor
                List<String> availableColors = new ArrayList<>();
                for (Card card : clientDeck) {
                    String symbol = card.getSymbol();
                    if (!availableColors.contains(symbol)) {
                        availableColors.add(symbol);
                    }
                }
                if(availableColors.contains(currentSymbol)){
                    return false;
                }
                return true;
            }
            return true;
        }

        private ArrayList<Card> getCards() {
            ArrayList<Card> cards = new ArrayList<>();
            List<Integer> clientsID = takeCurrentRoom().getClientsID();
            for (Integer clientID : clientsID) {
                Card card = takeCurrentRoom().getActualCard(clientID);
                cards.add(card);
            }
            return cards;
        }

        private void sortCards(List<Card> cards) {
            Comparator<Card> valueComparator = Comparator.comparing(Card::getValue).reversed();
            cards.sort(valueComparator);
        }

        private int takeWinnerID(Card winningCard) {
            HashMap<Integer, Card> actualPlay = takeCurrentRoom().getActualPlay();
            for (Map.Entry<Integer, Card> entry : actualPlay.entrySet()) {
                if (entry.getValue().equals(winningCard)) {
                    return entry.getKey();
                }
            }
            return 0;
        }

        private void handleRound1(String currentSymbol) {
            ArrayList<Card> winCard = getCards();
            winCard.removeIf(card -> !Objects.equals(currentSymbol, card.getSymbol()));
            sortCards(winCard);
            Card winningCard = winCard.get(0);
            int winningClientID = takeWinnerID(winningCard);
            takeCurrentRoom().setPoints(winningClientID,-20);
            takeCurrentRoom().nextTurnNumbered(winningClientID);
        }

        private void handleRound2(String currentSymbol) {

            ArrayList<Card> cards = getCards();
            sortCards(cards);
            Card winningCard = takeCurrentRoom().getFirstCardOnTable();
            int amountOfHearts = 0;
            for(Card card: cards){
                if(Objects.equals(card.getSymbol(), "Hearts"))amountOfHearts++;
                if(Objects.equals(card.getSymbol(), currentSymbol) && card.getValue()>winningCard.getValue()){
                    winningCard=card;
                }
            }
            int winningClientID = takeWinnerID(winningCard);
            takeCurrentRoom().setPoints(winningClientID, -20*amountOfHearts);
            takeCurrentRoom().nextTurnNumbered(winningClientID);
        }
        private void handleRound3(String currentSymbol) {
            ArrayList<Card> cards  = getCards();
            sortCards(cards);
            Card winningCard = takeCurrentRoom().getFirstCardOnTable();
            int amountOfQueens = 0;
            for(Card card: cards){
                if(Objects.equals(card.getValue(), 12))amountOfQueens++;
                if(Objects.equals(card.getSymbol(), currentSymbol) && card.getValue()>winningCard.getValue()){
                    winningCard=card;
                }
            }
            int winningClientID = takeWinnerID(winningCard);
            takeCurrentRoom().setPoints(winningClientID, -60*amountOfQueens);
            takeCurrentRoom().nextTurnNumbered(winningClientID);
        }
        private void handleRound4(String currentSymbol) {
            ArrayList<Card> cards = getCards();
            sortCards(cards);
            Card winningCard = takeCurrentRoom().getFirstCardOnTable();
            int amountOfJacksAndKings = 0;
            for(Card card: cards){
                if(Objects.equals(card.getValue(), 11)||Objects.equals(card.getValue(), 13))amountOfJacksAndKings++;
                if(Objects.equals(card.getSymbol(), currentSymbol) && card.getValue()>winningCard.getValue()){
                    winningCard=card;
                }
            }
            int winningClientID = takeWinnerID(winningCard);
            takeCurrentRoom().setPoints(winningClientID, -30*amountOfJacksAndKings);
            takeCurrentRoom().nextTurnNumbered(winningClientID);
        }
        private void handleRound5(String currentSymbol) {
            ArrayList<Card> cards =  getCards();
            sortCards(cards);
            Card winningCard = takeCurrentRoom().getFirstCardOnTable();
            boolean checkHeartsKing = false;
            for(Card card: cards){
                if(Objects.equals(card.getValue(), 13)&&Objects.equals(card.getSymbol(), "Hearts"))checkHeartsKing=true;
                if(Objects.equals(card.getSymbol(), currentSymbol) && card.getValue()>winningCard.getValue()){
                    winningCard=card;
                }
            }
            int winningClientID = takeWinnerID(winningCard);
            if(checkHeartsKing){
                takeCurrentRoom().setPoints(winningClientID, -150);
                takeCurrentRoom().initializeDeck();
                takeCurrentRoom().dealCards();
                takeCurrentRoom().shuffleDeck();
                takeCurrentRoom().nextRound();
                takeCurrentRoom().randomTurn();
                return;
            }
            takeCurrentRoom().nextTurnNumbered(winningClientID);
        }
        private void handleRound6(String currentSymbol) {
            ArrayList<Card> cards = getCards();
            sortCards(cards);
            Card winningCard = takeCurrentRoom().getFirstCardOnTable();
            for(Card card: cards){
                if(Objects.equals(card.getSymbol(), currentSymbol) && card.getValue()>winningCard.getValue()){
                    winningCard=card;
                }
            }
            int winningClientID = takeWinnerID(winningCard);
            if(takeCurrentRoom().getDeck().size()==28||takeCurrentRoom().getDeck().size()==4)takeCurrentRoom().setPoints(winningClientID, -75);
            takeCurrentRoom().nextTurnNumbered(winningClientID);
        }

        private void handleRound7(String currentSymbol){
            ArrayList<Card> cards = getCards();
            sortCards(cards);
            Card winningCard = takeCurrentRoom().getFirstCardOnTable();
            int amountOfHearts = 0;
            int amountOfQueens = 0;
            int amountOfJacksAndKings = 0;
            int checkHeartsKing = 0;
            for(Card card: cards){
                if(Objects.equals(card.getSymbol(), "Hearts"))amountOfHearts++;
                if(Objects.equals(card.getValue(), 12))amountOfQueens++;
                if(Objects.equals(card.getValue(), 11)||Objects.equals(card.getValue(), 13))amountOfJacksAndKings++;
                if(Objects.equals(card.getValue(), 13)&&Objects.equals(card.getSymbol(), "Hearts"))checkHeartsKing=1;
                if(Objects.equals(card.getSymbol(), currentSymbol) && card.getValue()>winningCard.getValue()){
                    winningCard=card;
                }
            }

            int winningClientID = takeWinnerID(winningCard);
            takeCurrentRoom().setPoints(winningClientID,-20);
            takeCurrentRoom().setPoints(winningClientID, -20*amountOfHearts);
            takeCurrentRoom().setPoints(winningClientID, -60*amountOfQueens);
            takeCurrentRoom().setPoints(winningClientID, -30*amountOfJacksAndKings);
            takeCurrentRoom().setPoints(winningClientID, -150*checkHeartsKing);
            if(takeCurrentRoom().getDeck().size()==28||takeCurrentRoom().getDeck().size()==4)takeCurrentRoom().setPoints(winningClientID, -75);
            takeCurrentRoom().nextTurnNumbered(winningClientID);
        }

        private void sumPoints(){
            int round = takeCurrentRoom().getRound();
            String currentSymbol = takeCurrentRoom().getFirstCardOnTable().getSymbol();
            switch (round) {
                case 1:
                    handleRound1(currentSymbol);
                    break;
                case 2:
                    handleRound2(currentSymbol);
                    break;
                case 3:
                    handleRound3(currentSymbol);
                    break;
                case 4:
                    handleRound4(currentSymbol);
                    break;
                case 5:
                    handleRound5(currentSymbol);
                    break;
                case 6:
                    handleRound6(currentSymbol);
                    break;
                case 7:
                    handleRound7(currentSymbol);
                    break;
                default:
                    System.out.println("BŁAD SERWERA");
            }
        }

        private void removeMainCards(){
            ArrayList<Card> updatedDeck = takeCurrentRoom().getDeck();

            HashMap<Integer,Card> actualPlay = takeCurrentRoom().getActualPlay();

            for (Card card : actualPlay.values()) {
                updatedDeck.removeIf(deckCard -> deckCard.equals(card));
            }

            takeCurrentRoom().setDeck(updatedDeck);
//            takeCurrentRoom().displayDeck();
            takeCurrentRoom().resetActualCards();
        }
        private void checkEndOfRound(){
            if(takeCurrentRoom().getDeck().isEmpty()){
                takeCurrentRoom().initializeDeck();
                takeCurrentRoom().dealCards();
                takeCurrentRoom().shuffleDeck();
//                takeCurrentRoom().displayDeck();
                takeCurrentRoom().nextRound();
                takeCurrentRoom().randomTurn();
            }
        }
        private void game() throws IOException, InterruptedException {
            while(true){
                int chosenValue = in.readInt();
                String chosenSymbol = in.readUTF();
                if( takeCurrentRoom().getClientsID().get(takeCurrentRoom().getTurn()) == clientId){
                    System.out.println(takeCurrentRoom().getFirstCardOnTable().getSymbol());
                    if(takeCurrentRoom().checkActualPlay()==0){
                        Card card = new Card(chosenSymbol,chosenValue);
                        card.setClientID(clientId);
                        takeCurrentRoom().setActualCard(clientId,card);
                        takeCurrentRoom().setFirstCardOnTable(card);
                        takeCurrentRoom().nextTurn();
                    }
                    else if(takeCurrentRoom().checkActualPlay()>=1){
                        if(!validateCardMove(chosenSymbol))continue;
                        Card card = new Card(chosenSymbol,chosenValue);
                        card.setClientID(clientId);
                        takeCurrentRoom().setActualCard(clientId,card);
                        takeCurrentRoom().nextTurn();
                    }
                    broadcastToSameRoomPlayers();
                    if(takeCurrentRoom().checkActualPlay()>=4){
                        sleep(1500);
                        sumPoints();
                        removeMainCards();
                        checkEndOfRound();
                        broadcastToSameRoomPlayers();
                    }
                }
            }
        }


        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                outputStreams.put(clientId, out);

                out.writeInt(clientId);
                out.flush();
                nickname = in.readUTF();

                sendRooms();
                waitOnRoomAndBroadcast();
                startOfGame();
                game();
            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
