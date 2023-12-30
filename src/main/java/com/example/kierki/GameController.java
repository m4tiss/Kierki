package com.example.kierki;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Box;
import javafx.scene.shape.Polyline;


import java.io.IOException;

public class GameController {
    @FXML
    private AnchorPane mainScene;

    @FXML
    private Polyline arrow1;

    @FXML
    private Polyline arrow2;

    @FXML
    private Polyline arrow3;

    @FXML
    private Polyline arrow4;


    @FXML
    private Box otherBox1;

    @FXML
    private Box otherBox2;

    @FXML
    private Box otherBox3;

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

    public void drawGame(Room room){
        System.out.println("gotowy");
        System.out.println("turan"+room.getTurn());

        Platform.runLater(() -> {

            otherBox1.setOpacity(1);
            otherBox2.setOpacity(1);
            otherBox3.setOpacity(1);

            int turn = room.getTurn();
            arrow1.setOpacity(turn == 0 ? 1 : 0);
            arrow2.setOpacity(turn == 1 ? 1 : 0);
            arrow3.setOpacity(turn == 2 ? 1 : 0);
            arrow4.setOpacity(turn == 3 ? 1 : 0);
        });

    }
}
