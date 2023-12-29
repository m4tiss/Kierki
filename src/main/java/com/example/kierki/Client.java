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
    GameController gameController;
    FXMLLoader gameLoader;
    Stage stage;
    ObjectInputStream in;
    ObjectOutputStream out;
    Socket clientSocket;
    public int clientId;

    public Client(Stage stage, LoginController loginController, RoomsController roomsController, FXMLLoader roomsLoader, GameController gameController, FXMLLoader gameLoader) {
        this.stage = stage;
        this.loginController = loginController;
        this.roomsLoader = roomsLoader;
        this.roomsController = roomsController;
        this.gameLoader = gameLoader;
        this.gameController = gameController;
    }
    public void setNickname(String nickname) throws IOException, ClassNotFoundException {
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
    private void takeRooms() throws IOException, ClassNotFoundException {
        int amountRooms = in.readInt();
        for (int i = 0; i < amountRooms; i++) {
            Integer key = (Integer) in.readObject();
            Room value = (Room) in.readObject();

            roomsController.addRoom(value.getRoomName(),value.getAmountOfPlayers());
        }
    }
    public void joinToRoom(){

    }
    public void start() throws IOException, ClassNotFoundException {
        connectToServer();
        takeIDSendNickname();
        takeRooms();

    }
    public static void main(String[] args) {
    }
}
