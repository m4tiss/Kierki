package com.example.kierki;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

import java.io.IOException;

public class RoomsController {
    private Client client;

    @FXML
    private FlowPane roomsBox;

    public void addRoom(Integer idRoom,String roomName, int amountPlayers) {
        Platform.runLater(() -> {
            Button roomButton = new Button();
            roomButton.setText("Pokój: " + roomName + "           " + "Graczy: " + amountPlayers + "/4");
            roomButton.setMinHeight(80);
            roomButton.setMinWidth(60);
            roomButton.setOnAction(event -> {
                System.out.println("Kliknięto pokój: " + roomName);
                try {
                    client.sendChosenRoom(idRoom);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                client.joinToRoom();
            });
            roomsBox.getChildren().add(roomButton);
        });
    }
    public void setClient(Client client) {
        this.client = client;
    }
}
