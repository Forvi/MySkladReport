package org.example.myskladreport.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.example.myskladreport.HelloApplication;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginPasswordController implements Initializable {

    @FXML
    private Button enterButton;

    @FXML
    private TextField loginField;

    @FXML
    private TextField passwordField;

    @FXML
    private Button questionButton;

    @FXML
    private Button enterTokenButton;

    @FXML
    protected void onEnterButtonClick() throws IOException {

        Stage currentStage = (Stage) enterButton.getScene().getWindow();
        currentStage.centerOnScreen();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("retail-store.fxml"));
        Parent root = fxmlLoader.load();
        
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
