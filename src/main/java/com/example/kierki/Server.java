package com.example.kierki;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Server {
    private static final int PORT = 8888;

    //mapa do trzyamania strumień klientów idKlienta, stream
    private static HashMap<Integer, ObjectOutputStream> outputStreams;

    //mapa do trzymania pokoi idRoom , Room
    private static HashMap<Integer, Room> rooms;

    //mapa do trzymania klient pokój idClient,idRoom,
    private static HashMap<Integer, Integer> clientRooms;

    private static int idRoom;
    private static int clientsId;
    private static HashMap<Integer, Semaphore> roomSemaphores;



    public static void initObjects() {
        outputStreams = new HashMap<>();
        rooms = new HashMap<>();
        clientRooms = new HashMap<>();
        idRoom = 1;
        clientsId = 1;
        rooms.put(idRoom, new Room("Zajaweczka",idRoom));
        idRoom++;
        roomSemaphores =new HashMap<>();
    }

    public static void main(String[] args) {

        initObjects();

        ExecutorService executorService = Executors.newCachedThreadPool();
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

    static class ClientHandler implements Runnable {
        private final Socket socket;
        private final int clientId;
        private String nickname;
        ObjectInputStream in;
        ObjectOutputStream out;

        public ClientHandler(Socket socket, int clientId) {
            this.socket = socket;
            this.clientId = clientId;
        }


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



        private void waitOnRoomAndBroadcast() throws IOException, ClassNotFoundException {
            Integer chosenRoom = (Integer) in.readObject();
            clientRooms.put(clientId,chosenRoom);
            Room currentRoom = rooms.get(chosenRoom);
            currentRoom.addPlayer(nickname,clientId);

            roomSemaphores.putIfAbsent(chosenRoom, new Semaphore(0));

            if(currentRoom.getAmountOfPlayers()==4){
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
            if(currentRoom.getAmountOfPlayers()==4){
                for (int i = 0; i < 4; i++) {
                    roomSemaphores.get(chosenRoom).release();
                }
            }
        }

        private Room takeCurrentRoom(){
            int idCurrentRoom = clientRooms.get(clientId);
            return rooms.get(idCurrentRoom);
        }
        private void startOfGame() throws IOException {
            int idCurrentRoom = clientRooms.get(clientId);
            try {
                roomSemaphores.get(idCurrentRoom).acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            out.reset();
            out.writeObject(rooms.get(idCurrentRoom));
            out.flush();
        }
        private void broadcastToSameRoomPlayers() throws IOException {
            int sendingClientRoom = clientRooms.get(clientId);

            for (Map.Entry<Integer, ObjectOutputStream> entry : outputStreams.entrySet()) {
                int targetClientId = entry.getKey();
                ObjectOutputStream targetOutputStream = entry.getValue();

                if (clientRooms.containsKey(targetClientId) && clientRooms.get(targetClientId) == sendingClientRoom) {
                    targetOutputStream.reset();
                    targetOutputStream.writeObject(takeCurrentRoom());
                    targetOutputStream.flush();
                    System.out.println("wysyłąłem pokój do : "+targetClientId);
                }
            }
        }
        private void game() throws IOException {
            while(true){
                int chosenValue = in.readInt();
                String chosenSymbol = in.readUTF();
                if( takeCurrentRoom().getClientsID().get(takeCurrentRoom().getTurn()) == clientId){
                    System.out.println("id klienta: " +clientId);
                    System.out.println("Id tego co ma ture" + takeCurrentRoom().getClientsID().get(takeCurrentRoom().getTurn()));
                    System.out.println(chosenValue+chosenSymbol);


                    if(takeCurrentRoom().checkActualPlay()==4)takeCurrentRoom().resetActualCards();
                    Card card = new Card(chosenSymbol,chosenValue);
                    card.setClientID(clientId);
                    takeCurrentRoom().setActualCard(clientId,card);


                    takeCurrentRoom().nextTurn();
                    broadcastToSameRoomPlayers();
                }
            }
        }


        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                outputStreams.put(clientId, out);

                out.writeInt(clientId);
                out.flush();
                nickname = in.readUTF();

//                System.out.print("Dołączył gracz o nicku:");
//                System.out.println(nickname);

                sendRooms();
                waitOnRoomAndBroadcast();
                startOfGame();
                game();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
