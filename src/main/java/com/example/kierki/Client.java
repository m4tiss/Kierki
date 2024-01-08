package com.example.kierki;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


import java.util.concurrent.*;


/** Klasa Client reprezentuje klienta gry kierki.
 * Jest odpowiedzialna za obsługę interfejsu użytkownika, komunikację z serwerem oraz zarządzanie scenami w aplikacji.
 *
 *  @version 1.0
 */

public class Client {

    private static final int PORT = 8888;
    private String nickname;

    private int clientID;

    Socket clientSocket;
    ObjectOutputStream out;

    Stage stage;
    LoginController loginController;
    RoomsController roomsController;
    GameController gameController;
    FXMLLoader roomsLoader;
    FXMLLoader gameLoader;

    /**
     * Konstruktor klasy Client, inicjalizuje obiekt klienta.
     *
     * @param stage główny kontener JavaFX
     * @param loginController kontroler do interfejsu logowania
     * @param roomsController kontroler do interfejsu pokojów
     * @param roomsLoader obiekt do wczytywania interfejsu pokojów
     * @param gameController kontroler do interfejsu gry
     * @param gameLoader obiekt do wczytywania interfejsu gry
     */
    public Client(Stage stage, LoginController loginController, RoomsController roomsController, FXMLLoader roomsLoader, GameController gameController, FXMLLoader gameLoader) {
        this.stage = stage;
        this.loginController = loginController;
        this.roomsLoader = roomsLoader;
        this.roomsController = roomsController;
        this.gameLoader = gameLoader;
        this.gameController = gameController;
    }


    /**
     * Ustawia pseudonim gracza i przechodzi do sceny pokojów.
     *
     * @param nickname pseudonim gracza
     * @throws IOException wyjątek wejścia/wyjścia
     */
    public void setNickname(String nickname) throws IOException {
        this.nickname = nickname;
        Scene roomsScene = new Scene(roomsLoader.getRoot(), 1280, 720);
        stage.setScene(roomsScene);
        out.writeUTF(nickname);
        out.flush();
    }

    /**
     * Pobiera identyfikator klienta.
     *
     * @return identyfikator klienta
     */
    public int getID() {
        return clientID;
    }


    /**
     * Wysyła ruch gracza (zagranie karty) do serwera.
     *
     * @param value wartość karty
     * @param symbol symbol karty
     * @throws IOException wyjątek wejścia/wyjścia
     */
    public void sendMove(int value, String symbol) throws IOException {
        out.writeInt(value);
        out.flush();
        out.writeUTF(symbol);
        out.flush();
    }

    /**
     * Wysyła wiadomość czatu do serwera.
     *
     * @param message treść wiadomości
     * @throws IOException wyjątek wejścia/wyjścia
     */
    public void sendMessage(String message) throws IOException {
        out.writeInt(-1);
        out.flush();
        String finalMessage = nickname+": " + message;
        out.writeUTF(finalMessage);
        out.flush();
    }


    /**
     * Wysyła wybrany pokój do serwera.
     *
     * @param idRoom identyfikator wybranego pokoju
     * @throws IOException wyjątek wejścia/wyjścia
     */
    public void sendChosenRoom(Integer idRoom) throws IOException {
        out.writeObject(idRoom);
        out.flush();
    }


    /**
     * Dołącza do wybranego pokoju i przełącza się do sceny gry.
     */
    public void joinToRoom() {
        Scene gameScene = new Scene(gameLoader.getRoot(), 1344, 832);
        stage.setScene(gameScene);
    }

    /**
     * Informuje serwer o chęci dodania nowego pokoju.
     *
     * @throws IOException wyjątek wejścia/wyjścia
     */
    public void addRoom() throws IOException {
        out.writeObject(-1);
        out.flush();
    }


    protected void setStreams(ObjectOutputStream outer){
            out=outer;
    }

    /**
     * Inicjalizuje połączenie z serwerem i uruchamia wątek odbierający komunikaty.
     */
    public void startReceiver() {
        ObjectInputStream in = null;
        try {
            clientSocket = new Socket("localhost", PORT);
            in = new ObjectInputStream(clientSocket.getInputStream());
            out = new ObjectOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        ReceiverClient receiver = new ReceiverClient(this,executorService, in, clientSocket, gameController, roomsController);
        executorService.schedule(receiver, 0, TimeUnit.SECONDS);
    }


    /**
     * Ustawia identyfikator klienta.
     *
     * @param clientID identyfikator klienta
     */
    public void setClientID(int clientID){
        this.clientID=clientID;
    }

}



