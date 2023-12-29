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
    private static ArrayList<Socket> clients;
    private static HashMap<Integer, ObjectOutputStream> outputStreams;

    private static HashMap<Integer, Room> rooms;


    public static void initObjects() {
        clients = new ArrayList<>();
        outputStreams = new HashMap<>();
        rooms = new HashMap<>();
        rooms.put(1,new Room("Testowy"));
    }

    public static void main(String[] args) {
        initObjects();
        int clientsId = 1;
        ExecutorService executorService = Executors.newCachedThreadPool();
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                clients.add(clientSocket);
                ClientHandler client = new ClientHandler(clientSocket, clientsId,rooms);
                executorService.execute(client);
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
        HashMap<Integer, Room> rooms;


        ObjectInputStream in;
        ObjectOutputStream out;

        public ClientHandler(Socket socket, int clientId,HashMap<Integer, Room> rooms) {
            this.socket = socket;
            this.clientId = clientId;
            this.nickname = "";
            this.rooms = rooms;
        }


        private void sendIDAndTakeNickname() throws IOException {
            out.writeInt(clientId);
            out.flush();
            nickname = in.readUTF();
        }

        private void sendRooms() throws IOException {
            Set<Map.Entry<Integer, Room>> entrySet = rooms.entrySet();

            out.writeInt(entrySet.size());
            out.flush();

            for (Map.Entry<Integer, Room> entry : entrySet) {
                out.writeObject(entry.getKey());   // Wysyła klucz
                out.writeObject(entry.getValue()); // Wysyła wartość
                out.flush();
            }
        }
        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                outputStreams.put(clientId, out);
                sendIDAndTakeNickname();
                System.out.print("Dołączył gracz o nicku:");
                System.out.println(nickname);
                sendRooms();

                while(true){

                }


            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
