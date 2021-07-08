package ru.gb;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static ru.gb.Main.*;

public class LoginController {

    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField loginField;
    @FXML
    private Label infoLabel;

    public void sendAuth(ActionEvent actionEvent) {

        if(loginField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            return;
        }

        if (socket == null || socket.isClosed()) {
            connect();
            auth();
        }

        try {
            System.out.println("CLIENT: Send auth message");
            out.writeUTF("/auth " + loginField.getText() + " " + passwordField.getText());
            loginField.clear();
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void auth() {
        AuthService authService = new AuthService();

        authService.setOnSucceeded(workerStateEvent -> {
            chatController.startRead();
            screenController.activate("chat");
        });

        infoLabel.textProperty().bind(authService.messageProperty());

        authService.start();
    }

    private void connect() {
        try {
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            isConnected = true;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static class AuthService extends Service<Void> {

        @Override
        protected Task<Void> createTask() {
            return new Task<>() {
                @Override
                protected Void call() throws Exception {
                    while (true) {
                        final String msgAuth = in.readUTF();
                        System.out.println("CLIENT: Received message: " + msgAuth);
                        if (msgAuth.startsWith("/authok")) {
                            nick = msgAuth.split("\\s")[1];
                            updateMessage("Успешная авторизация под ником " + nick + "\n");
                            break;
                        }
                        if("/end".equals(msgAuth)) {
                            break;
                        }
                        updateMessage(msgAuth);
                    }
                    return null;
                }
            };
        }
    }
}
