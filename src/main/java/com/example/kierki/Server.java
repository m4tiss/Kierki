package com.example.kierki;

import java.io.*;
import java.net.*;
import java.util.HashMap;
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
                System.out.println("przyjalem");
                clients.add(clientSocket);
                ClientHandler client = new ClientHandler(clientSocket, clientsId);
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

        public ClientHandler(Socket socket, int clientId) {
            this.socket = socket;
            this.clientId = clientId;
            this.nickname = "";
        }

        @Override
        public void run() {
            try {
                System.out.println("robie");
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                outputStreams.put(clientId, out);
                out.writeInt(clientId);
                out.flush();
                nickname = in.readUTF();
                System.out.println(nickname);
                while(true){

                }


            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
