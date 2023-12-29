package com.example.kierki;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;

public class GameController {
    private Client client;
    @FXML
    private Label amountPlayers;
    public void setClient(Client client) {
        this.client = client;
    }

    public void updateAmountPlayers(int current) {
        String newText = current + "/" + 4;
        amountPlayers.setText(newText);
    }
}
