package com.example.kierki;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientController {

    private static final int PORT = 8888;
    private static int clientId;
    private static ObjectInputStream in;
    private static ObjectOutputStream out;
    private Socket socket;
    private Stage stage;
    private Client client;

    @FXML
    private TextField nicknameInput;

    public void setStage(Stage stage) {
        this.stage = stage;
        client = (Client) stage.getUserData();
    }

    private void initObjectsAndVariables(Socket clientSocket) throws IOException {
        in = new ObjectInputStream(clientSocket.getInputStream());
        out = new ObjectOutputStream(clientSocket.getOutputStream());
    }

    public void setClient(Client client) {
        this.client = client;
    }

    @FXML
    protected void onConfirmButtonClick(ActionEvent event) throws IOException {
        client.setNickname(nicknameInput.getText());

        try {
            socket = new Socket("localhost", PORT);
            initObjectsAndVariables(socket);

            clientId = in.readInt();
            client.display(clientId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
