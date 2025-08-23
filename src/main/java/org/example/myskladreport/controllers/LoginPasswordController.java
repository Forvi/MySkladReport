package org.example.myskladreport.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import org.controlsfx.control.PopOver;
import org.example.myskladreport.HelloApplication;
import org.example.myskladreport.utils.SkladRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginPasswordController implements Initializable {

    @FXML
    private Button enterButton;

    @FXML
    private TextField loginField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button questionButton;

    @FXML
    private Button enterTokenButton;

    private SkladRequest skladRequest;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        skladRequest = new SkladRequest();
    }

    @FXML
    protected void onEnterButtonClick() throws IOException {
        String login = loginField.getText();
        String password = passwordField.getText();

        if (login.isEmpty() || password.isEmpty()) {
            showEmptyFieldHandler();
            return;
        }

        String token = generateToken(login, password);

        if (Objects.isNull(token)) {
            showErrorFieldHandler();
            return;
        }

        Stage currentStage = (Stage) enterButton.getScene().getWindow();
        currentStage.centerOnScreen();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("retail-store.fxml"));
        Parent root = fxmlLoader.load();
        
        RetailStoreController retailStoreController = fxmlLoader.getController();
        retailStoreController.setToken(token);

        Stage newStage = new Stage();
        newStage.setTitle("Точки продаж");
        Scene scene = new Scene(root);
        scene.getStylesheets().add(HelloApplication.class.getResource("styles/styles.css").toExternalForm());
        newStage.setScene(scene);
        newStage.setResizable(false);
        newStage.centerOnScreen();
        currentStage.close();
        
        newStage.show();
    }

    @FXML
    protected void onEnterTokenButton() throws IOException {
        Stage currentStage = (Stage) enterButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-token.fxml"));
        Parent root = fxmlLoader.load();
        
        Stage newStage = new Stage();
        newStage.setTitle("MySklad Report App");
        Scene scene = new Scene(root);
        scene.getStylesheets().add(HelloApplication.class.getResource("styles/styles.css").toExternalForm());
        newStage.setScene(scene);
        newStage.setResizable(false);
        newStage.centerOnScreen();
        currentStage.close();
        
        newStage.show();
    }

    @FXML
    protected void questionButtonHandler() {
        Label text = new Label("- Введите логин и пароль от Вашего аккаунта МойСклад\n" + 
                                "- Если Вы не хотите каждый раз вводить данные для входа, активируйте 'Запомнить'\n" +
                                "- Не переживайте за данные, при функции 'Запомнить' - логин и пароль шифруются");
        VBox vbox = new VBox(text);
        vbox.setPadding(new Insets(15));
        PopOver popOver = new PopOver(vbox);
        questionButton.setOnAction(e -> {
            popOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
            popOver.show(questionButton);
        });
    }

    private void showEmptyFieldHandler() {
        Label content = new Label("Заполните текстовые поля!");

        content.setWrapText(true);
        VBox vbox = new VBox(content);
        vbox.setPadding(new Insets(12));

        PopOver popOver = new PopOver(vbox);
        popOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
        popOver.show(enterButton);
    }

    private void showErrorFieldHandler() {
        Label content = new Label("Ошибка аутентификации: Неправильный пароль или имя пользователя!");

        content.setWrapText(true);
        VBox vbox = new VBox(content);
        vbox.setPadding(new Insets(12));

        PopOver popOver = new PopOver(vbox);
        popOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
        popOver.show(enterButton);
    }

    private String generateToken(String login, String password) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String response = skladRequest.getNewTokenByLogin(login, password);
            var token = objectMapper.readTree(response).get("access_token");

            if (Objects.isNull(token)) {
                return null;
            }

            return token.asText();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
