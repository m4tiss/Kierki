package com.example.kierki;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;

import java.awt.Color;

/**
 * Klasa RoomsController zarządza logiką funkcjonalności związaną z pokojami w aplikacji Kierki.
 * Ta klasa wykorzystuje JavaFX do komponentów interfejsu graficznego
 */
public class RoomsController {

    /**
     * Instancja Client odpowiedzialna za obsługę komunikacji z serwerem.
     */
    private Client client;

    /**
     * FlowPane reprezentujący kontener dla przycisków pokojów.
     */
    @FXML
    private FlowPane roomsBox;

    /**
     * Obsługuje zdarzenie kliknięcia przycisku dodawania pokoju przez użytkownika.
     *
     * @param event Zdarzenie ActionEvent wywołane kliknięciem przycisku dodawania pokoju.
     * @throws IOException Jeśli wystąpi błąd wejścia-wyjścia podczas procesu dodawania pokoju.
     */
    @FXML
    void addRoomButton(ActionEvent event) throws IOException {
        client.addRoom();
    }

    /**
     * Dodaje przycisk reprezentujący pokój do interfejsu graficznego.
     * Ustawia odpowiednie informacje o pokoju, takie jak nazwa pokoju, liczba graczy, czy jest pełny.
     *
     * @param room Obiekt reprezentujący pokój do dodania.
     */
    public void addRoom(Room room) {
        Platform.runLater(() -> {
            Button roomButton = new Button();
            roomButton.setText("Pokój#" + room.getIdRoom() + "           " + "Graczy: " + room.getAmountOfPlayers() + "/4");
            roomButton.setStyle(
                    "-fx-background-color: green; " +
                            "-fx-text-fill: white; " +
                            "-fx-border-radius: 20px;" +
                            "-fx-font-size: 14px;" +
                            "-fx-cursor: hand;"
            );
            roomButton.setPrefHeight(80);
            roomButton.setPrefWidth(300);

            if (room.getAmountOfPlayers() >= 4) {
                roomButton.setStyle("-fx-background-color: red;" +
                        "-fx-text-fill: white; " +
                        "-fx-border-radius: 20px;" +
                        "-fx-font-size: 14px;");
                roomButton.setDisable(true);
            }

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

    /**
     * Czyści listę pokojów z interfejsu graficznego.
     */
    public void clearRooms() {
        Platform.runLater(() -> {
            roomsBox.getChildren().clear();
        });
    }

    /**
     * Ustawia instancję Client dla RoomsController.
     *
     * @param client Instancja Client do ustawienia.
     */
    public void setClient(Client client) {
        this.client = client;
    }
}
