package com.example.kierki;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServerTest {

    @Test
    void testSortFourCards() {
        Server.ClientHandler clientHandler = new Server.ClientHandler(null, 1);
        List<Card> cards = new ArrayList<>();
        cards.add(new Card("Spades", 5));
        cards.add(new Card("Hearts", 10));
        cards.add(new Card("Diamonds", 8));
        cards.add(new Card("Clubs", 2));

        clientHandler.sortCards(cards);

        assertEquals(10, cards.get(0).getValue());
        assertEquals(8, cards.get(1).getValue());
        assertEquals(5, cards.get(2).getValue());
        assertEquals(2, cards.get(3).getValue());
    }

    @Test
    void validateCardMove_WhenSymbolsAreEqual_ShouldReturnTrue() {
        // Arrange
        Server.initObjects();

        Server.ClientHandler clientHandler = new Server.ClientHandler(null, 1);
        clientHandler.putClientInRoom(1);
        clientHandler.takeCurrentRoom().setFirstCardOnTable(new Card("Hearts", 5));

        // Act
        boolean result = clientHandler.validateCardMove("Hearts");

        // Assert
        assertTrue(result);
    }

    @Test
    void validateCardMove_WhenSymbolsAreNotEqualAndPlayerDoesNotHaveMatchingColor_ShouldReturnTrue() {

        Server.initObjects();
        // Arrange
        Server.ClientHandler clientHandler = new Server.ClientHandler(null, 1);
        clientHandler.putClientInRoom(1);
        Card firstCard = new Card("Diamonds", 7);
        firstCard.setClientID(1);
        Card secondCard = new Card("Hearts", 10);
        secondCard.setClientID(1);
        ArrayList<Card> cards = new ArrayList<>();
        cards.add(firstCard);
        cards.add(secondCard);
        clientHandler.takeCurrentRoom().setDeck(cards);
        clientHandler.takeCurrentRoom().setFirstCardOnTable(new Card("Clubs",12));

        // Act
        boolean result = clientHandler.validateCardMove("Hearts");

        // Assert
        assertTrue(result);
    }


    @Test
    void validateCardMove_WhenSymbolsAreNotEqualAndPlayerHaveMatchingColor_ShouldReturnFalse() {

        Server.initObjects();
        // Arrange
        Server.ClientHandler clientHandler = new Server.ClientHandler(null, 1);
        clientHandler.putClientInRoom(1);
        Card firstCard = new Card("Diamonds", 7);
        firstCard.setClientID(1);
        Card secondCard = new Card("Hearts", 10);
        secondCard.setClientID(1);
        ArrayList<Card> cards = new ArrayList<>();
        cards.add(firstCard);
        cards.add(secondCard);
        clientHandler.takeCurrentRoom().setDeck(cards);
        clientHandler.takeCurrentRoom().setFirstCardOnTable(new Card("Hearts",12));

        // Act
        boolean result = clientHandler.validateCardMove("Diamonds");

        // Assert
        assertFalse(result);
    }

    @Test
    void takeWinnerID_WhenWinningCardIsPresentInActualPlay_ShouldReturnCorrectPlayerID() {
        // Arrange
        Server.initObjects();
        Server.ClientHandler clientHandler = new Server.ClientHandler(null, 1);
        clientHandler.putClientInRoom(1);

        Card winningCard = new Card("Hearts", 10);
        winningCard.setClientID(1);

        Card firstCard = new Card("Diamonds", 7);
        firstCard.setClientID(2);
        Card secondCard = new Card("Hearts", 10);
        secondCard.setClientID(1);


        clientHandler.takeCurrentRoom().setActualCard(2, firstCard);
        clientHandler.takeCurrentRoom().setActualCard(1,secondCard);

        // Act
        int winnerID = clientHandler.takeWinnerID(winningCard);

        // Assert
        assertEquals(1, winnerID);
    }





    //"Hearts", "Diamonds", "Clubs", "Spades"
}