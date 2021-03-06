package ru.gb;

import javafx.beans.value.ChangeListener;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import ru.gb.logger.MsgLoggerImpl;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static ru.gb.Main.*;

public class ChatController implements Initializable {

    @FXML
    private TextField textField;
    @FXML
    private ListView<String> clientList;
    @FXML
    private TextArea textArea;

    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();


    protected void loadHistory(int countRows) {
        textArea.clear();
        List<String> lastRows = logger.getLastRows(countRows);
        if (lastRows != null && !lastRows.isEmpty()) {
            lastRows.forEach(msg -> textArea.appendText(msg + MsgLoggerImpl.NEW_LINE));
        }
    }


    protected void startRead() {
        ReadService readService = new ReadService();

        ChangeListener<Long> countReadListener = (observableValue, prev, current) -> {
            while (!messageQueue.isEmpty()) {
                try {
                    String msgFromServer = messageQueue.take();
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
                    logger.write(msgFromServer);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        readService.valueProperty().addListener(countReadListener);

        readService.setOnSucceeded(workerStateEvent -> {
            System.out.println("LOGOUT EVENT BY \"/END\" COMMAND...");
            nick = "";
            try {
                in.close();
                out.close();
                socket.close();
                screenController.activate("login");
                loginController.resetTimer(new ActionEvent());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        readService.start();
    }

    public void sendMsg(ActionEvent actionEvent) {
        try {
            final String msg = textField.getText().trim();

            if (msg.isEmpty()) {
                return;
            }

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


    private class ReadService extends Service<Long> {
        private Long counter = 0L;

        @Override
        protected Task<Long> createTask() {
            return new Task<>() {
                @Override
                protected Long call() throws Exception {
                    while (true) { // ?????????? ???????????????? ?????????????????????? ?????????? ???????????????????????? ?????? ??????????????????
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
