package com.example.kierki;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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
}