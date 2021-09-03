package ru.gb;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {

    private final AuthService authService;

    private final List<ClientHandler> clients;

    public ChatServer() {
        clients = new ArrayList<>();
        authService = new DBSimpleAuthService();

        try (ServerSocket serverSocket = new ServerSocket(8189)) {
            System.out.println("SERVER: Server start...");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("SERVER: Client connected...");
                new ClientHandler(socket, this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcastClientList() {
        StringBuilder sb = new StringBuilder("/clients ");
        for (ClientHandler client : clients) {
            sb.append(client.getName()).append(" ");
        }
        broadcast(sb.toString());
    }

    public void broadcast(String msg) {
        for (ClientHandler client : clients) {
            client.sendMessage(msg);
        }
    }

    public void subscribe(ClientHandler clientHandler) {
        System.out.println("SERVER: Client " + clientHandler.getName() + " login...");
        clients.add(clientHandler);
        broadcastClientList();
    }

    public void unsubscribe(ClientHandler clientHandler) {
        System.out.println("SERVER: Client " + clientHandler.getName() + " logout...");
        clients.remove(clientHandler);
        broadcastClientList();
    }

    public AuthService getAuthService() {
        return authService;
    }

    public boolean isNicknameBusy(String nickname) {
        for (ClientHandler client : clients) {
            if(client.getName().equals(nickname)) {
                return true;
            }
        }
        return false;
    }

    public void sendMsgToClient(ClientHandler from, String nickTo, String msg) {
        for (ClientHandler o : clients) {
            if (o.getName().equals(nickTo)) {
                o.sendMessage("от " + from.getName() + ": " + msg);
                from.sendMessage("клиенту " + nickTo + ": " + msg);
                return;
            }
        }
        from.sendMessage("Участника с ником " + nickTo + " нет в чат-комнате");
    }

    public void changeNickname(ClientHandler clientHandler, String newNickname) {

        final String checkNicknameSQL = "SELECT id FROM usr WHERE nickname = ?";
        final String updateNicknameSQL = "UPDATE usr SET nickname = ? WHERE nickname = ?";

        Connection connection = null;
        PreparedStatement prepareStatement = null;

        try {
            final DataSource ds = PooledDataSource.getDataSource();
            connection = ds.getConnection();
            prepareStatement = connection.prepareStatement(checkNicknameSQL);

            prepareStatement.setString(1, newNickname);
            ResultSet resultSet = prepareStatement.executeQuery();
            if (resultSet.next()) {
                clientHandler.sendMessage(String.format("SERVER: nickname [%s] is busy.", newNickname));
            } else {
                prepareStatement = connection.prepareStatement(updateNicknameSQL);
                prepareStatement.setString(1, newNickname);
                prepareStatement.setString(2, clientHandler.getName());
                prepareStatement.executeUpdate();
                broadcast(String.format(
                        "SERVER: [%s] changed his nickname to [%s]",
                        clientHandler.getName(), newNickname)
                );
                clientHandler.setName(newNickname);
                broadcastClientList();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (prepareStatement != null) {
                try {
                    prepareStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
