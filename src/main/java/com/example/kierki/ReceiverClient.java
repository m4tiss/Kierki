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
        private void takeRooms() throws IOException, ClassNotFoundException, IOException {
        int amountRooms = in.readInt();
        for (int i = 0; i < amountRooms; i++) {
            Integer key = (Integer) in.readObject();
            Room value = (Room) in.readObject();
            roomsController.addRoom(key,value.getRoomName(), value.getAmountOfPlayers());
        }
    }

    @Override
    public void run() {
        try {
            takeRooms();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        while(true){}
    }
}
