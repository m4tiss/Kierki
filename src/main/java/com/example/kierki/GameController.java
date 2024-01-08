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


/**
 * Klasa GameController odpowiada za zarządzanie interfejsem graficznym gry w aplikacji Kierki.
 * Kontroluje wyświetlanie kart, informacji o graczach, rundach, punktacji oraz obsługę czatu.
 */
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


    /**
     * Inicjalizuje główne karty graczy na interfejsie graficznym.
     */
    public void initMainCards() {
        this.mainCards = new ImageView[4];
        mainCards[0] = mainCard1;
        mainCards[1] = mainCard2;
        mainCards[2] = mainCard3;
        mainCards[3] = mainCard4;
        for (int i = 0; i < 4; i++) mainCards[i].setOpacity(1);
    }


    /**
     * Aktualizuje punkty dla każdego gracza na interfejsie graficznym.
     *
     * @param room Obiekt reprezentujący aktualny stan pokoju gry.
     */
    public void updatePoints(Room room){

        points1.setText(String.valueOf(room.getPoints().get(room.getClientsID().get(0))));
        points2.setText(String.valueOf(room.getPoints().get(room.getClientsID().get(1))));
        points3.setText(String.valueOf(room.getPoints().get(room.getClientsID().get(2))));
        points4.setText(String.valueOf(room.getPoints().get(room.getClientsID().get(3))));
    }

    /**
     * Ustawia klienta dla kontrolera gry.
     *
     * @param client Instancja klasy Client reprezentująca klienta gry.
     */
    public void setClient(Client client) {
        this.client = client;
    }



    /**
     * Ustawia nazwy graczy na interfejsie graficznym na podstawie danych z pokoju.
     *
     * @param room Obiekt reprezentujący aktualny stan pokoju gry.
     */
    private void setNicknames(Room room) {
        nickname1.setText(room.getPlayers().get(0));
        nickname2.setText(room.getPlayers().get(1));
        nickname3.setText(room.getPlayers().get(2));
        nickname4.setText(room.getPlayers().get(3));
    }


    /**
     * Aktualizuje informacje o rundzie na interfejsie graficznym.
     *
     * @param room Obiekt reprezentujący aktualny stan pokoju gry.
     */
    private void updateRound(Room room) {
            roundText.setOpacity(1);
            roundNumber.setOpacity(1);
            roundNumber.setText(String.valueOf(room.getRound()));
    }


    /**
     * Aktualizuje informacje o liczbie obecnych graczy na interfejsie graficznym.
     *
     * @param current Aktualna liczba graczy.
     */
    public void updateAmountPlayers(int current) {
        String newText = current + "/" + 4;
        System.out.println(newText);
        Platform.runLater(() -> {
            amountPlayers.setText(newText);
        });
    }

    public void closeGame(Room room) throws InterruptedException, IOException {
        Platform.runLater(() -> {
            clearArrows();
            resetActualCards();
            updateCardFlowPane(null);
            updatePoints(room);
        });
        sleep(3000);
        client.closeGame();
    }


    /**
     * Rozpoczyna grę poprzez inicjalizację głównych kart oraz usuwa powitalny tekst i informację o ilości graczy.
     */
    public void startGame() {
        initMainCards();
        Platform.runLater(() -> {
            mainScene.getChildren().remove(welcomeText);
            mainScene.getChildren().remove(amountPlayers);
        });
    }

    /**
     * Inicjalizuje elementy czatu na interfejsie graficznym.
     */
    private void initChat(){
        mainChat.setOpacity(1);
        inputChat.setOpacity(1);
        chatButton.setOpacity(1);
    }


    /**
     * Aktualizuje czat na interfejsie graficznym na podstawie informacji z pokoju.
     *
     * @param room Obiekt reprezentujący aktualny stan pokoju gry.
     */
    private void updateChat(Room room){
        ArrayList<String> chat = room.getChat();
        ObservableList<String> chatMessages = FXCollections.observableArrayList(chat);
        mainChat.setItems(chatMessages);
        mainChat.scrollTo(chatMessages.size() - 1);
    }


    /**
     * Rysuje aktualną kartę na interfejsie graficznym dla poprzedniego gracza, który wykonał ruch.
     *
     * @param room Obiekt reprezentujący aktualny stan pokoju gry.
     */
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


    /**
     * Obsługuje dodanie wiadomości do czatu po wciśnięciu przycisku.
     *
     * @param event Zdarzenie przycisku.
     * @throws IOException Jeżeli wystąpi błąd wejścia-wyjścia podczas wysyłania wiadomości do serwera.
     */
    @FXML
    void addMessage(ActionEvent event) throws IOException {
        String message = inputChat.getText();
        if(message.equals(""))return;
        client.sendMessage(message);
        inputChat.setText("");
    }

    /**
     * Metoda czyści strzałki na interfejsie graficznym.
     */
    private void clearArrows() {
        arrow1.setOpacity(0);
        arrow2.setOpacity(0);
        arrow3.setOpacity(0);
        arrow4.setOpacity(0);
    }

    /**
     * Aktualizuje strzałki na interfejsie graficznym na podstawie aktualnego gracza, który ma kolej na ruch.
     *
     * @param turn Numer gracza, który ma aktualnie kolej na ruch.
     */
    private void updateArrows(int turn) {
        arrow1.setOpacity(turn == 0 ? 1 : 0);
        arrow2.setOpacity(turn == 1 ? 1 : 0);
        arrow3.setOpacity(turn == 2 ? 1 : 0);
        arrow4.setOpacity(turn == 3 ? 1 : 0);
    }


    /**
     * Rysuje aktualną kartę na interfejsie graficznym dla poprzedniego gracza, który wykonał ruch.
     *
     * @param room Obiekt reprezentujący aktualny stan pokoju gry.
     */
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

    /**
     * Zresetuj karty na stole.
     */
    private void resetActualCards() {
        for (int i = 0; i < 4; i++) {
            String nameCard = "file:cards/reverseCard.png";
            Image cardImage = new Image(nameCard);
            mainCards[i].setImage(cardImage);
        }
    }


    /**
     * Aktualizuje elementy interfejsu graficznego dla bieżącej rozgrywki.
     *
     * @param room     Obiekt reprezentujący aktualny stan pokoju gry.
     * @param clientID Identyfikator klienta.
     */
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


    /**
     * Ustawia karty w rewersie na interfejsie graficznym dla graczy pozostałych w pokoju.
     */
    private void setReverseCards() {
        reverse1.setOpacity(1);
        reverse2.setOpacity(1);
        reverse3.setOpacity(1);
    }



    /**
     * Oblicza ilość kart danego klienta w danym pokoju.
     *
     * @param room     Obiekt reprezentujący aktualny stan pokoju gry.
     * @param clientID Identyfikator klienta.
     * @return Ilość kart klienta w pokoju.
     */
    public int calculateCards(Room room,int clientID) {
        int amountClientCards=0;
        for(Card card : room.getDeck()){
            if(card.getClientID()==clientID)amountClientCards++;
        }
        return amountClientCards;
    }

    /**
     * Aktualizuje karty klienta na interfejsie graficznym.
     *
     * @param room     Obiekt reprezentujący aktualny stan pokoju gry.
     * @param clientID Identyfikator klienta.
     */
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

    /**
     * Obsługuje kliknięcie na kartę, wysyłając ruch do serwera.
     *
     * @param value  Wartość karty.
     * @param symbol Symbol karty.
     * @throws IOException Jeżeli wystąpi błąd wejścia-wyjścia podczas wysyłania ruchu do serwera.
     */
    private void handleCardClick(int value, String symbol) throws IOException {
        client.sendMove(value, symbol);
    }

    /**
     * Sortuje karty klienta według symbolu, a następnie wartości w odwrotnej kolejności.
     *
     * @param clientCards Lista kart klienta do posortowania.
     */
    private void sortCard(ArrayList<Card> clientCards) {
        Comparator<Card> symbolThenValueComparator = Comparator.comparing(Card::getSymbol)
                .thenComparing(Comparator.comparing(Card::getValue).reversed());

        Collections.sort(clientCards, symbolThenValueComparator);
    }


    /**
     * Aktualizuje elementy FlowPane z kartami na interfejsie graficznym.
     *
     * @param room Obiekt reprezentujący aktualny stan pokoju gry.
     */
    private void updateCardFlowPane(Room room) {
        cardArea.getChildren().clear();
        if(room == null)return;
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

    /**
     * Dodaje efekt "hover" do karty na interfejsie graficznym.
     *
     * @param imageView Obiekt ImageView reprezentujący kartę.
     */
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
