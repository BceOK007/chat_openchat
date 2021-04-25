package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static ServerSocket server;
    private static Socket socket;

    private static final int PORT = 8189;
    private List<ClientHandler> clients;
    private AuthService authService;

    private static Connection databaseConnection;
    private static Statement stmt;

    private ExecutorService executorService = Executors.newCachedThreadPool();

    public Server() {
        clients = new CopyOnWriteArrayList<>();
//        authService = new SimpleAuthService();

        try {
            server = new ServerSocket(PORT);
            System.out.println("Server started");

            //Коннектимся к БД
            connectDatabase();
            authService = new DatabaseAuthService(databaseConnection, stmt);
            System.out.println("Connection database");

            while(true){
                socket = server.accept();
                System.out.println(socket.getLocalSocketAddress());
                System.out.println("Client connect: "+ socket.getRemoteSocketAddress());
                new ClientHandler(this, socket, executorService);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Failed connect to database");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            //закрываем соединение с БД
            discinnectDatabase();

            //закрываем сервис
            executorService.shutdown();

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

    public void broadcastMsg(ClientHandler sender, String msg){
        String message = String.format("%s: %s", sender.getNickname(), msg);
        for (ClientHandler c : clients) {
            c.sendMsg(message);
        }
    }

    public void privateMsg(ClientHandler sender, String receiver, String msg){
        String message = String.format("[ %s ] to [ %s ] : %s", sender.getNickname(), receiver, msg);
        for (ClientHandler c : clients) {
            if(c.getNickname().equals(receiver)) {
                c.sendMsg(message);
                if (sender.equals(c)) {
                    break;
                }
                sender.sendMsg(message);
                return;
            }
        }
        sender.sendMsg("not found user: " + receiver);
    }

    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastClientList();
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastClientList();
    }

    public AuthService getAuthService() {
        return authService;
    }

    public boolean isLoginAuthenticated (String login) {
        for (ClientHandler c : clients) {
            if(c.getLogin().equals(login)) {
                return true;
            }
        }

        return false;
    }

    public void broadcastClientList() {
        StringBuilder sb = new StringBuilder("/clientlist");
        for (ClientHandler c : clients) {
            sb.append(" ").append(c.getNickname());
        }

        String msg = sb.toString();

        for (ClientHandler c : clients) {
            c.sendMsg(msg);
        }
    }

    private void connectDatabase() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        databaseConnection = DriverManager.getConnection("jdbc:sqlite:database.db");
        stmt = databaseConnection.createStatement();
    }

    private void discinnectDatabase() {
        try {
            if(stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if(databaseConnection != null) {
                databaseConnection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
