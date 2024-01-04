package com.example.kierki;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.concurrent.ScheduledExecutorService;

public class ReceiverClient implements Runnable {

    public static int clientId;
    private Client client;
    private  ObjectInputStream in;
    private  Socket socket;
    private ScheduledExecutorService executor;
    private GameController gameController;
    private RoomsController roomsController;


    public ReceiverClient(Client client,ScheduledExecutorService exec, ObjectInputStream in,Socket socket, GameController gameController,RoomsController roomsController) {
        this.client=client;
        this.executor = exec;
        this.in = in;
        this.socket = socket;
        this.gameController = gameController;
        this.roomsController = roomsController;
    }
    private void setID() throws ClassNotFoundException, IOException {
        clientId = in.readInt();
        client.setClientID(clientId);
    }
        private void takeRooms() throws ClassNotFoundException, IOException {
        int amountRooms = in.readInt();
        for (int i = 0; i < amountRooms; i++) {
            Integer idRoom =  in.readInt();
            Room room = (Room) in.readObject();
            roomsController.addRoom(room);
        }
    }

    private void checkOnOther() throws ClassNotFoundException, IOException {
        int numberOfPlayers = 0;
        while(numberOfPlayers!=4){
            numberOfPlayers = in.readInt();
            System.out.println(numberOfPlayers);
            gameController.updateAmountPlayers(numberOfPlayers);
        }
        System.out.println("działa");
        gameController.startGame();
    }

    private void startOfGame() throws IOException, ClassNotFoundException {
        Room room = (Room) in.readObject();
        System.out.println(room);
        gameController.drawGame(room,clientId);
    }

    private void game() throws IOException, ClassNotFoundException {
        Room room = (Room) in.readObject();
        gameController.game(room,clientId);
    }
    @Override
    public void run() {
        try {
            setID();
            takeRooms();
            checkOnOther();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            startOfGame();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        while(true){
            try {
                game();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
