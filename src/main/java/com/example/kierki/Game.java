package com.example.kierki;

import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

public class Game {

    private Stage stage;

    private GameController gameController;
    private FXMLLoader gameLoader;

    public Game(Stage stage,GameController gameController, FXMLLoader gameLoader){
        this.stage=stage;
        this.gameController=gameController;
        this.gameLoader=gameLoader;
    }
}
