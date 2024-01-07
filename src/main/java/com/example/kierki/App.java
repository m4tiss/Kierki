package com.example.kierki;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Główna klasa aplikacji Kierki, dziedzicząca po klasie Application.
 * Odpowiada za inicjalizację i uruchomienie interfejsu użytkownika oraz klienta.
 */
public class App extends Application {

    /**
     * Konstruktor domyślny klasy App.
     */
    public App() {

    }

    /**
     * Metoda start inicjalizuje interfejs użytkownika oraz klienta i uruchamia aplikację.
     *
     * @param stage Główny etap aplikacji, na którym będzie wyświetlany interfejs użytkownika.
     * @throws IOException Jeśli wystąpi błąd wejścia-wyjścia podczas inicjalizacji interfejsu.
     */
    @Override
    public void start(Stage stage) throws IOException {
        // Inicjalizacja interfejsu logowania
        FXMLLoader loginLoader = new FXMLLoader(App.class.getResource("nicknamePanel.fxml"));
        Scene login = new Scene(loginLoader.load(), 1280, 720);
        LoginController loginController = loginLoader.getController();

        // Inicjalizacja interfejsu pokojów
        FXMLLoader roomsLoader = new FXMLLoader(App.class.getResource("rooms.fxml"));
        roomsLoader.load();
        RoomsController roomsController = roomsLoader.getController();

        // Inicjalizacja interfejsu gry
        FXMLLoader gameLoader = new FXMLLoader(App.class.getResource("game.fxml"));
        gameLoader.load();
        GameController gameController = gameLoader.getController();

        // Inicjalizacja klienta
        Client client = new Client(stage, loginController, roomsController, roomsLoader, gameController, gameLoader);
        client.startReceiver();

        // Konfiguracja etapu aplikacji
        stage.setTitle("Kierki");
        loginController.setClient(client);
        roomsController.setClient(client);
        gameController.setClient(client);

        // Ustawienie etapu na interfejs logowania
        stage.setScene(login);
        stage.show();
    }

    /**
     * Metoda main rozpoczyna działanie aplikacji.
     *
     * @param args Argumenty wiersza poleceń.
     */
    public static void main(String[] args) {
        launch();
    }
}
