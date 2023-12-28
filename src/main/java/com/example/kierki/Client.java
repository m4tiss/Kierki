package com.example.kierki;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {

    private String nickname;
    LoginController loginController;
    RoomsController roomsController;
    GameController gameController;
    Stage stage;

    public Client(Stage stage, LoginController loginController, RoomsController roomsController, GameController gameController) {
        this.stage = stage;
        this.loginController = loginController;
        this.roomsController = roomsController;
        this.gameController = gameController;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public static void main(String[] args) {
        try(Socket clientSocket = new Socket("localhost", 8888)) {
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
