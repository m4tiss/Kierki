package com.example.kierki;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;


import java.io.IOException;

public class GameController {
    @FXML
    private AnchorPane mainScene;

    @FXML
    private Label amountPlayers;
    @FXML
    private Label welcomeText;
    private Client client;

    public void setClient(Client client) {
        this.client = client;
    }

    public void updateAmountPlayers(int current) {
        String newText = current + "/" + 4;
        System.out.println(newText);
        Platform.runLater(() -> {
                amountPlayers.setText(newText);
        });
    }

    public void startGame(){
        Platform.runLater(() -> {
            mainScene.getChildren().remove(welcomeText);
            mainScene.getChildren().remove(amountPlayers);
        });
    }
}
