package com.example.kierki;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.shape.Polyline;
import javafx.util.Duration;


import java.io.IOException;
import java.util.ArrayList;

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
    private FlowPane cardArea;


    @FXML
    private ImageView reverse1;

    @FXML
    private ImageView reverse2;

    @FXML
    private ImageView reverse3;


    @FXML
    private ImageView mainCard1;

    @FXML
    private ImageView mainCard2;

    @FXML
    private ImageView mainCard3;

    @FXML
    private ImageView mainCard4;


    @FXML
    private Label nickname1;

    @FXML
    private Label nickname2;

    @FXML
    private Label nickname3;

    @FXML
    private Label nickname4;

    @FXML
    private Label amountPlayers;
    @FXML
    private Label welcomeText;
    private Client client;
    private ImageView[] cardImageViews;

    public void setClient(Client client) {
        this.client = client;
    }

    private void setNicknames(Room room){
        nickname1.setText(room.getPlayers().get(0));
        nickname2.setText(room.getPlayers().get(1));
        nickname3.setText(room.getPlayers().get(2));
        nickname4.setText(room.getPlayers().get(3));
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
            setReverseCards();
            updateArrows(room.getTurn());
            setNicknames(room);
            initializeCards();
            updateCardFlowPane(room);
        });
    }

    private void updateArrows(int turn){
        arrow1.setOpacity(turn == 0 ? 1 : 0);
        arrow2.setOpacity(turn == 1 ? 1 : 0);
        arrow3.setOpacity(turn == 2 ? 1 : 0);
        arrow4.setOpacity(turn == 3 ? 1 : 0);
    }

    public void game(Room room){
        Platform.runLater(() -> {
            updateArrows(room.getTurn());
        });
    }

    private void setReverseCards(){
        reverse1.setOpacity(1);
        reverse2.setOpacity(1);
        reverse3.setOpacity(1);
    }
    public void initializeCards() {
        cardImageViews = new ImageView[13];
        for (int i = 0; i < cardImageViews.length; i++) {
            cardImageViews[i] = new ImageView();
            cardImageViews[i].setFitWidth(80);
            cardImageViews[i].setPreserveRatio(true);
            cardImageViews[i].cursorProperty().setValue(Cursor.HAND);
            addHoverEffect(cardImageViews[i]);
        }
    }
    private void handleCardClick(int value, String symbol) throws IOException {
        client.sendMove(value,symbol);
    }
    private void updateCardFlowPane(Room room) {
        cardArea.getChildren().clear();
        for (ImageView cardImageView : cardImageViews) {
            cardArea.getChildren().add(cardImageView);
        }

        ArrayList<Card> clientCards = room.getCardsFromClientID(client.getID());
        for (int i = 0; i < clientCards.size(); i++) {
            String nameCard = "file:cards/"+clientCards.get(i).getValue()+clientCards.get(i).getSymbol()+".png";
            Image cardImage =  new Image(nameCard);
            cardImageViews[i].setImage(cardImage);
            int finalI = i;
            cardImageViews[i].setOnMouseClicked(event -> {
                try {
                    handleCardClick(clientCards.get(finalI).getValue(),clientCards.get(finalI).getSymbol());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
    private void addHoverEffect(ImageView imageView) {
        final double scaleFactorOnHover = 1.2;
        final Duration duration = Duration.millis(200);

        ScaleTransition hoverUpTransition = new ScaleTransition(duration, imageView);
        hoverUpTransition.setToX(scaleFactorOnHover);
        hoverUpTransition.setToY(scaleFactorOnHover);

        ScaleTransition hoverDownTransition = new ScaleTransition(duration, imageView);
        hoverDownTransition.setToX(1.0);
        hoverDownTransition.setToY(1.0);

        imageView.setOnMouseEntered(event -> {
            hoverUpTransition.playFromStart();
        });

        imageView.setOnMouseExited(event -> {
            hoverDownTransition.playFromStart();
        });
    }
}
