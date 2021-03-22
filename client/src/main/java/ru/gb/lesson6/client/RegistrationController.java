package ru.gb.lesson6.client;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class RegistrationController {
    private static final Logger LOGGER = LogManager.getLogger(RegistrationController.class);
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    @FXML
    TextField login, password, nickname;

    @FXML
    Label result;

    public void tryToRegister() {
        try {
            if (socket == null || socket.isClosed()) {
                socket = new Socket("localhost", 8189);
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
                // /registration login pass nick
                out.writeUTF("/registration " + login.getText() + " " + password.getText() + " " + nickname.getText());
                String answer = in.readUTF();
                result.setText(answer);
            }
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
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
                socket.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }
}
