package org.example.myskladreport.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

import javafx.scene.control.Alert;

public class ErrorLogger {
    private static final Path logFilePath = Paths.get("error.log");

    /**
     * <p>Сохраняет появившееся ошибки</p>
     * 
     * @param e отловленное исключение
     */
    public static void logAndShowError(Exception e) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String fullError = LocalDateTime.now() + ":\n" + sw.toString() + "\n-----------------------------\n";
            Files.write(logFilePath,
                        fullError.getBytes(),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            System.err.println(fullError);
            e.printStackTrace();
            showErrorAlert("Ошибка", "Произошла ошибка: " + e.getClass().getSimpleName());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    /**
     * <p>Модульное окно, сообщающее об ошибке</p>
     * 
     * @param title
     * @param message
     */
    private static void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
