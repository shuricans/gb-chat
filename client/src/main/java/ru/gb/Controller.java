package ru.gb;

import javafx.beans.value.ChangeListener;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Controller implements Initializable {

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nick;

    @FXML
    private HBox clientPanel;
    @FXML
    private HBox msgPanel;
    @FXML
    private TextField textField;
    @FXML
    private Button btnSend;
    @FXML
    private ListView<String> clientList;
    @FXML
    private TextArea textArea;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField loginField;
    @FXML
    private HBox authPanel;

    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

    private void auth() {

        AuthService authService = new AuthService();

        authService.setOnSucceeded(workerStateEvent -> {
            textArea.appendText(workerStateEvent.getSource().getMessage());
            setAuth(true);
            startRead();
        });

        authService.start();
    }

    private void startRead() {

        ReadService readService = new ReadService();

        ChangeListener<Long> countReadListener = (observableValue, prev, current) -> {
            while (!messageQueue.isEmpty()) {
                try {
                    String msgFromServer = messageQueue.take();
                    System.err.println("msgFromServer: " + msgFromServer);
                    if (msgFromServer.startsWith(nick)) {
                        msgFromServer = "[You] " + msgFromServer;
                    }
                    if (msgFromServer.startsWith("/clients")) {
                        final List<String> clients = new ArrayList<>(Arrays.asList(msgFromServer.split("\\s")));
                        clients.remove(0);
                        clientList.getItems().clear();
                        clientList.getItems().addAll(clients);
                        return;
                    }
                    textArea.appendText(msgFromServer + "\n");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        readService.valueProperty().addListener(countReadListener);

        readService.setOnSucceeded(workerStateEvent -> {
            System.out.println("GETTOTHECHOPPA!");
            nick = "";
            setAuth(false);
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        readService.start();
    }

    private void connect() {
        try {
            this.socket = new Socket("localhost", 8189);
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            setAuth(false);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void connectLegacy() {
        try {
            this.socket = new Socket("localhost", 8189);
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            setAuth(false);

            new Thread(() -> {
                try {
                    while (true) { // Ждем сообщения об успешной авторизации ("/authok")
                        final String msgAuth = in.readUTF();
                        System.out.println("CLIENT: Received message: " + msgAuth);
                        if (msgAuth.startsWith("/authok")) {
                            setAuth(true);
                            nick = msgAuth.split("\\s")[1];
                            textArea.appendText("Успешная авторизация под ником " + nick + "\n");
                            break;
                        }
                        textArea.appendText(msgAuth + "\n");
                    }
                    while (true) { // После успешной авторизации можно обрабатывать все сообщения
                        String msgFromServer = in.readUTF();
                        System.out.println("CLIENT: Received message: " + msgFromServer);
                        if (msgFromServer.startsWith(nick)) {
                            msgFromServer = "[You] " + msgFromServer;
                        }
                        if ("/end".equalsIgnoreCase(msgFromServer)) {
                            break;
                        }
                        if (msgFromServer.startsWith("/clients")) {
                            final List<String> clients = new ArrayList<>(Arrays.asList(msgFromServer.split("\\s")));
                            clients.remove(0);
                            clientList.getItems().clear();
                            clientList.getItems().addAll(clients);
                            continue;
                        }
                        textArea.appendText(msgFromServer + "\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                } finally {
                    try {
                        setAuth(false);
                        socket.close();
                        nick = "";
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void setAuth(boolean isAuthSuccess) {
        authPanel.setVisible(!isAuthSuccess);
        authPanel.setManaged(!isAuthSuccess);

        msgPanel.setVisible(isAuthSuccess);
        msgPanel.setManaged(isAuthSuccess);

        clientPanel.setVisible(isAuthSuccess);
        clientPanel.setManaged(isAuthSuccess);
    }

    public void sendAuth(ActionEvent actionEvent) {
//        if (socket == null || socket.isClosed()) {
////            connect();
//            auth();
//        }

        try {
            System.out.println("CLIENT: Send auth message");
            out.writeUTF("/auth " + loginField.getText() + " " + passwordField.getText());
            loginField.clear();
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(ActionEvent actionEvent) {
        try {
            final String msg = textField.getText();
            System.out.println("CLIENT: Send message to server: " + msg);
            out.writeUTF(msg);
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        connect();
        auth();
    }

    public void selectClient(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            final String msg = textField.getText();
            String nickname = clientList.getSelectionModel().getSelectedItem();
            textField.setText("/w " + nickname + " " + msg);
            textField.requestFocus();
            textField.selectEnd();
        }
    }

    private class AuthService extends Service<Void> {

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
                    }
                    return null;
                }
            };
        }
    }

    private class ReadService extends Service<Long> {
        private Long counter = 0L;

        @Override
        protected Task<Long> createTask() {
            return new Task<>() {
                @Override
                protected Long call() throws Exception {
                    while (true) { // После успешной авторизации можно обрабатывать все сообщения
                        final String msgFromServer = in.readUTF();
                        System.out.println("CLIENT: Received message: " + msgFromServer);
                        if ("/end".equalsIgnoreCase(msgFromServer)) {
                            break;
                        }
                        messageQueue.put(msgFromServer);
                        updateValue(++counter);
                    }
                    return counter;
                }
            };
        }
    }
}
