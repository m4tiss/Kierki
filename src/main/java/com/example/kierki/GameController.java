package com.example.kierki;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.io.IOException;

public class GameController {
    private Client client;
    public void setClient(Client client) {
        this.client = client;
    }
}
