package server;

public interface AuthService {
    /**
     * Метод получения никнейма по логину и паролю.
     * Если учетки с таким логином и паролем нет, то вернет
     * Если учетка есть, то вернет никнейм.
     * @return никнейм, если есть совпадение по логину и паролю, null, если нет совпадения
     */
    String detNicknameByLoginAndPassword(String login, String password);

    /**
     * Попытка регистраци новой учетной записи
     * @param login
     * @param password
     * @param nickname
     * @return
     */
    boolean registration (String login, String password, String nickname);
}
