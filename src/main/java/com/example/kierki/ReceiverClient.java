package com.example.kierki;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.concurrent.ScheduledExecutorService;

public class ReceiverClient implements Runnable {

    public static int clientId;
    private  ObjectInputStream in;
    private  Socket socket;
    private ScheduledExecutorService executor;
    private GameController gameController;
    private RoomsController roomsController;


    public ReceiverClient(ScheduledExecutorService exec, ObjectInputStream in,Socket socket, GameController gameController,RoomsController roomsController) {
        this.executor = exec;
        this.in = in;
        this.socket = socket;
        this.gameController = gameController;
        this.roomsController = roomsController;
    }
        private void takeRooms() throws ClassNotFoundException, IOException {
        int amountRooms = in.readInt();
        for (int i = 0; i < amountRooms; i++) {
            Integer idRoom =  in.readInt();
            Room room = (Room) in.readObject();
            roomsController.addRoom(idRoom,room.getRoomName(), room.getAmountOfPlayers());
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
    }

    @Override
    public void run() {
        try {
            takeRooms();
            checkOnOther();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        while(true){

        }
    }
}
