package ru.gb;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Main extends Application {

    static Socket socket;
    static DataInputStream in;
    static DataOutputStream out;
    static boolean isConnected = false;
    static String nick;

    static ChatController chatController;
    static LoginController loginController;
    static ScreenController screenController;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
        Scene scene = new Scene(loader.load(), 600, 400);
        loginController = loader.getController();

        screenController = new ScreenController(scene);

        loader = new FXMLLoader(getClass().getResource("/login.fxml"));
        screenController.add("login", loader.load());

        loader = new FXMLLoader(getClass().getResource("/chat.fxml"));
        screenController.add("chat", loader.load());
        chatController = loader.getController();


        stage.setTitle("Чат клиент");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        System.out.println("GETTOTHECHOPPA!");
        try {

            if (!isConnected) {
                return;
            }

            if (socket == null || socket.isClosed()) {
                return;
            }

            out.writeUTF("/end");
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
