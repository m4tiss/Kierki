package com.example.kierki;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ScheduledExecutorService;

public class ReceiverClient implements Runnable {

    public static int clientId;
    private  ObjectInputStream in;
    private  ObjectOutputStream out;
    private  Socket socket;
    private ScheduledExecutorService executor;
    private Room chosenRoom;
    private GameController gameController;


    public ReceiverClient(ScheduledExecutorService exec, ObjectInputStream in, ObjectOutputStream out,Socket socket,Room chosenRoom, GameController gameController) {
        this.executor = exec;
        this.in = in;
        this.out = out;
        this.socket = socket;
        this.chosenRoom = chosenRoom;
        this.gameController = gameController;
    }

    @Override
    public void run() {
        try {
            in.readInt();
            chosenRoom.addPlayer("og");
            gameController.updateAmountPlayers(chosenRoom.getAmountOfPlayers());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
