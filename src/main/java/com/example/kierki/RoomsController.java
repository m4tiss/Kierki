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

    @FXML
    void addRoomButton(ActionEvent event) throws IOException {
    client.addRoom();
    }

        public void addRoom(Room room) {
        Platform.runLater(() -> {
            Button roomButton = new Button();
            roomButton.setText("Pokój: " + room.getRoomName() + "           " + "Graczy: " + room.getAmountOfPlayers() + "/4");
            roomButton.setMinHeight(80);
            roomButton.setMinWidth(60);

            if(room.getAmountOfPlayers()>=4)roomButton.setDisable(true);

            roomButton.setOnAction(event -> {
                try {
                    client.sendChosenRoom(room.getIdRoom());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                client.joinToRoom();
            });
            roomsBox.getChildren().add(roomButton);
        });
    }

    public void clearRooms(){
        Platform.runLater(() -> {
        roomsBox.getChildren().clear();
        });
    }
    public void setClient(Client client) {
        this.client = client;
    }
}
