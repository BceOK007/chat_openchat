package client;

import javax.swing.*;
import java.io.*;
import java.util.ArrayDeque;

public class FileHistoryService {
    private File fileHistory;
    FileWriter fileWriter;
    FileReader fileReader;
    BufferedReader bufferedReader;
    BufferedWriter bufferedWriter;

    /**
     * Подготовка названия файла и проверка на наличие файла
     * @param login логин пользователя
     * @return true если нужный файл существует, иначе false
     */
    public boolean prepareFile (String login) {
        fileHistory = new File(String.format("history/history_%s.txt", login));
        return fileHistory.exists();
    }

    /**
     * Метод чтения одной строки из файла
     */
    public String loadHistoryRow() throws IOException {
        if(fileReader == null) {
            fileReader = new FileReader(fileHistory);
            bufferedReader = new BufferedReader(fileReader);
        }

        return bufferedReader.readLine();
    }

    /**
     * Метод записи в файл сообщения
     * @param msg сообщение
     */
    public void saveHistoryRow(String msg){
        try {
            if(fileWriter == null) {
                fileWriter = new FileWriter(fileHistory, true);
            }

            fileWriter.write(msg);
            fileWriter.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод, закрывающий доступ к файлам
     */
    public void close(){
        try {
            if(fileWriter != null) {

                    fileWriter.close();
            }
            if(bufferedReader != null) {
                bufferedReader.close();
            }
            if (fileReader != null) {
                fileReader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод получения 100 последних строк из файла
     * @param login логи пользователя
     * @return 100 последних сообщений
     */
    public String loadLast100Message(String login) {
        if(!prepareFile(login)) {
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();
        String row;
        try {
            ArrayDeque<String> arrayDeque = new ArrayDeque<>();

            while ((row = loadHistoryRow()) != null) {
                arrayDeque.addLast(row);
                //если записанная строка 101я, то удаляем первый элемент очереди
                if(arrayDeque.size() > 100) {
                    arrayDeque.removeFirst();
                }
            }

            while(!arrayDeque.isEmpty()) {
                stringBuilder.append(arrayDeque.pollFirst()).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stringBuilder.toString();
    }
}
