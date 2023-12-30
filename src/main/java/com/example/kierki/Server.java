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

            System.out.println("ID KLIENTA: "+clientId);
            System.out.println("ID POKOJU: "+sendingClientRoom);
            System.out.println("NICKNAME: "+nickname);

            for (Map.Entry<Integer, ObjectOutputStream> entry : outputStreams.entrySet()) {
                int targetClientId = entry.getKey();
                ObjectOutputStream targetOutputStream = entry.getValue();

                if (clientRooms.containsKey(targetClientId) && clientRooms.get(targetClientId) == sendingClientRoom) {
                    targetOutputStream.writeInt(rooms.get(chosenRoom).getAmountOfPlayers());
                    targetOutputStream.flush();
                    System.out.println("wysyłąłem do : "+targetClientId);
                }
            }
            if(currentRoom.getAmountOfPlayers()==4){
                for (int i = 0; i < 4; i++) {
                    roomSemaphores.get(chosenRoom).release();
                }
            }
        }

        private void game() throws IOException {
            int idCurrentRoom = clientRooms.get(clientId);
            try {
                System.out.println("czekam na semaforze");
                roomSemaphores.get(idCurrentRoom).acquire();
                System.out.println("klient"+ clientId+"poszedlem za watek");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Tura"+rooms.get(idCurrentRoom).getTurn());
            out.reset();
            out.writeObject(rooms.get(idCurrentRoom));
            out.flush();
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

                System.out.print("Dołączył gracz o nicku:");
                System.out.println(nickname);

                sendRooms();
                waitOnRoomAndBroadcast();
                game();
                while (true) {

                }
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
