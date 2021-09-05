package ru.gb;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import ru.gb.logger.MsgLoggerImpl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import static ru.gb.Main.*;

public class LoginController implements Initializable {

    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField loginField;
    @FXML
    private Label infoLabel;
    @FXML
    private Label timerLabelPrev;
    @FXML
    private Label timerLabel;
    @FXML
    private Label timerLabelNext;
    @FXML
    private Hyperlink resetLink;
    @FXML
    private Button authButton;

    private Timer timer;

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

    private Path getFilePath() {
        StringBuilder builderPath = new StringBuilder(System.getProperty("user.home"));
        builderPath.append("\\history_");
        builderPath.append(nick);
        builderPath.append(".txt");
        return Paths.get(builderPath.toString());
    }

    private void auth() {
        AuthService authService = new AuthService();

        authService.setOnSucceeded(workerStateEvent -> {
            timer.cancel();
            logger = new MsgLoggerImpl(getFilePath());
            chatController.loadHistory(100);
            chatController.startRead();
            screenController.activate("chat");
            infoLabel.textProperty().unbind();
            infoLabel.setText("");
            infoLabel.setVisible(false);
        });

        infoLabel.setVisible(true);
        infoLabel.textProperty().bind(authService.messageProperty());

        authService.start();
    }

    private void connect() {
        try {
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void startTimer() {
        timer = new Timer(20);

        timer.setOnSucceeded(workerStateEvent -> {
            System.out.println("timeout");
            try {
                out.writeUTF("/end");
            } catch (IOException e) {
                e.printStackTrace();
            }
            passwordField.setDisable(true);
            loginField.setDisable(true);
            authButton.setDisable(true);
            timerLabelPrev.setText("");
            timerLabel.textProperty().unbind();
            timerLabel.setText("");
            timerLabelNext.setText("");
            timerLabel.setText("Время вышло... ");
            resetLink.setVisible(true);
        });

        timerLabel.textProperty().bind(timer.valueProperty().asString());

        timer.start();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        connect();
        auth();
        startTimer();
    }

    public void resetTimer(ActionEvent actionEvent) {
        passwordField.setDisable(false);
        loginField.setDisable(false);
        authButton.setDisable(false);
        timerLabelPrev.setText("Осталось ");
        timerLabelNext.setText(" c");
        resetLink.setVisible(false);
        resetLink.setVisited(false);
        connect();
        auth();
        startTimer();
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

    private static class Timer extends Service<Integer> {

        private Integer timer;

        public Timer(int time) {
            this.timer = time;
        }

        @Override
        protected Task<Integer> createTask() {
            return new Task<>() {
                @Override
                protected Integer call() throws Exception {
                    while (timer > 0) {
                        if(isCancelled()) {
                            break;
                        }
                        updateValue(timer--);
                        Thread.sleep(1000);
                    }
                    return timer;
                }
            };
        }
    }
}
