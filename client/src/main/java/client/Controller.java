package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    public TextArea textArea;
    @FXML
    public TextField textField;
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public HBox authPanel;
    @FXML
    public HBox msgPanel;
    @FXML
    public ListView<String> clientList;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private final String IP_ADDRESS = "localhost";
    private final int PORT = 8189;

    private boolean authenticated;
    private String nickname;

    private Stage stage;
    private Stage regStage;
    private  RegControler regControler;

    private String login;
    private FileHistoryService fileHistoryService = new FileHistoryService();

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
        msgPanel.setVisible(authenticated);
        msgPanel.setManaged(authenticated);
        clientList.setVisible(authenticated);
        clientList.setManaged(authenticated);

        if(!authenticated) {
            nickname = "";
            fileHistoryService.close();
        }
        setTitle(nickname);
        textArea.clear();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(()-> {
            stage = (Stage)textArea.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                System.out.println("bye");
                if(socket != null && !socket.isClosed()) {
                    try {
                        out.writeUTF("/end");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
        setAuthenticated(false);
    }

    private void connect(){
        try {
            socket = new Socket(IP_ADDRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    //цмкл аутентификации
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/")) {
                            if (str.equals("/end")) {
                                break;
                            }
                            if(str.startsWith("/auth_ok")) {
                                nickname = str.split("\\s+")[1];
                                setAuthenticated(true);

                                textArea.appendText(fileHistoryService.loadLast100Message(login));

                                break;
                            }
                            if (str.startsWith("/reg_ok")) {
                                regControler.showResult("/reg_ok");
                            }
                            if (str.startsWith("/reg_no")) {
                                regControler.showResult("/reg_no");
                            }

                        } else{

                            textArea.appendText(str + "\n");
                        }
                    }

                    //цмкл работы
                    while (authenticated) {
                        String str = in.readUTF();

                        if (str.startsWith("/")) {
                            if (str.equals("/end")) {
                                break;
                            }

                            //обновление списка клиентов
                            if (str.startsWith("/clientlist")) {
                                String[] token = str.split("\\s+");
                                Platform.runLater(()-> {
                                    clientList.getItems().clear();
                                    for (int i = 1; i < token.length; i++) {
                                        clientList.getItems().add(token[i]);
                                    }
                                });
                            }

                            //изменяем титул у окна и никнайм
                            if (str.startsWith("/changeNick_ok")) {
                                String[] token = str.split("\\s+", 2);
                                nickname = token[1];
                                setTitle(nickname);
                            }
                        } else {
                            textArea.appendText(str + "\n");
                            fileHistoryService.saveHistoryRow(str + "\n");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("disconnect");
                    setAuthenticated(false);
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

    @FXML
    public void sendMsg(ActionEvent actionEvent) throws IOException {
        try {
            out.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void tryToAuth(ActionEvent actionEvent) {
        if(socket == null || socket.isClosed()) {
            connect();
        }
        String msg = String.format("/auth %s %s", loginField.getText().trim(), passwordField.getText().trim());

        login = loginField.getText().trim();

        try {
            out.writeUTF(msg);
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setTitle(String nickname) {
        Platform.runLater(()-> {
            if(nickname.equals("")) {
                stage.setTitle("Open chat");
            } else
            {
                stage.setTitle(String.format("Open chat: [ %s ]", nickname));
            }
        });
    }

    public void clickClientList(MouseEvent mouseEvent) {
        String receiver = clientList.getSelectionModel().getSelectedItem();
        textField.setText("/w " + receiver + " ");
    }

    private void createRegWindow(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/reg.fxml"));
            Parent root = fxmlLoader.load();
            regStage = new Stage();
            regStage.setTitle("Registration");
            regStage.setScene(new Scene(root, 350, 300));

            regStage.initModality(Modality.APPLICATION_MODAL);
            regStage.initStyle(StageStyle.UTILITY);

            regControler = fxmlLoader.getController();
            regControler.setController(this);

            regControler.loginField.clear();
            regControler.passwordField.clear();
            regControler.nickField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToReg(ActionEvent actionEvent) {
        if(regStage == null) {
            createRegWindow();
        }
        Platform.runLater(()-> {
            regStage.show();
        });
    }

    public void registration(String login, String password, String nickname) {
        if (socket == null || socket.isClosed()) {
            connect();
        }
        String msg = String.format("/reg %s %s %s", login, password, nickname);
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
