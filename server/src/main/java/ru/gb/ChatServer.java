package ru.gb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gb.dao.UserDao;
import ru.gb.dao.UserDaoImpl;
import ru.gb.model.User;
import ru.gb.service.AuthService;
import ru.gb.service.DBSimpleAuthService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ru.gb.ConfigProperties.getPropertyValue;

public class ChatServer {

    private static final Logger LOGGER = LogManager.getLogger(ChatServer.class);

    private final AuthService authService;

    private final List<ClientHandler> clients;

    private final UserDao userDao;

    public ChatServer() {
        clients = new ArrayList<>();
        authService = new DBSimpleAuthService();
        userDao = new UserDaoImpl();
        final int nThreads = Integer.parseInt(getPropertyValue("nThreads"));
        final ExecutorService executorService = Executors.newFixedThreadPool(nThreads);

        try (ServerSocket serverSocket = new ServerSocket(8189)) {
            LOGGER.info("Started");
            while (true) {
                LOGGER.info("Waiting client connect");
                Socket socket = serverSocket.accept();
                LOGGER.info("Anonymous client just connected");
                executorService.submit(new ClientHandler(socket, this));
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            executorService.shutdownNow();
        }
    }

    public synchronized void broadcastClientList() {
        StringBuilder sb = new StringBuilder("/clients ");
        for (ClientHandler client : clients) {
            sb.append(client.getName()).append(" ");
        }
        broadcast(sb.toString());
    }

    public synchronized void broadcast(String msg) {
        for (ClientHandler client : clients) {
            client.sendMessage(msg);
        }
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        LOGGER.info("Client " + clientHandler.getName() + " login");
        clients.add(clientHandler);
        broadcastClientList();
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        LOGGER.info("Client " + clientHandler.getName() + " logout");
        clients.remove(clientHandler);
        broadcastClientList();
    }

    public AuthService getAuthService() {
        return authService;
    }

    public synchronized boolean isNicknameBusy(String nickname) {
        for (ClientHandler client : clients) {
            if (client.getName().equals(nickname)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void sendMsgToClient(ClientHandler from, String nickTo, String msg) {
        for (ClientHandler o : clients) {
            if (o.getName().equals(nickTo)) {
                o.sendMessage("от " + from.getName() + ": " + msg);
                from.sendMessage("клиенту " + nickTo + ": " + msg);
                return;
            }
        }
        from.sendMessage("Участника с ником " + nickTo + " нет в чат-комнате");
    }

    public synchronized void changeNickname(ClientHandler clientHandler, String newNickname) {

        User userWithSameNickname = userDao.findByNickname(newNickname).orElse(null);
        if (userWithSameNickname != null) {
            clientHandler.sendMessage(String.format("SERVER: nickname [%s] is busy.", newNickname));
        } else {
            User user = userDao.findByNickname(clientHandler.getName()).orElse(null);
            if(user != null) {
                user.setNickname(newNickname);
                userDao.update(user);
                broadcast(String.format(
                        "SERVER: [%s] changed his nickname to [%s]",
                        clientHandler.getName(), newNickname)
                );
                clientHandler.setName(newNickname);
                broadcastClientList();
            }
        }
    }
}
