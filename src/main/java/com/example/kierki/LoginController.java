package com.example.kierki;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;


import java.io.IOException;
public class LoginController {
    private Client client;

    @FXML
    private TextField nicknameInput;
    public void setClient(Client client) {
        this.client = client;
    }

    @FXML
    protected void onConfirmButtonClick(ActionEvent event) throws IOException {
        client.setNickname(nicknameInput.getText());
    }
}
