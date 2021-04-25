package server;

import java.sql.*;

public class DatabaseAuthService implements AuthService{
    private Connection connection;
    private Statement statement;

    public DatabaseAuthService(Connection connection, Statement statement) {
        this.connection = connection;
        this.statement = statement;
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {

        String sql = "SELECT nickname FROM users where login = '%s' and password = '%s'";

        try {
            ResultSet resultSet = statement.executeQuery(String.format(sql, login, password));
            if(resultSet.next()) {
                String nickname = resultSet.getString("nickname");
                return nickname;
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;

    }


    @Override
    public boolean registration(String login, String password, String nickname) {

        if(isLoginFree(login) && isNicknameFree(nickname)) {

            try {
                statement.executeUpdate(String.format("INSERT INTO users (login, password, nickname) VALUES ('%s', '%s', '%s')", login, password, nickname));
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Проверяет не занят ли никнайм
     * @param nickname
     * @return true - если свободен, иначе false
     */
    private boolean isNicknameFree (String nickname) {
        String sql = "SELECT COUNT(*) as cnt FROM users where nickname = '%s'";

        try {
            ResultSet resultSet = statement.executeQuery(String.format(sql, nickname));
            if(resultSet.next()) {
                if(resultSet.getInt("cnt") > 0) {
                    return false;
                }
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * Проверяет не занят ли логин
     * @param login
     * @return true - если свободен, иначе false
     */
    private boolean isLoginFree (String login) {
        String sql = "SELECT COUNT(*) as cnt FROM users where login = '%s'";

        try {
            ResultSet resultSet = statement.executeQuery(String.format(sql, login));
            if(resultSet.next()) {
                if(resultSet.getInt("cnt") > 0) {
                    return false;
                }
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public boolean changeNickname(String nickname, String newNickname) {

        if(isNicknameFree(newNickname)) {
            try {
                statement.executeUpdate(String.format("UPDATE users SET nickname = '%s' WHERE nickname = '%s'", newNickname, nickname));
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return true;
        }

        return false;
    }
}
