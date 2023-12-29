package com.example.kierki;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;


public class App extends Application {

    public App(){}

    @Override
    public void start(Stage stage) throws IOException, InterruptedException {
        FXMLLoader loginLoader = new FXMLLoader(App.class.getResource("nicknamePanel.fxml"));
        Scene login = new Scene(loginLoader.load(), 1280, 720);
        LoginController loginController = loginLoader.getController();


        FXMLLoader roomsLoader = new FXMLLoader(App.class.getResource("rooms.fxml"));
        roomsLoader.load();
        RoomsController roomsController = roomsLoader.getController();


        FXMLLoader gameLoader = new FXMLLoader(App.class.getResource("game.fxml"));
        gameLoader.load();
        GameController gameController = gameLoader.getController();

        Client client = new Client(stage,loginController,roomsController, roomsLoader,gameController,gameLoader);

        stage.setTitle("Kierki");

        loginController.setClient(client);
        roomsController.setClient(client);
        gameController.setClient(client);


        stage.setScene(login);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}