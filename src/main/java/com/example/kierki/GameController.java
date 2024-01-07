package com.example.kierki;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.shape.Polyline;
import javafx.util.Duration;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static java.lang.Thread.sleep;

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

    @FXML
    private Label points1;

    @FXML
    private Label points2;

    @FXML
    private Label points3;

    @FXML
    private Label points4;

    @FXML
    private Label roundText;

    @FXML
    private Label roundNumber;

    @FXML
    private ListView<String> mainChat;
    @FXML
    private Button chatButton;

    @FXML
    private TextField inputChat;

    private Client client;
    private ImageView[] cardImageViews;

    private ImageView[] mainCards;

    public void initMainCards() {
        this.mainCards = new ImageView[4];
        mainCards[0] = mainCard1;
        mainCards[1] = mainCard2;
        mainCards[2] = mainCard3;
        mainCards[3] = mainCard4;
        for (int i = 0; i < 4; i++) mainCards[i].setOpacity(1);
    }

    public void updatePoints(Room room){

        points1.setText(String.valueOf(room.getPoints().get(room.getClientsID().get(0))));
        points2.setText(String.valueOf(room.getPoints().get(room.getClientsID().get(1))));
        points3.setText(String.valueOf(room.getPoints().get(room.getClientsID().get(2))));
        points4.setText(String.valueOf(room.getPoints().get(room.getClientsID().get(3))));
    }
    public void setClient(Client client) {
        this.client = client;
    }

    private void setNicknames(Room room) {
        nickname1.setText(room.getPlayers().get(0));
        nickname2.setText(room.getPlayers().get(1));
        nickname3.setText(room.getPlayers().get(2));
        nickname4.setText(room.getPlayers().get(3));
    }

    private void updateRound(Room room) {
            roundText.setOpacity(1);
            roundNumber.setOpacity(1);
            roundNumber.setText(String.valueOf(room.getRound()));
    }

    public void updateAmountPlayers(int current) {
        String newText = current + "/" + 4;
        System.out.println(newText);
        Platform.runLater(() -> {
            amountPlayers.setText(newText);
        });
    }

    public void startGame() {
        initMainCards();
        Platform.runLater(() -> {
            mainScene.getChildren().remove(welcomeText);
            mainScene.getChildren().remove(amountPlayers);
        });
    }

    private void initChat(){
        mainChat.setOpacity(1);
        inputChat.setOpacity(1);
        chatButton.setOpacity(1);
    }

    private void updateChat(Room room){
        ArrayList<String> chat = room.getChat();
        ObservableList<String> chatMessages = FXCollections.observableArrayList(chat);
        mainChat.setItems(chatMessages);
        mainChat.scrollTo(chatMessages.size() - 1);
    }

    public void drawGame(Room room,int clientID) {

        Platform.runLater(() -> {
            setReverseCards();
            initChat();
            updateChat(room);
            updateArrows(room.getTurn());
            updateRound(room);
            setNicknames(room);
            updateCards(room,clientID);
            updateCardFlowPane(room);
            updatePoints(room);
        });
    }

    @FXML
    void addMessage(ActionEvent event) throws IOException {
        String message = inputChat.getText();
        if(message.equals(""))return;
        client.sendMessage(message);
        inputChat.setText("");
    }
    private void updateArrows(int turn) {
        arrow1.setOpacity(turn == 0 ? 1 : 0);
        arrow2.setOpacity(turn == 1 ? 1 : 0);
        arrow3.setOpacity(turn == 2 ? 1 : 0);
        arrow4.setOpacity(turn == 3 ? 1 : 0);
    }

    private void drawActualCard(Room room) {
        int previousTurn;
        if (room.getTurn() == 0) previousTurn = 3;
        else previousTurn = room.getTurn() - 1;
        Card card = room.getActualCard(room.getClientsID().get(previousTurn));

        if (card != null) {
            String nameCard = "file:cards/" + card.getValue() + card.getSymbol() + ".png";
            Image cardImage = new Image(nameCard);
            mainCards[previousTurn].setImage(cardImage);
            System.out.println("rysuje dla:"+previousTurn);
        }
    }

    private void resetActualCards() {
        for (int i = 0; i < 4; i++) {
            String nameCard = "file:cards/reverseCard.png";
            Image cardImage = new Image(nameCard);
            mainCards[i].setImage(cardImage);
        }
    }

    public void game(Room room,int clientID) {
        Platform.runLater(() -> {
            System.out.println("ILOŚĆ KART W DECKU:" + room.getDeck().size());
            if (room.checkActualPlay() == 0)resetActualCards();
            if (room.checkActualPlay() != 0)drawActualCard(room);
            if (room.checkActualPlay() != 4)updateArrows(room.getTurn());
            updateCards(room,clientID);
            updateCardFlowPane(room);
            updatePoints(room);
            updateRound(room);
            updateChat(room);
        });
    }

    private void setReverseCards() {
        reverse1.setOpacity(1);
        reverse2.setOpacity(1);
        reverse3.setOpacity(1);
    }

    public int calculateCards(Room room,int clientID) {
        int amountClientCards=0;
        for(Card card : room.getDeck()){
            if(card.getClientID()==clientID)amountClientCards++;
        }
        return amountClientCards;
    }
    public void updateCards(Room room,int clientID) {
        cardImageViews = null;
        int size = calculateCards(room,clientID);
        cardImageViews = new ImageView[size];
        for (int i = 0; i < cardImageViews.length; i++) {
            cardImageViews[i] = new ImageView();
            cardImageViews[i].setFitWidth(80);
            cardImageViews[i].setPreserveRatio(true);
            cardImageViews[i].cursorProperty().setValue(Cursor.HAND);
            addHoverEffect(cardImageViews[i]);
        }
    }

    private void handleCardClick(int value, String symbol) throws IOException {
        client.sendMove(value, symbol);
    }

    private void sortCard(ArrayList<Card> clientCards) {
        Comparator<Card> symbolThenValueComparator = Comparator.comparing(Card::getSymbol)
                .thenComparing(Comparator.comparing(Card::getValue).reversed());

        Collections.sort(clientCards, symbolThenValueComparator);
    }

    private void updateCardFlowPane(Room room) {
        cardArea.getChildren().clear();
        for (ImageView cardImageView : cardImageViews) {
            cardArea.getChildren().add(cardImageView);
        }

        ArrayList<Card> clientCards = room.getCardsFromClientID(client.getID());
        sortCard(clientCards);

        for (int i = 0; i < clientCards.size(); i++) {
            String nameCard = "file:cards/" + clientCards.get(i).getValue() + clientCards.get(i).getSymbol() + ".png";
            Image cardImage = new Image(nameCard);
            cardImageViews[i].setImage(cardImage);
            int finalI = i;
            cardImageViews[i].setOnMouseClicked(event -> {
                try {
                    handleCardClick(clientCards.get(finalI).getValue(), clientCards.get(finalI).getSymbol());
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
