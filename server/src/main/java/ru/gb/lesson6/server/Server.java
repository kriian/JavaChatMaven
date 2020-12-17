package ru.gb.lesson6.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server {
    private Vector<ClientHandler> clients;

    public Server() {
        try {
            SQLHandler.connect();
            ServerSocket serverSocket = new ServerSocket(8189);
            clients = new Vector<>();

            while (true) {
                System.out.println("Ждем подключение клиента...");
                Socket socket = serverSocket.accept();
                ClientHandler c = new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            SQLHandler.disconnect();
        }
    }

    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    public void broadcast(String msg) {
        for (ClientHandler c : clients) {
            c.sendMsg(msg);
        }

    }
}
