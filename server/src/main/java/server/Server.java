package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private static ServerSocket server;
    private static Socket socket;

    private static final int PORT = 8189;
    private List<ClientHandler> clients;
    private AuthService authService;

    public Server() {
        clients = new CopyOnWriteArrayList<>();
        authService = new SimpleAuthService();

        try {
            server = new ServerSocket(PORT);
            System.out.println("Server started");

            while(true){
                socket = server.accept();
                System.out.println(socket.getLocalSocketAddress());
                System.out.println("Client connect: "+ socket.getRemoteSocketAddress());
                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Отправка сообщения всем подключенным пользователям
     * @param sender отправитель сообщения
     * @param msg текст сообщения
     */
    public void broadcastMsg(ClientHandler sender, String msg){
        String message = String.format("%s: %s", sender.getNickname(), msg);
        for (ClientHandler c : clients) {
            c.sendMsg(message);
        }
    }

    /**
     * Отправка сообщения только отправителю и указанному пользователю
     * @param sender отправитель
     * @param msg текст сообщения
     * @param recipient получатель
     */
    public void broadcastMsg(ClientHandler sender, String msg, ClientHandler recipient){
        String message = String.format("%s: %s", sender.getNickname(), msg);
        for (ClientHandler c : clients) {
            //сообщение отправляем только отправителю и указанному получателю
            if(c.getNickname().equals(sender.getNickname()) || c.getNickname().equals(recipient.getNickname())) {
                c.sendMsg(message);
            }
        }
    }

    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    public AuthService getAuthService() {
        return authService;
    }

    /**
     * Поиск пользователя в списке подключенных по его нику
     * @param nickname никнейм искомого пользователя
     * @return ссылка на найденого пользователя, при нейдачном поиске null
     */
    public ClientHandler getRecipient(String nickname){
        for (ClientHandler c : clients) {
            if(c.getNickname().equals(nickname)) {
                return c;
            }
        }
        return null;
    }
}
