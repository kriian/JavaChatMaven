package ru.gb.lesson6.server;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private BufferedWriter bufferedWriter;
    private String nickname;
    private ExecutorService executorService;
    private static final Logger LOGGER = LogManager.getLogger(ClientHandler.class);

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            bufferedWriter = new BufferedWriter(new FileWriter("demo.txt", true));
            executorService = Executors.newFixedThreadPool(1);
            executorService.execute(myThread());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private Runnable myThread() {
        return () -> {
            try {
                while (true) {
                    String str = in.readUTF();
                    // /auth login1 password1
                    if (str.startsWith("/auth")) {
                        String[] subStrings = str.split(" ", 3);
                        if (subStrings.length == 3) {
                            String nickFromDB = SQLHandler.getNickByLoginAndPassword(subStrings[1], subStrings[2]);
                            if (nickFromDB != null) {
                                if (!server.isNickInChat(nickFromDB)) {
                                    nickname = nickFromDB;
                                    sendMsg("/authok " + nickname);
                                    server.subscribe(this);
                                    break;
                                } else {
                                    sendMsg("This nick already in use");
                                }
                            } else {
                                sendMsg("Wrong login/password");
                            }
                        } else {
                            sendMsg("Wrong data format");
                        }
                    }
                    if (str.startsWith("/registration")) {
                        String[] subStr = str.split(" ");
                        // /registration login pass nick
                        if (subStr.length == 4) {
                            if (SQLHandler.tryToRegister(subStr[1], subStr[2], subStr[3])) {
                                sendMsg("Registration complete");
                            } else {
                                sendMsg("Incorrect login/password/nickname");
                            }
                        }
                    }
                }

                while (true) {
                    String str = in.readUTF();
                    bufferedWriter.append(nickname).append(": ").append(str);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    if (str.startsWith("/")) {
                        if (str.equals("/end")) {
                            server.unsubscribe(this);
                            break;
                        } else if (str.startsWith("/w")) {
                            // /w nick hello m8! hi
                            final String[] subStrings = str.split(" ", 3);
                            if (subStrings.length == 3) {
                                final String toUserNick = subStrings[1];
                                if (server.isNickInChat(toUserNick)) {
                                    server.unicastMsg(toUserNick, "from " + nickname + ": " + subStrings[2]);
                                    sendMsg("to " + toUserNick + ": " + subStrings[2]);
                                } else {
                                    sendMsg("User with nick '" + toUserNick + "' not found in chat room");
                                }
                            } else {
                                sendMsg("Wrong private message");
                            }
                        } else if (str.startsWith("/changenick")) {
                            // /changenick newNick oldNick
                            String[] subStr = str.split(" ");
                            if (!SQLHandler.isSuchNickname(subStr[1])) {
                                SQLHandler.updateNickname(subStr[1], subStr[2]);
                                server.updateList(subStr[2], subStr[1]);
                                nickname = subStr[1];
                            }
                        }
                    } else {
                        LOGGER.info("Сообщение от клиента {} << {} >>", nickname, str);
                        server.broadcastMsg(nickname + ": " + str);
                    }
                }
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
                try {
                    out.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
                try {
                    socket.close();
                    executorService.shutdown();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        };
    }

    public String getNickname() {
        return nickname;
    }
}
