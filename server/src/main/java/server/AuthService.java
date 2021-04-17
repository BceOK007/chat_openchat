package server;

import java.sql.SQLException;

public interface AuthService {
    /**
     * Метод получения никнейма по логину и паролю.
     * Если учетки с таким логином и паролем нет, то вернет
     * Если учетка есть, то вернет никнейм.
     * @return никнейм, если есть совпадение по логину и паролю, null, если нет совпадения
     */
    String getNicknameByLoginAndPassword(String login, String password) throws SQLException;

    /**
     * Попытка регистраци новой учетной записи
     * @param login
     * @param password
     * @param nickname
     * @return
     */
    boolean registration (String login, String password, String nickname);

    /**
     * Метод, позволяющий сменить никнейм пользователю
     * @param nickname текущий никнейм
     * @param newNickname новый никнейм
     */
    boolean changeNickname(String nickname, String newNickname);
}
