package com.example.kierki;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Klasa ReceiverClient odpowiedzialna jest za odbieranie informacji od serwera w aplikacji Kierki
 * oraz obsługę komunikacji między klientem a serwerem.
 * Implementuje interfejs Runnable, co umożliwia jej uruchamianie w osobnym wątku.
 */
public class ReceiverClient implements Runnable {

    public static int clientId;
    private Client client;
    private ObjectInputStream in;
    private Socket socket;
    private ScheduledExecutorService executor;
    private GameController gameController;
    private RoomsController roomsController;


    /**
     * Konstruktor klasy ReceiverClient inicjalizuje obiekt ReceiverClient z niezbędnymi parametrami.
     *
     * @param client          Instancja klasy Client reprezentująca klienta aplikacji.
     * @param exec            Zaplanowane zadania do wykonania okresowo w ramach executora.
     * @param in              Obiekt InputStream do odbierania obiektów od serwera.
     * @param socket          Gniazdo Socket do nawiązania połączenia z serwerem.
     * @param gameController  Kontroler gry, odpowiedzialny za aktualizację interfejsu graficznego gry.
     * @param roomsController Kontroler pokojów, odpowiedzialny za aktualizację interfejsu graficznego pokojów.
     */
    public ReceiverClient(Client client, ScheduledExecutorService exec, ObjectInputStream in, Socket socket, GameController gameController, RoomsController roomsController) {
        this.client = client;
        this.executor = exec;
        this.in = in;
        this.socket = socket;
        this.gameController = gameController;
        this.roomsController = roomsController;
    }


    /**
     * Metoda odczytuje identyfikator klienta z InputStream i ustawia go w kliencie.
     *
     * @throws ClassNotFoundException Jeżeli nie uda się zidentyfikować klasy podczas deserializacji.
     * @throws IOException            Jeżeli wystąpi błąd wejścia-wyjścia podczas odczytu danych z InputStream.
     */
    private void setID() throws ClassNotFoundException, IOException {
        clientId = in.readInt();
        client.setClientID(clientId);
    }

    /**
     * Metoda  odbiera informacje od serwera dotyczące dostępnych pokoi,
     * aktualizuje interfejs graficzny pokojów oraz dodaje nowe pokoje do listy.
     *
     * @throws ClassNotFoundException Jeżeli nie uda się zidentyfikować klasy podczas deserializacji.
     * @throws IOException            Jeżeli wystąpi błąd wejścia-wyjścia podczas odczytu danych z InputStream.
     */
    private void takeRooms() throws ClassNotFoundException, IOException {
        while (true) {
            int amountRooms = in.readInt();
            if (amountRooms == -1) break;

            roomsController.clearRooms();

            for (int i = 0; i < amountRooms; i++) {
                Integer idRoom = in.readInt();
                Room room = (Room) in.readObject();
                roomsController.addRoom(room);
            }

        }
    }
    /**
     * Metoda odbiera od serwera informacje o liczbie graczy w pokoju,
     * aktualizuje interfejs graficzny gry i rozpoczyna grę, gdy liczba graczy osiągnie 4.
     *
     * @throws ClassNotFoundException Jeżeli nie uda się zidentyfikować klasy podczas deserializacji.
     * @throws IOException            Jeżeli wystąpi błąd wejścia-wyjścia podczas odczytu danych z InputStream.
     */
    private void checkOnOther() throws ClassNotFoundException, IOException {
        int numberOfPlayers = 0;
        while (numberOfPlayers != 4) {
            numberOfPlayers = in.readInt();
            System.out.println(numberOfPlayers);
            gameController.updateAmountPlayers(numberOfPlayers);
        }
        ;
        gameController.startGame();
    }


    /**
     * Metoda odbiera od serwera informacje o starcie gry
     * oraz inicjalizuje interfejs graficzny gry.
     *
     * @throws ClassNotFoundException Jeżeli nie uda się zidentyfikować klasy podczas deserializacji.
     * @throws IOException            Jeżeli wystąpi błąd wejścia-wyjścia podczas odczytu danych z InputStream.
     */
    private void startOfGame() throws IOException, ClassNotFoundException {
        Room room = (Room) in.readObject();
        System.out.println(room);
        gameController.drawGame(room, clientId);
    }


    /**
     * Metoda odbiera od serwera informacje dotyczące aktualnego stanu gry,
     * a następnie aktualizuje interfejs graficzny gry.
     *
     * @throws IOException            Jeżeli wystąpi błąd wejścia-wyjścia podczas odczytu danych z InputStream.
     * @throws ClassNotFoundException Jeżeli nie uda się zidentyfikować klasy podczas deserializacji.
     */
    private void game() throws IOException, ClassNotFoundException {
        Room room = (Room) in.readObject();
        gameController.game(room, clientId);
    }


    /**
     * Metoda implementuje logikę odbierania danych od serwera w osobnym wątku.
     * Rozpoczyna się od ustawienia identyfikatora klienta, a następnie przechodzi do
     * odbierania informacji o dostępnych pokojach, ilości graczy w grze oraz rozpoczęciu gry.
     * W pętli obsługiwane są kolejne etapy gry.
     */
    @Override
    public void run() {
        try {
            setID();
            takeRooms();
            checkOnOther();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            startOfGame();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                game();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
