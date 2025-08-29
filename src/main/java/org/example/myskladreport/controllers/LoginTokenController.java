package org.example.myskladreport.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.controlsfx.control.PopOver;
import org.example.myskladreport.HelloApplication;
import org.example.myskladreport.utils.SkladRequest;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginTokenController implements Initializable {

    @FXML
    private Button enterButton;

    @FXML
    private Button enterPassButton;

    @FXML
    private Button questionButton;

    @FXML
    private AnchorPane tokenButton;

    @FXML
    private TextField tokenField;

    private SkladRequest skladRequest;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        skladRequest = new SkladRequest();
    }

    /**
     * <p>Обработчик кнопки "Войти"</p>
     * 
     * @param event
     */
    @FXML
    protected void onEnterButtonClick(ActionEvent event) throws IOException, InterruptedException {
        String token = tokenField.getText();

        Stage currentStage = (Stage) enterButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("retail-store.fxml"));

        Parent root = fxmlLoader.load();

        if (token.isEmpty()) {
            showEmptyFieldHandler();
            return;
        }
        
        RetailStoreController retailStoreController = fxmlLoader.getController();
        if (skladRequest.validateToken(token)) {
            retailStoreController.setToken(token);
        } else {
            showNotValidTokenHandler();
            return;
        }

        Stage newStage = new Stage();
        newStage.setTitle("Точки продаж");
        Scene scene = new Scene(root);
        scene.getStylesheets().add(HelloApplication.class.getResource("styles/styles.css").toExternalForm());
        newStage.setScene(scene);
        newStage.setResizable(false);
        newStage.setX(currentStage.getX()); 
        newStage.setY(currentStage.getY());
        currentStage.close();
        
        newStage.show();
    }

    /**
     * <p>Обработчик кнопки "Войти по паролю".</p>
     * 
     * @param event
     */
    @FXML
    protected void enterPassButtonHandler() throws IOException {

        Stage currentStage = (Stage) enterButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-password.fxml"));
        Parent root = fxmlLoader.load();
        
        Stage newStage = new Stage();
        newStage.setTitle("MySklad Report App");
        Scene scene = new Scene(root);
        scene.getStylesheets().add(HelloApplication.class.getResource("styles/styles.css").toExternalForm());
        newStage.setScene(scene);
        newStage.setResizable(false);
        newStage.setX(currentStage.getX()); 
        newStage.setY(currentStage.getY());
        currentStage.close();
        
        newStage.show();
    }

    /**
     * <p>Обработчик кнопки "?".</p>
     */
    @FXML
    protected void questionButtonHandler() {
        Label text = new Label("- Введите логин и пароль от Вашего аккаунта МойСклад");
        VBox vbox = new VBox(text);
        vbox.setPadding(new Insets(15));
        PopOver popOver = new PopOver(vbox);
        questionButton.setOnAction(e -> {
            popOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
            popOver.show(questionButton);
        });
    }
 
    /**
     * <p>Показывает предупреждающий POPUP о незаполненных текстовых полях.</p>
     */
    private void showEmptyFieldHandler() {
        Label content = new Label("Заполните текстовые поля!");

        content.setWrapText(true);
        VBox vbox = new VBox(content);
        vbox.setPadding(new Insets(12));

        PopOver popOver = new PopOver(vbox);
        popOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
        popOver.show(enterButton);
    }

    /**
     * <p>Показывает предупреждающий POPUP о невалидном токене.</p>
     */
    private void showNotValidTokenHandler() {
        Label content = new Label("Данного токена не существует!");

        content.setWrapText(true);
        VBox vbox = new VBox(content);
        vbox.setPadding(new Insets(12));

        PopOver popOver = new PopOver(vbox);
        popOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
        popOver.show(enterButton);
    }




}
