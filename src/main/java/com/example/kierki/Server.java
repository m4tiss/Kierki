package com.example.kierki;


import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import static java.lang.Thread.sleep;

/**
 * Klasa implementująca serwer gry kierki.
 * Serwer obsługuje komunikację między klientami a serwerem, zarządza pokojami gry oraz przebiegiem samej gry.
 *
 * @author Mateusz Gwóźdź
 * @version 1.0
 */
public class Server {
    private static final int PORT = 8888;

    //mapa do trzyamania strumień klientów idKlienta, stream
    private static HashMap<Integer, ObjectOutputStream> outputStreams;

    //mapa do trzymania pokoi idRoom , Room
    private static HashMap<Integer, Room> rooms;

    //mapa do trzymania klient pokój idClient,idRoom,
    private static HashMap<Integer, Integer> clientRooms;

    private static ArrayList<ObjectOutputStream> clientsInLobby;

    private static int idRoom;
    private static int clientsId;
    private static HashMap<Integer, Semaphore> roomSemaphores;



    /**
     * Inicjalizuje wszystkie niezbędne obiekty, takie jak mapy, listy, semafory, oraz ustawia początkowe wartości.
     */
    public static void initObjects() {
        outputStreams = new HashMap<>();
        rooms = new HashMap<>();
        clientRooms = new HashMap<>();
        clientsInLobby = new ArrayList<>();
        idRoom = 1;
        clientsId = 1;
        rooms.put(idRoom, new Room("Pokój_1",idRoom));
        idRoom++;
        roomSemaphores =new HashMap<>();
    }

    /**
     * Metoda główna programu. Tworzy serwer, nasłuchuje na określonym porcie, akceptuje połączenia i uruchamia wątki obsługujące klientów.
     *
     * @param args Argumenty przekazywane przy uruchamianiu programu.
     */
    public static void main(String[] args) {

        initObjects();

        ExecutorService executorService = Executors.newCachedThreadPool();

        ServerInfo serverInfo = new ServerInfo();
        executorService.execute(serverInfo);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, clientsId);
                executorService.execute(clientHandler);
                clientsId++;
            }
        } catch (IOException e) {
            System.out.println("Error with serverSocket");
        } finally {
            executorService.shutdown();
        }
    }

    /**
     * Klasa wewnętrzna reprezentująca obsługę klienta przez serwer.
     */
    static class ClientHandler implements Runnable {
        private final Socket socket;
        private final int clientId;
        private String nickname;
        ObjectInputStream in;
        ObjectOutputStream out;

        /**
         * Konstruktor klasy wewnętrznej ClientHandler.
         *
         * @param socket   Gniazdo Socket, przez które odbywa się komunikacja z klientem.
         * @param clientId Unikalny identyfikator klienta przypisany podczas jego dołączania do serwera.
         */
        public ClientHandler(Socket socket, int clientId) {
            this.socket = socket;
            this.clientId = clientId;
        }


        /**
         * Metoda wysyłająca informacje o dostępnych pokojach do klienta.
         * Wysyła liczbę dostępnych pokoi, a następnie dla każdego pokoju wysyła jego identyfikator oraz obiekt klasy Room.
         *
         * @throws IOException Występuje w przypadku problemów z operacjami wejścia/wyjścia.
         */
        private void sendRooms() throws IOException {
            Set<Map.Entry<Integer, Room>> entrySet = rooms.entrySet();
            out.writeInt(entrySet.size());
            out.flush();

            for (Map.Entry<Integer, Room> entry : entrySet) {
                out.writeInt(entry.getKey());
                out.writeObject(entry.getValue());
                out.flush();
            }
        }



        /**
         * Metoda oczekująca na wybór pokoju przez klienta i rozgłaszająca informacje o dostępności pokoi.
         * Po otrzymaniu wybranego pokoju, dodaje klienta do tego pokoju, inicjalizuje grę w przypadku pełnej liczby graczy,
         * i informuje innych klientów o liczbie graczy w danym pokoju.
         *
         * @throws IOException            Występuje w przypadku problemów z operacjami wejścia/wyjścia.
         * @throws ClassNotFoundException Występuje, gdy nie można zidentyfikować przesyłanego obiektu.
         */
        private void waitOnRoomAndBroadcast() throws IOException, ClassNotFoundException {
            Integer chosenRoom = (Integer) in.readObject();

            while (true) {
                if (chosenRoom != -1) break;

                rooms.put(idRoom, new Room("Pokój_" + idRoom, idRoom));
                idRoom++;

                broadcastToPlayersInLobby();

                chosenRoom = (Integer) in.readObject();
            }

            clientsInLobby.remove(out);
            out.flush();

            clientRooms.put(clientId, chosenRoom);
            Room currentRoom = rooms.get(chosenRoom);

            out.writeInt(-1);
            currentRoom.addPlayer(nickname, clientId);
            broadcastToPlayersInLobby();

            roomSemaphores.putIfAbsent(chosenRoom, new Semaphore(0));
            if (currentRoom.getAmountOfPlayers() == 4) {
                currentRoom.setGameInProgress(Boolean.TRUE);
                currentRoom.shuffleDeck();
                currentRoom.dealCards();
                currentRoom.randomTurn();
            }

            int sendingClientRoom = clientRooms.get(clientId);

            for (Map.Entry<Integer, ObjectOutputStream> entry : outputStreams.entrySet()) {
                int targetClientId = entry.getKey();
                ObjectOutputStream targetOutputStream = entry.getValue();

                if (clientRooms.containsKey(targetClientId) && clientRooms.get(targetClientId) == sendingClientRoom) {
                    targetOutputStream.writeInt(rooms.get(chosenRoom).getAmountOfPlayers());
                    targetOutputStream.flush();
                }
            }

            if (currentRoom.getAmountOfPlayers() == 4) {
                for (int i = 0; i < 4; i++) {
                    roomSemaphores.get(chosenRoom).release();
                }
            }
        }


        /**
         * Metoda pomocnicza zwracająca aktualny pokój, do którego przypisany jest klient.
         *
         * @return Obiekt klasy Room reprezentujący aktualny pokój klienta.
         */
        private Room takeCurrentRoom(){
            int idCurrentRoom = clientRooms.get(clientId);
            return rooms.get(idCurrentRoom);
        }


        /**
         * Metoda inicjująca początek gry dla klienta.
         * Oczekuje na akwizycję semafora dla aktualnego pokoju, inicjalizuje punkty graczy oraz wysyła informacje o stanie gry do klienta.
         *
         * @throws IOException Występuje w przypadku problemów z operacjami wejścia/wyjścia.
         */
        private void startOfGame() throws IOException {
            int idCurrentRoom = clientRooms.get(clientId);

            try {
                roomSemaphores.get(idCurrentRoom).acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            takeCurrentRoom().initializePoints();

            out.reset();
            out.writeObject(rooms.get(idCurrentRoom));
            out.flush();
        }

        /**
         * Metoda rozgłaszająca informacje o stanie gry w tym samym pokoju do wszystkich klientów znajdujących się w tym pokoju.
         *
         * @throws IOException Występuje w przypadku problemów z operacjami wejścia/wyjścia.
         */
        private void broadcastToSameRoomPlayers() throws IOException {
            int sendingClientRoom = clientRooms.get(clientId);

            for (Map.Entry<Integer, ObjectOutputStream> entry : outputStreams.entrySet()) {
                int targetClientId = entry.getKey();
                ObjectOutputStream targetOutputStream = entry.getValue();

                if (clientRooms.containsKey(targetClientId) && clientRooms.get(targetClientId) == sendingClientRoom) {
                    targetOutputStream.reset();
                    targetOutputStream.writeObject(takeCurrentRoom());
                    targetOutputStream.flush();
                }
            }
        }

        /**
         * Metoda rozgłaszająca informacje o dostępnych pokojach do wszystkich klientów oczekujących w lobby.
         *
         * @throws IOException Występuje w przypadku problemów z operacjami wejścia/wyjścia.
         */
        private void broadcastToPlayersInLobby() throws IOException {
            for (ObjectOutputStream targetOutputStream : clientsInLobby) {
                Set<Map.Entry<Integer, Room>> entrySet = rooms.entrySet();
                targetOutputStream.writeInt(entrySet.size());
                targetOutputStream.flush();

                for (Map.Entry<Integer, Room> entry : entrySet) {
                    targetOutputStream.reset();
                    targetOutputStream.writeInt(entry.getKey());
                    targetOutputStream.writeObject(entry.getValue());
                    targetOutputStream.flush();
                }
            }
        }



        /**
         * Metoda sprawdzająca poprawność ruchu karty przez klienta.
         *
         * @param chosenSymbol Symbol wybranej karty przez klienta.
         * @return true, jeśli ruch jest poprawny, false w przeciwnym razie.
         */
        private boolean validateCardMove(String chosenSymbol){
            ArrayList<Card> clientDeck = takeCurrentRoom().getCardsFromClientID(clientId);

            String currentSymbol = takeCurrentRoom().getFirstCardOnTable().getSymbol();

            if(!Objects.equals(chosenSymbol, currentSymbol)){
                //Sprawdzenie czy user ma w talii taki kolor
                List<String> availableColors = new ArrayList<>();
                for (Card card : clientDeck) {
                    String symbol = card.getSymbol();
                    if (!availableColors.contains(symbol)) {
                        availableColors.add(symbol);
                    }
                }
                if(availableColors.contains(currentSymbol)){
                    return false;
                }
                return true;
            }
            return true;
        }

        /**
         * Metoda pobierająca karty aktualnie rozgrywanej tury dla wszystkich klientów w pokoju.
         *
         * @return Lista obiektów klasy Card reprezentujących karty aktualnie rozgrywanej tury.
         */
        private ArrayList<Card> getCards() {
            ArrayList<Card> cards = new ArrayList<>();
            List<Integer> clientsID = takeCurrentRoom().getClientsID();
            for (Integer clientID : clientsID) {
                Card card = takeCurrentRoom().getActualCard(clientID);
                cards.add(card);
            }
            return cards;
        }

        /**
         * Metoda sortująca karty zgodnie z wartością, malejąco.
         *
         * @param cards Lista obiektów klasy Card do posortowania.
         */
        protected void sortCards(List<Card> cards) {
            Comparator<Card> valueComparator = Comparator.comparing(Card::getValue).reversed();
            cards.sort(valueComparator);
        }

        /**
         * Metoda pobierająca identyfikator klienta, który wygrał aktualną rundę na podstawie karty zwycięskiej.
         *
         * @param winningCard Karta zwycięska, na podstawie której wybierany jest zwycięzca.
         * @return Identyfikator klienta, który wygrał rundę.
         */
        private int takeWinnerID(Card winningCard) {
            HashMap<Integer, Card> actualPlay = takeCurrentRoom().getActualPlay();
            for (Map.Entry<Integer, Card> entry : actualPlay.entrySet()) {
                if (entry.getValue().equals(winningCard)) {
                    return entry.getKey();
                }
            }
            return 0;
        }
        /**
         * Metoda obsługująca pierwszą rundę gry.
         *
         * @param currentSymbol Aktualny symbol karty, według którego odbywa się rozgrywka.
         */
        private void handleRound1(String currentSymbol) {
            ArrayList<Card> winCard = getCards();
            winCard.removeIf(card -> !Objects.equals(currentSymbol, card.getSymbol()));
            sortCards(winCard);
            Card winningCard = winCard.get(0);
            int winningClientID = takeWinnerID(winningCard);
            takeCurrentRoom().setPoints(winningClientID,-20);
            takeCurrentRoom().nextTurnNumbered(winningClientID);
        }

        /**
         * Metoda obsługująca drugą rundę gry.
         *
         * @param currentSymbol Aktualny symbol karty, według którego odbywa się rozgrywka.
         */
        private void handleRound2(String currentSymbol) {

            ArrayList<Card> cards = getCards();
            sortCards(cards);
            Card winningCard = takeCurrentRoom().getFirstCardOnTable();
            int amountOfHearts = 0;
            for(Card card: cards){
                if(Objects.equals(card.getSymbol(), "Hearts"))amountOfHearts++;
                if(Objects.equals(card.getSymbol(), currentSymbol) && card.getValue()>winningCard.getValue()){
                    winningCard=card;
                }
            }
            int winningClientID = takeWinnerID(winningCard);
            takeCurrentRoom().setPoints(winningClientID, -20*amountOfHearts);
            takeCurrentRoom().nextTurnNumbered(winningClientID);
        }
        /**
         * Metoda obsługująca trzecią rundę gry.
         *
         * @param currentSymbol Aktualny symbol karty, według którego odbywa się rozgrywka.
         */
        private void handleRound3(String currentSymbol) {
            ArrayList<Card> cards  = getCards();
            sortCards(cards);
            Card winningCard = takeCurrentRoom().getFirstCardOnTable();
            int amountOfQueens = 0;
            for(Card card: cards){
                if(Objects.equals(card.getValue(), 12))amountOfQueens++;
                if(Objects.equals(card.getSymbol(), currentSymbol) && card.getValue()>winningCard.getValue()){
                    winningCard=card;
                }
            }
            int winningClientID = takeWinnerID(winningCard);
            takeCurrentRoom().setPoints(winningClientID, -60*amountOfQueens);
            takeCurrentRoom().nextTurnNumbered(winningClientID);
        }

        /**
         * Metoda obsługująca czwartą rundę gry.
         *
         * @param currentSymbol Aktualny symbol karty, według którego odbywa się rozgrywka.
         */
        private void handleRound4(String currentSymbol) {
            ArrayList<Card> cards = getCards();
            sortCards(cards);
            Card winningCard = takeCurrentRoom().getFirstCardOnTable();
            int amountOfJacksAndKings = 0;
            for(Card card: cards){
                if(Objects.equals(card.getValue(), 11)||Objects.equals(card.getValue(), 13))amountOfJacksAndKings++;
                if(Objects.equals(card.getSymbol(), currentSymbol) && card.getValue()>winningCard.getValue()){
                    winningCard=card;
                }
            }
            int winningClientID = takeWinnerID(winningCard);
            takeCurrentRoom().setPoints(winningClientID, -30*amountOfJacksAndKings);
            takeCurrentRoom().nextTurnNumbered(winningClientID);
        }

        /**
         * Metoda obsługująca pierwszą piątą gry.
         *
         * @param currentSymbol Aktualny symbol karty, według którego odbywa się rozgrywka.
         */
        private void handleRound5(String currentSymbol) {
            ArrayList<Card> cards =  getCards();
            sortCards(cards);
            Card winningCard = takeCurrentRoom().getFirstCardOnTable();
            boolean checkHeartsKing = false;
            for(Card card: cards){
                if(Objects.equals(card.getValue(), 13)&&Objects.equals(card.getSymbol(), "Hearts"))checkHeartsKing=true;
                if(Objects.equals(card.getSymbol(), currentSymbol) && card.getValue()>winningCard.getValue()){
                    winningCard=card;
                }
            }
            int winningClientID = takeWinnerID(winningCard);
            if(checkHeartsKing){
                takeCurrentRoom().setPoints(winningClientID, -150);
                takeCurrentRoom().initializeDeck();
                takeCurrentRoom().dealCards();
                takeCurrentRoom().shuffleDeck();
                takeCurrentRoom().nextRound();
                takeCurrentRoom().randomTurn();
                return;
            }
            takeCurrentRoom().nextTurnNumbered(winningClientID);
        }
        /**
         * Metoda obsługująca szóstą rundę gry.
         *
         * @param currentSymbol Aktualny symbol karty, według którego odbywa się rozgrywka.
         */
        private void handleRound6(String currentSymbol) {
            ArrayList<Card> cards = getCards();
            sortCards(cards);
            Card winningCard = takeCurrentRoom().getFirstCardOnTable();
            for(Card card: cards){
                if(Objects.equals(card.getSymbol(), currentSymbol) && card.getValue()>winningCard.getValue()){
                    winningCard=card;
                }
            }
            int winningClientID = takeWinnerID(winningCard);
            if(takeCurrentRoom().getDeck().size()==28||takeCurrentRoom().getDeck().size()==4)takeCurrentRoom().setPoints(winningClientID, -75);
            takeCurrentRoom().nextTurnNumbered(winningClientID);
        }
        /**
         * Metoda obsługująca siódmą rundę gry.
         *
         * @param currentSymbol Aktualny symbol karty, według którego odbywa się rozgrywka.
         */

        private void handleRound7(String currentSymbol){
            ArrayList<Card> cards = getCards();
            sortCards(cards);
            Card winningCard = takeCurrentRoom().getFirstCardOnTable();
            int amountOfHearts = 0;
            int amountOfQueens = 0;
            int amountOfJacksAndKings = 0;
            int checkHeartsKing = 0;
            for(Card card: cards){
                if(Objects.equals(card.getSymbol(), "Hearts"))amountOfHearts++;
                if(Objects.equals(card.getValue(), 12))amountOfQueens++;
                if(Objects.equals(card.getValue(), 11)||Objects.equals(card.getValue(), 13))amountOfJacksAndKings++;
                if(Objects.equals(card.getValue(), 13)&&Objects.equals(card.getSymbol(), "Hearts"))checkHeartsKing=1;
                if(Objects.equals(card.getSymbol(), currentSymbol) && card.getValue()>winningCard.getValue()){
                    winningCard=card;
                }
            }

            int winningClientID = takeWinnerID(winningCard);
            takeCurrentRoom().setPoints(winningClientID,-20);
            takeCurrentRoom().setPoints(winningClientID, -20*amountOfHearts);
            takeCurrentRoom().setPoints(winningClientID, -60*amountOfQueens);
            takeCurrentRoom().setPoints(winningClientID, -30*amountOfJacksAndKings);
            takeCurrentRoom().setPoints(winningClientID, -150*checkHeartsKing);
            if(takeCurrentRoom().getDeck().size()==28||takeCurrentRoom().getDeck().size()==4)takeCurrentRoom().setPoints(winningClientID, -75);
            takeCurrentRoom().nextTurnNumbered(winningClientID);
        }


        /**
         * Metoda sumująca punkty dla danej rundy i obsługująca odpowiednie zasady.
         */
        private void sumPoints(){
            int round = takeCurrentRoom().getRound();
            String currentSymbol = takeCurrentRoom().getFirstCardOnTable().getSymbol();
            switch (round) {
                case 1:
                    handleRound1(currentSymbol);
                    break;
                case 2:
                    handleRound2(currentSymbol);
                    break;
                case 3:
                    handleRound3(currentSymbol);
                    break;
                case 4:
                    handleRound4(currentSymbol);
                    break;
                case 5:
                    handleRound5(currentSymbol);
                    break;
                case 6:
                    handleRound6(currentSymbol);
                    break;
                case 7:
                    handleRound7(currentSymbol);
                    break;
                default:
                    System.out.println("BŁAD SERWERA");
            }
        }


        /**
         * Metoda usuwająca karty z głównej talii gry, które zostały użyte w danej rundzie.
         */
        private void removeMainCards(){
            ArrayList<Card> updatedDeck = takeCurrentRoom().getDeck();

            HashMap<Integer,Card> actualPlay = takeCurrentRoom().getActualPlay();

            for (Card card : actualPlay.values()) {
                updatedDeck.removeIf(deckCard -> deckCard.equals(card));
            }

            takeCurrentRoom().setDeck(updatedDeck);
            takeCurrentRoom().resetActualCards();
        }

        /**
         * Metoda sprawdzająca, czy runda gry dobiegła końca.
         * Jeżeli talia gry jest pusta, inicjalizuje nową talie, rozdaje karty, tasuje talie, przechodzi do kolejnej rundy
         * i ustala losowo, który gracz rozpocznie tę rundę.
         */
        private void checkEndOfRound(){
            if(takeCurrentRoom().getDeck().isEmpty()){
                takeCurrentRoom().initializeDeck();
                takeCurrentRoom().dealCards();
                takeCurrentRoom().shuffleDeck();
                takeCurrentRoom().nextRound();
                takeCurrentRoom().randomTurn();
            }
        }


        /**
         * Metoda sprawdzająca, czy gra dobiegła końca.
         * Sprawdza, czy talia gry jest pusta i czy obecna runda to siódma (ostatnia) runda.
         * Jeżeli warunki są spełnione, oznacza zakończenie gry.
         */
        private void checkEndOfGame(){
            if(takeCurrentRoom().getDeck().isEmpty()&&takeCurrentRoom().getRound()==7){
                //end
            }
        }


        /**
         * Reprezentuje główną logikę gry, w której gracze wykonują swoje tury, rzucają karty,
         * i zarządzają przebiegiem gry. Metoda ta działa w nieskończonej pętli, symulując ciągły charakter gry.
         * Odczytuje wejście od gracza, przetwarza ich akcje i aktualizuje stan gry. Gra obejmuje funkcje takie jak
         * komunikaty czatu, walidacja kart, obliczenia punktów oraz obsługa końca rund i gry.
         * Metoda zapewnia, że stan gry jest utrzymany w synchronizacji z działaniami graczy.
         */
        private void game() throws IOException, InterruptedException {
            while(true){
                int chosenValue = in.readInt();
                String chosenSymbol = in.readUTF();
                if(chosenValue == -1){
                    takeCurrentRoom().addMessageToChat(chosenSymbol);
                    broadcastToSameRoomPlayers();
                    continue;
                }
                if( takeCurrentRoom().getClientsID().get(takeCurrentRoom().getTurn()) == clientId){
//                    System.out.println(takeCurrentRoom().getFirstCardOnTable().getSymbol());
                    if(takeCurrentRoom().checkActualPlay()==0){
                        Card card = new Card(chosenSymbol,chosenValue);
                        card.setClientID(clientId);
                        takeCurrentRoom().setActualCard(clientId,card);
                        takeCurrentRoom().setFirstCardOnTable(card);
                        takeCurrentRoom().nextTurn();
                    }
                    else if(takeCurrentRoom().checkActualPlay()>=1){
                        if(!validateCardMove(chosenSymbol))continue;
                        Card card = new Card(chosenSymbol,chosenValue);
                        card.setClientID(clientId);
                        takeCurrentRoom().setActualCard(clientId,card);
                        takeCurrentRoom().nextTurn();
                    }
                    broadcastToSameRoomPlayers();
                    if(takeCurrentRoom().checkActualPlay()>=4){
                        sleep(1500);
                        sumPoints();
                        removeMainCards();
                        checkEndOfGame();
                        checkEndOfRound();
                        broadcastToSameRoomPlayers();
                    }
                }
            }
        }


        /**
         * Metoda obsługująca połączenie z klientem.
         * Inicjalizuje strumienie wejścia/wyjścia, dodaje klienta do ogólnej mapy strumieni,
         * przeprowadza proces logowania, dodaje klienta do listy oczekujących w lobby,
         * wysyła informacje o dostępnych pokojach, czeka na wybór pokoju, inicjuje start gry
         * i rozpoczyna główną pętlę gry obsługującą tury i interakcje gracza.
         */
        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                outputStreams.put(clientId, out);
                //loginPanel
                out.writeInt(clientId);
                out.flush();
                nickname = in.readUTF();


                clientsInLobby.add(out);
                sendRooms();
                waitOnRoomAndBroadcast();
                startOfGame();
                game();
            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Klasa wewnętrzna reprezentująca wątek informacyjny serwera.
     */
    static class ServerInfo implements Runnable {

        /**
         * Uruchamia wątek informacyjny, który pozwala administratorowi serwera na wyświetlanie statystyk i informacji o pokojach.
         */
        @Override
        public void run() {
            Scanner scanner = new Scanner(System.in);
            int userInput=0;
            while(true){
                System.out.println("1 - Wyswietl statystyki serwera");
                userInput = scanner.nextInt();
                if (userInput == 1) {
                    Set<Map.Entry<Integer, Room>> roomsSet = rooms.entrySet();
                    for (Map.Entry<Integer, Room> room : roomsSet) {
                        System.out.println("Pokój numer: " + room.getKey());
                        ArrayList<Integer> clientsID = room.getValue().getClientsID();
                        System.out.print("ID graczy w pokoju: ");
                        for(Integer clientID : clientsID){
                            System.out.print(clientID+" ");
                        }
                        System.out.println("\nGRA W TOKU: "+ room.getValue().isGameInProgress());
                    }
                }

            }


            //scanner.close();
        }
    }
}
