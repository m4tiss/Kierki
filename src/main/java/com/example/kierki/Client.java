package com.example.kierki;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {

    private String nickname;
    LoginController loginController;
//    RoomsController roomsController;
//    GameController gameController;
    Stage stage;

    ObjectInputStream in;
    ObjectOutputStream out;
    Socket clientSocket;

    public int clientId;

    public Client(Stage stage, LoginController loginController) {
        this.stage = stage;
        this.loginController = loginController;
//        this.roomsController = roomsController;
//        this.gameController = gameController;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
        start();
    }
    public void start(){
        connectToServer();
    }

    private void connectToServer() {
        try {
            clientSocket = new Socket("localhost", 8888);
            in = new ObjectInputStream(clientSocket.getInputStream());
            out = new ObjectOutputStream(clientSocket.getOutputStream());

            clientId = in.readInt();
            out.writeUTF(nickname);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
    }
}
