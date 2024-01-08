package com.example.kierki;

import java.io.Serializable;
import java.util.*;

/**
 * Klasa reprezentująca pokój w grze kierki.
 * <p>
 * Pokój zawiera informacje o grze, takie jak gracze, talia kart,
 * stan gry, punkty graczy, aktualna rozgrywka, chat i wiele innych.
 * </p>
 *
 * @version 1.0
 */
public class Room implements Serializable {

    private final int idRoom;
    private int amountOfPlayers;
    private final String roomName;
    private ArrayList<String> players;

    private ArrayList<Integer> clientsID;

    private boolean gameInProgress;

    private int round;

    private Card firstCardOnTable;

    private ArrayList<Card> deck;

    private HashMap<Integer, Integer> points;

    private ArrayList<String> chat;

    private int turn;

    private HashMap<Integer, Card> actualPlay;

    /**
     * Konstruktor tworzący nowy pokój gry.
     *
     * @param roomName nazwa pokoju
     * @param idRoom identyfikator pokoju
     */

    public Room(String roomName, int idRoom) {
        this.roomName = roomName;
        this.players = new ArrayList<>();
        this.clientsID = new ArrayList<>();
        this.chat= new ArrayList<>();
        this.chat.add("Admin: Witam was w rozgrywce, bawcie się dobrze");
        this.actualPlay = new HashMap<>();
        this.points = new HashMap<>();
        this.gameInProgress = false;
        this.idRoom = idRoom;
        this.amountOfPlayers = 0;
        this.round = 2;
        this.firstCardOnTable = new Card("XXX", 99);
        initializeDeck();
    }

    /**
     * Metoda pobiera pierwszą kartę, która została wyłożona na stole w danej rundzie.
     *
     * @return pierwsza karta na stole
     */
    public Card getFirstCardOnTable() {
        return firstCardOnTable;
    }

    /**
     * Metoda ustawia pierwszą kartę na stole na podaną kartę.
     *
     * @param firstCardOnTable karta do ustawienia jako pierwsza na stole
     */
    public void setFirstCardOnTable(Card firstCardOnTable) {
        this.firstCardOnTable = firstCardOnTable;
    }

    /**
     * Metoda pobiera listę wiadomości z chatu w pokoju.
     *
     * @return lista wiadomości w chatu
     */
    public ArrayList<String> getChat() {
        return chat;
    }

    /**
     * Metoda pobiera talię kart aktualnie używaną w grze.
     *
     * @return lista kart w talii
     */
    public ArrayList<Card> getDeck() {
        return deck;
    }

    /**
     * Metoda ustawia talię kart w pokoju na podaną listę kart.
     *
     * @param deck lista kart do ustawienia
     */
    public void setDeck(ArrayList<Card> deck) {
        this.deck = deck;
    }

    /**
     * Metoda ustawia kartę, którą dany klient zagrał w bieżącej rundzie.
     *
     * @param clientID identyfikator klienta
     * @param card karta, którą klient zagrał
     */

    public void setActualCard(int clientID, Card card) {
        actualPlay.put(clientID, card);
    }

    /**
     * Metoda sprawdza, czy gra jest obecnie w trakcie.
     *
     * @return true, jeśli gra jest w trakcie; false, jeśli gra jest zakończona lub nie rozpoczęta
     */

    public boolean isGameInProgress() {
        return gameInProgress;
    }


    /**
     * Metoda dodająca wiadomość do chatu pokoju.
     *
     * @param message treść wiadomości
     */
    public void addMessageToChat(String message){
        chat.add(message);
    }

    /**
     * Metoda czyszcząca cały chat pokoju.
     */
    public void clearChat(){
        chat.clear();
    }

    /**
     * metoda pobiera aktualny stan punktów w pokoju.
     *
     * @return mapa punktów, gdzie kluczem jest identyfikator klienta, a wartością liczba punktów
     */
    public HashMap<Integer, Integer> getPoints() {
        return points;
    }

    /**
     * Metoda ustawiająca punkty dla danego klienta.
     *
     * @param clientID identyfikator klienta
     * @param clientPoints liczba punktów do dodania
     */
    public void setPoints(int clientID, int clientPoints) {
        int currentPoints = points.get(clientID);
        currentPoints += clientPoints;
        points.put(clientID, currentPoints);
    }


    /**
     * Metoda przechodząca do kolejnej rundy gry.
     */
    public void nextRound() {
        round++;
    }

    /**
     * Metoda pobiera numer bieżącej rundy gry.
     *
     * @return numer rundy
     */
    public int getRound() {
        return round;
    }


    /**
     * Metoda inicjalizująca punkty dla wszystkich klientów.
     */
    public void initializePoints() {
        for (int i = 0; i < 4; i++) {
            points.put(clientsID.get(i), 0);
        }
    }

    /**
     * Metoda pobiera aktualną lewe, gdzie kluczem jest identyfikator klienta,
     * a wartością jest karta, którą dany klient zagrał w danej rundzie.
     *
     * @return mapa aktualnej rozgrywki
     */
    public HashMap<Integer, Card> getActualPlay() {
        return actualPlay;
    }


    /**
     * Metoda pobiera aktualną kartę zagrana przez konkretnego klienta w bieżącej rundzie.
     *
     * @param clientID identyfikator klienta
     * @return obiekt karty zagranej przez klienta
     */
    public Card getActualCard(int clientID) {
        return actualPlay.get(clientID);
    }



    /**
     * Metoda resetuje aktualne karty rozgrywki, czyli usuwa informacje o kartach zagranych przez graczy.
     */
    public void resetActualCards() {
        actualPlay.clear();
    }

    /**
     * Metoda sprawdza, ile kart jest aktualnie zagranych w danej rundzie.
     *
     * @return liczba kart zagranych w bieżącej rundzie
     */
    public int checkActualPlay() {
        return actualPlay.size();
    }

    /**
     * Metoda pobiera listę identyfikatorów klientów uczestniczących w pokoju.
     *
     * @return lista identyfikatorów klientów
     */
    public ArrayList<Integer> getClientsID() {
        return clientsID;
    }

    /**
     * Metoda pobiera nazwę pokoju.
     *
     * @return nazwa pokoju
     */
    public String getRoomName() {
        return roomName;
    }

    /**
     * Metoda pobiera liczbę graczy aktualnie uczestniczących w pokoju.
     *
     * @return liczba graczy
     */

    public int getAmountOfPlayers() {
        return amountOfPlayers;
    }

    /**
     * Metoda pobiera identyfikator pokoju.
     *
     * @return identyfikator pokoju
     */
    public int getIdRoom() {
        return idRoom;
    }


    /**
     * Metoda inicjalizuje talię kart w pokoju, tworząc nowe karty zgodnie z regułami gry.
     */
    public void initializeDeck() {
        deck = new ArrayList<>();

        String[] symbols = {"Hearts", "Diamonds", "Clubs", "Spades"};
        int[] values = {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14};

        for (String symbol : symbols) {
            for (int value : values) {
                deck.add(new Card(symbol, value));
            }
        }
    }

    /**
     * Metoda tasująca talie kart.
     */
    public void shuffleDeck() {
        Collections.shuffle(deck);
    }

    /**
     * Metoda rozdaje karty graczom zgodnie z zasadami gry kierki.
     * Karty są przypisywane do klientów w sposób cykliczny, zapewniając
     * równomierne rozdanie kart między wszystkich graczy.
     */
    public void dealCards() {
        int currentClientIndex = 0;

        for (Card card : deck) {
            int currentClientID = clientsID.get(currentClientIndex);
            card.setClientID(currentClientID);
            currentClientIndex = (currentClientIndex + 1) % amountOfPlayers;
        }
    }

    /**
     * Metoda ustala losowy numer gracza rozpoczynającego turę.
     * Generuje losową liczbę od 0 do 3, reprezentującą numer gracza,
     * który rozpocznie następną turę.
     */
    public void randomTurn() {
        Random random = new Random();
        turn = random.nextInt(4);
        System.out.println("Randomly generated turn: " + turn);
    }

    /**
     * Metoda przechodzi do następnego gracza w kolejności.
     * Aktualizuje numer gracza, który ma wykonać następny ruch.
     */
    public void nextTurn() {
        turn = (turn + 1) % 4;
        System.out.println("Next turn: " + turn);
    }

    /**
     * Metoda przechodzi do wyznaczonego gracza.
     * @param clientID identyfikator klienta
     */
    public void nextTurnNumbered(int clientID){
        turn = clientsID.indexOf(clientID);
    }


    /**
     * Metoda dodająca nowego gracza do pokoju.
     *
     * @param nickname nick nowego gracza
     * @param clientID identyfikator klienta
     */
    public void addPlayer(String nickname, int clientID) {
        players.add(nickname);
        amountOfPlayers++;
        clientsID.add(clientID);
    }

    /**
     * Metoda ustawia stan gry, wskazując, czy gra jest aktualnie w trakcie.
     *
     * @param bool true, jeśli gra jest w trakcie; false, jeśli gra jest zakończona lub nie rozpoczęta
     */
    public void setGameInProgress(Boolean bool) {
        gameInProgress = bool;
    }


    /**
     * Wyświetla informacje o kartach w talii na konsoli.
     * Metoda wypisuje wartość, symbol i identyfikator klienta dla każdej karty w talii.
     */
    public void displayDeck() {
        for (Card card : deck) {
            card.displayCard();
        }
    }

    /**
     * Metoda pobiera numer aktualnego gracza, który ma wykonać ruch.
     *
     * @return numer gracza (0-3) w kolejności ruchów
     */
    public int getTurn() {
        return turn;
    }


    /**
     * Metoda pobiera listę kart przypisanych do określonego klienta.
     *
     * @param clientID identyfikator klienta
     * @return lista kart przypisanych do danego klienta
     */
    public ArrayList<Card> getCardsFromClientID(int clientID) {
        ArrayList<Card> clientsCards = new ArrayList<>();
        for (Card card : deck) {
            if (card.getClientID() == clientID) {
                clientsCards.add(card);
            }
        }
        return clientsCards;
    }

    /**
     * Metoda pobiera listę nazw graczy aktualnie uczestniczących w grze.
     *
     * @return lista nazw graczy
     */
    public ArrayList<String> getPlayers() {
        return players;
    }

}