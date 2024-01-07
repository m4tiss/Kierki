/**
 *
 */
package com.example.kierki;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.io.IOException;

/**
 * Klasa LoginController zarządza logiką funkcjonalności logowania w aplikacji Kierki,
 * odpowiada za obsługę logiki interfejsu użytkownika
 * związanego z funkcjonalnością logowania.
 * Ta klasa wykorzystuje JavaFX do komponentów interfejsu graficznego.
 */
public class LoginController {

    /**
     * Instancja Client odpowiedzialna za obsługę komunikacji z serwerem.
     */
    private Client client;

    /**
     * Pole TextField reprezentujące wprowadzony przez użytkownika pseudonim.
     */
    @FXML
    private TextField nicknameInput;

    /**
     * Ustawia instancję Client dla LoginController.
     *
     * @param client Instancja Client do ustawienia.
     */
    public void setClient(Client client) {
        this.client = client;
    }

    /**
     * Obsługuje zdarzenie kliknięcia przycisku potwierdzenia przez użytkownika.
     * Pobiera pseudonim z pola wprowadzania i ustawia go w skojarzonej instancji Client.
     *
     * @param event Zdarzenie ActionEvent wywołane kliknięciem przycisku potwierdzenia.
     * @throws IOException Jeśli wystąpi błąd wejścia-wyjścia podczas procesu ustawiania pseudonimu.
     */
    @FXML
    protected void onConfirmButtonClick(ActionEvent event) throws IOException {
        client.setNickname(nicknameInput.getText());
    }
}
