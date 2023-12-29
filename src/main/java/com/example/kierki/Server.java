package com.example.kierki;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;

public class Server {
    private static final int PORT = 8888;

    //mapa do trzyamania strumień klientów idKlienta, stream
    private static HashMap<Integer, ObjectOutputStream> outputStreams;

    //mapa do trzymania pokoi idRoom , Room
    private static HashMap<Integer, Room> rooms;

    //mapa do trzymania klient pokój idRoom,idClient
    private static HashMap<Integer, Integer> clientRooms;

    private static int idRoom;
    private static int clientsId;


    public static void initObjects() {
        outputStreams = new HashMap<>();
        rooms = new HashMap<>();
        idRoom = 1;
        clientsId = 1;
        rooms.put(idRoom, new Room("Testowy"));
        idRoom++;
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
                out.writeObject(entry.getKey());
                out.writeObject(entry.getValue());
                out.flush();
            }
        }

//        private void waitOnRoom() throws IOException, ClassNotFoundException {
//            chosenRoom = (Integer) in.readObject();
//            rooms.get(chosenRoom).addPlayer(nickname);
//            Room toSend = rooms.get(chosenRoom);
//            out.reset();
//            out.writeObject(toSend);
//            out.flush();
//            for (ObjectOutputStream outClient : outputStreams.values()) {
//                if (outClient != null) {
//                    if (outClient != out) {
//                        outClient.writeInt(chosenRoom);
//                        outClient.flush();
//                    }
//                }
//            }
//
//        }

        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                outputStreams.put(clientId, out);

                nickname = in.readUTF();
                System.out.print("Dołączył gracz o nicku:");
                System.out.println(nickname);
                sendRooms();
//                waitOnRoom();
                while (true) {

                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
