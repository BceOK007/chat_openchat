package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private String nickname;

    public ClientHandler(Server server, Socket socket) {
        try{
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    //цикл аутентификации
                    while (true) {
                        String str = in.readUTF();

                        if (str.equals("/end")) {
                            out.writeUTF("/end");
                            break;
                        }

                        if(str.startsWith("/auth")) {
                            String[] token = str.split("\\s+");
                            String newNick = server.getAuthService().getNicknameByLoginAndPassword(token[1], token[2]);
                            if (newNick != null) {
                                nickname = newNick;
                                sendMsg("/auth_ok " + nickname);
                                server.subscribe(this);
                                System.out.println("Client authenticated. nick: " + nickname + " Address: " + socket.getRemoteSocketAddress());
                                break;
                            } else {
                                sendMsg("Неверный логин / пароль");
                            }
                        }
                    }

                    //цикл работы
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/")) {

                            if (str.equals("/end")) {
                                out.writeUTF("/end");
                                break;
                            }
                            //Отправка сообщения только одному юзеру
                            if(str.startsWith("/w ")) {
                                String[] oneMsg = str.split("\\s+", 3);
                                ClientHandler recipient = server.getRecipient(oneMsg[1]);
                                //проверяем есть ли указзанный юзер среди подключенных
                                if(oneMsg.length == 3) {
                                    if(recipient != null) {
                                        server.broadcastMsg(this, oneMsg[2], recipient);
                                    } else {
                                        sendMsg(String.format("Пользователь с указанным ником %s не в сети", oneMsg[1]));
                                    }
                                } else {
                                    sendMsg("Неправильный формат команды /w");
                                }
                            }
                        } else {
                            server.broadcastMsg(this, str);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    server.unsubscribe(this);
                    System.out.println("Client disconnect " + socket.getRemoteSocketAddress());
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg){
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickname() {
        return nickname;
    }
}
