package org.example.myskladreport.controllers;

import java.io.IOException;

import org.example.myskladreport.HelloApplication;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class LoginTokenController {

    @FXML
    private Button enterButton;

    @FXML
    private Button enterPassButton;

    @FXML
    private Button questionButton;

    @FXML
    private CheckBox rememberCheckBox;

    @FXML
    private AnchorPane tokenButton;

    @FXML
    private TextField tokenField;

    @FXML
    void onEnterButtonClick(ActionEvent event) {

    }

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
        newStage.centerOnScreen();
        currentStage.close();
        
        newStage.show();
    }
 
    private void checkData(String login, String password) {
        
    }

}
