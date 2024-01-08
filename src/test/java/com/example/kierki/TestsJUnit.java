package com.example.kierki;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestsJUnit {

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
    void validateCardMove_WhenFirstCardInSecondRoundIsHeart_ShouldReturnFalse() {
        Server.initObjects();

        Server.ClientHandler clientHandler = new Server.ClientHandler(null, 1);
        clientHandler.putClientInRoom(1);
        clientHandler.takeCurrentRoom().nextRound();
        Card firstCard = new Card("Diamonds", 7);
        firstCard.setClientID(1);
        Card secondCard = new Card("Hearts", 10);
        secondCard.setClientID(1);
        clientHandler.takeCurrentRoom().setDeck(new ArrayList<>(List.of(firstCard, secondCard)));
        boolean result = clientHandler.validateCardMove("Hearts");
        assertFalse(result);
    }

    @Test
    void validateCardMove_WhenFirstCardInSecondRoundIsNotHeart_ShouldReturnTrue() {
        Server.initObjects();
        Server.ClientHandler clientHandler = new Server.ClientHandler(null, 1);
        clientHandler.putClientInRoom(1);
        clientHandler.takeCurrentRoom().nextRound();
        Card firstCard = new Card("Diamonds", 7);
        firstCard.setClientID(1);
        Card secondCard = new Card("Hearts", 10);
        secondCard.setClientID(1);
        clientHandler.takeCurrentRoom().setDeck(new ArrayList<>(List.of(firstCard, secondCard)));
        boolean result = clientHandler.validateCardMove("Diamonds");
        assertTrue(result);
    }

    @Test
    void validateCardMove_WhenFirstCardInSecondRoundIsHeartButThisIsTheLastSymbolInDeck_ShouldReturnTrue() {
        Server.initObjects();
        Server.ClientHandler clientHandler = new Server.ClientHandler(null, 1);
        clientHandler.putClientInRoom(1);
        clientHandler.takeCurrentRoom().nextRound();
        Card firstCard = new Card("Hearts", 7);
        firstCard.setClientID(1);
        Card secondCard = new Card("Hearts", 10);
        secondCard.setClientID(1);
        clientHandler.takeCurrentRoom().setDeck(new ArrayList<>(List.of(firstCard, secondCard)));
        boolean result = clientHandler.validateCardMove("Hearts");
        assertTrue(result);
    }
    @Test
    void validateCardMove_WhenSymbolsAreEqual_ShouldReturnTrue() {
        Server.initObjects();

        Server.ClientHandler clientHandler = new Server.ClientHandler(null, 1);
        clientHandler.putClientInRoom(1);
        clientHandler.takeCurrentRoom().setFirstCardOnTable(new Card("Hearts", 5));

        boolean result = clientHandler.validateCardMove("Hearts");

        assertTrue(result);
    }

    @Test
    void validateCardMove_WhenSymbolsAreNotEqualAndPlayerDoesNotHaveMatchingColor_ShouldReturnTrue() {

        Server.initObjects();

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

        boolean result = clientHandler.validateCardMove("Hearts");

        assertTrue(result);
    }


    @Test
    void validateCardMove_WhenSymbolsAreNotEqualAndPlayerHaveMatchingColor_ShouldReturnFalse() {

        Server.initObjects();

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

        boolean result = clientHandler.validateCardMove("Diamonds");

        assertFalse(result);
    }

    @Test
    void takeWinnerID_WhenWinningCardIsPresentInActualPlay_ShouldReturnCorrectPlayerID() {

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

        int winnerID = clientHandler.takeWinnerID(winningCard);

        assertEquals(1, winnerID);
    }


    @Test
    void sendMove_ShouldWriteCorrectDataToOutputStream() throws IOException {

        ObjectOutputStream mockDataOutputStream = mock(ObjectOutputStream.class);
        Client client = new Client( null, null, null, null, null,null);
        client.setStreams(mockDataOutputStream);

        int expectedValue = 42;
        String expectedSymbol = "Hearts";


        client.sendMove(expectedValue, expectedSymbol);

        InOrder inOrder = inOrder(mockDataOutputStream);
        inOrder.verify(mockDataOutputStream).writeInt(expectedValue);
        inOrder.verify(mockDataOutputStream).flush();
        inOrder.verify(mockDataOutputStream).writeUTF(expectedSymbol);
        inOrder.verify(mockDataOutputStream).flush();
    }

    @Test
    void sendChosenRoom_ShouldWriteCorrectDataToOutputStream() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream mockObjectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

        Client client = new Client(null, null, null, null, null, null);
        client.setStreams(mockObjectOutputStream);

        Integer expectedRoomId = 2;

        client.sendChosenRoom(expectedRoomId);

        mockObjectOutputStream.flush();

        byte[] writtenBytes = byteArrayOutputStream.toByteArray();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(writtenBytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);

        assertEquals(expectedRoomId, objectInputStream.readObject());
    }

    @Test
    void dealCards_ShouldAssignCardsToClientsInCyclicOrder() {
        Room room = new Room("TestRoom", 1);
        room.setGameInProgress(true);


        room.initializeDeck();

        ArrayList<Integer> mockClientsID = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            mockClientsID.add(i);
            room.addPlayer(null,i);
        }

        room.shuffleDeck();
        room.dealCards();

        int currentClientIndex = 0;
        for (Card card : room.getDeck()) {
            int expectedClientID = mockClientsID.get(currentClientIndex);
            assertEquals(expectedClientID, card.getClientID());
            currentClientIndex = (currentClientIndex + 1) % 4;
        }
    }

    @Test
    void getCardsFromClientID_ShouldReturnCardsForGivenClientID() {
        Room room = new Room("TestRoom", 1);

        Card card1 = mock(Card.class);
        Card card2 = mock(Card.class);
        Card card3 = mock(Card.class);
        Card card4 = mock(Card.class);

        when(card1.getClientID()).thenReturn(1);
        when(card2.getClientID()).thenReturn(2);
        when(card3.getClientID()).thenReturn(1);
        when(card4.getClientID()).thenReturn(3);

        room.setDeck(new ArrayList<>(List.of(card1, card2, card3, card4)));

        ArrayList<Card> result = room.getCardsFromClientID(1);

        assertEquals(2, result.size());
        assertTrue(result.contains(card1));
        assertTrue(result.contains(card3));
    }

    @Test
    void getCardsFromClientID_ShouldReturnEmptyListForNonExistentClientID() {
        Room room = new Room("TestRoom", 1);

        Card card1 = mock(Card.class);
        Card card2 = mock(Card.class);
        Card card3 = mock(Card.class);

        when(card1.getClientID()).thenReturn(1);
        when(card2.getClientID()).thenReturn(2);
        when(card3.getClientID()).thenReturn(1);

        room.setDeck(new ArrayList<>(List.of(card1, card2, card3)));

        ArrayList<Card> result = room.getCardsFromClientID(3);

        assertTrue(result.isEmpty());
    }

    @Test
    public void testEquals_ShouldReturnTrue() {
        Card card1 = new Card( "Hearts", 10);
        Card card2 = new Card( "Hearts", 10);

        assertEquals(card1, card2);

    }

    @Test
    public void testEquals_ShouldReturnFalse() {
        Card card1 = new Card( "Hearts", 10);
        Card card3 = new Card( "Diamonds", 10);
        assertNotEquals(card1, card3);
    }

}