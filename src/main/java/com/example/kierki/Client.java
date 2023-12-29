package com.example.kierki;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


import java.util.concurrent.*;

public class Client {

    private static final int PORT = 8888;
    private String nickname;
    Socket clientSocket;
    ObjectOutputStream out;
    Stage stage;
    LoginController loginController;
    RoomsController roomsController;
    GameController gameController;
    FXMLLoader roomsLoader;
    FXMLLoader gameLoader;

    public Client(Stage stage, LoginController loginController, RoomsController roomsController, FXMLLoader roomsLoader, GameController gameController, FXMLLoader gameLoader) {
        this.stage = stage;
        this.loginController = loginController;
        this.roomsLoader = roomsLoader;
        this.roomsController = roomsController;
        this.gameLoader = gameLoader;
        this.gameController = gameController;
    }
    public void setNickname(String nickname) throws IOException {
        this.nickname = nickname;
        Scene roomsScene = new Scene(roomsLoader.getRoot(), 1280, 720);
        stage.setScene(roomsScene);
        out.writeUTF(nickname);
        out.flush();

    }

    public void startReceiver() {
        ObjectInputStream in = null;
        try {
            clientSocket = new Socket("localhost", PORT);
            in = new ObjectInputStream(clientSocket.getInputStream());
            out = new ObjectOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        ReceiverClient receiver = new ReceiverClient(executorService, in, clientSocket,gameController,roomsController);
        executorService.schedule(receiver, 0, TimeUnit.SECONDS);
    }


    public static void main(String[] args) {
    }

}

//    private void takeIDSendNickname() throws IOException {
//        clientId = in.readInt();
//        out.writeUTF(nickname);
//        out.flush();
//    }



//    public void sendChosenRoom(Integer room) throws IOException, ClassNotFoundException {
//        out.writeObject(room);
//        out.flush();
//        out.reset();
//        chosenRoom =  (Room) in.readObject();
//    }
//    public void joinToRoom() {
//        Scene gameScene = new Scene(gameLoader.getRoot(), 1280, 720);
//        stage.setScene(gameScene);
//        Game game = new Game(stage, gameController, gameLoader,chosenRoom);
//        game.updatePlayersInRoom();
//
//    }

//    public void start() throws IOException, ClassNotFoundException {
//        connectToServer();
//        takeIDSendNickname();
//        takeRooms();
//    }
