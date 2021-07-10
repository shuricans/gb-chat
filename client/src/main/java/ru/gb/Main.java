package ru.gb;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Main extends Application {

    static Socket socket;
    static DataInputStream in;
    static DataOutputStream out;
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
        Object object = loader.load();
        Scene scene = new Scene((Parent) object, 600, 400);
        loginController = loader.getController();

        screenController = new ScreenController(scene);
        screenController.add("login", (Pane) object);

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
