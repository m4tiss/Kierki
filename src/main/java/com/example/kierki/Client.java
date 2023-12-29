package com.example.kierki;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {

    private static final int PORT = 8888;
    private String nickname;
    LoginController loginController;
    RoomsController roomsController;

    FXMLLoader roomsLoader;

//    GameController gameController;
    Stage stage;

    ObjectInputStream in;
    ObjectOutputStream out;
    Socket clientSocket;

    public int clientId;

    public Client(Stage stage, LoginController loginController, RoomsController roomsController, FXMLLoader roomsLoader) {
        this.stage = stage;
        this.loginController = loginController;
        this.roomsLoader = roomsLoader;
//        this.roomsController = roomsController;
//        this.gameController = gameController;
    }
    public void setNickname(String nickname) throws IOException {
        this.nickname = nickname;
        Scene roomsScene = new Scene(roomsLoader.getRoot(), 1280, 720);
        stage.setScene(roomsScene);
        start();
    }
    private void connectToServer() {
        try {
            clientSocket = new Socket("localhost", PORT);
            in = new ObjectInputStream(clientSocket.getInputStream());
            out = new ObjectOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void takeIDSendNickname() throws IOException {
        clientId = in.readInt();
        out.writeUTF(nickname);
        out.flush();
    }

    private int takeRooms() throws IOException {
        in.readInt();
    }
    public void start() throws IOException {
        connectToServer();
        takeIDSendNickname();
        int rooms = takeRooms();
    }
    public static void main(String[] args) {
    }
}
