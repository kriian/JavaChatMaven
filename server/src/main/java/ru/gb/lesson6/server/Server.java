package ru.gb.lesson6.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private Map<String, ClientHandler> clients;

    public Server() {
        try {
            SQLHandler.connect();
            ServerSocket serverSocket = new ServerSocket(8189);
            clients = new ConcurrentHashMap<>();
            while (true) {
                System.out.println("Ждем подключения клиента");
                Socket socket = serverSocket.accept();
                ClientHandler c = new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            SQLHandler.disconnect();
        }
    }

    public void subscribe(ClientHandler client) {
        broadcastMsg(client.getNickname() + " in chat now");
        clients.put(client.getNickname(), client);
        client.sendMsg("Welcome to chat");
        broadcastClientList();
    }

    public void unsubscribe(ClientHandler client) {
        broadcastMsg(client.getNickname() + " leaves chat");
        clients.remove(client.getNickname());
        broadcastClientList();
    }

    public boolean isNickInChat(String nickname) {
        return clients.containsKey(nickname);
    }

    public void broadcastMsg(String msg) {
        for (ClientHandler c : clients.values()) {
            c.sendMsg(msg);
        }
    }

    public void unicastMsg(String nickname, String msg) {
        if (isNickInChat(nickname)) {
            ClientHandler clientHandler = clients.get(nickname);
            clientHandler.sendMsg(msg);
        }
    }

    public void broadcastClientList() {
        StringBuilder sb = new StringBuilder();
        sb.append("/clientslist");
        // /clientslist nick1 nick2 nick3
        for (String nick : clients.keySet()) {
            sb.append(" " + nick);
        }
        broadcastMsg(sb.toString());
    }
}
