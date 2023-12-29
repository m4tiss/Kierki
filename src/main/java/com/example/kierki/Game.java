package com.example.kierki;

import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

public class Game {

    private Stage stage;

    private Room chosenRoom;

    private GameController gameController;
    private FXMLLoader gameLoader;

    public Game(Stage stage,GameController gameController, FXMLLoader gameLoader,Room chosenRoom){
        this.stage=stage;
        this.gameController=gameController;
        this.gameLoader=gameLoader;
        this.chosenRoom=chosenRoom;
    }

    public void updatePlayersInRoom(){
        gameController.updateAmountPlayers(chosenRoom.getAmountOfPlayers());
    }
}
