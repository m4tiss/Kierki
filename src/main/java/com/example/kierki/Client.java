package com.example.kierki;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;

import static java.lang.Thread.sleep;

public class Client extends Application {

    private String nickname;

    public Client() {
        nickname = "";
    }

    public void display(int das) {
        System.out.println(das);
    }

    public void setNickname(String nick) {
        nickname = nick;
    }

    @Override
    public void start(Stage stage) throws IOException, InterruptedException {
        FXMLLoader fxmlLoader = new FXMLLoader(Client.class.getResource("nicknamePanel.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1280, 720);

        ClientController controller = fxmlLoader.getController();
        controller.setStage(stage);

        stage.setTitle("Kierki");
        controller.setClient(this);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}