package org.example.myskladreport.controllers;

import java.io.IOException;

import org.example.myskladreport.HelloApplication;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginPasswordController {

    @FXML
    private Button enterButton;

    @FXML
    private TextField loginField;

    @FXML
    private TextField passwordField;

    @FXML
    private Button questionButton;


    @FXML
    protected void onEnterButtonClick() throws IOException {

        Stage currentStage = (Stage) enterButton.getScene().getWindow();
        
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("retail-store.fxml"));
        Parent root = fxmlLoader.load();
        
        Stage newStage = new Stage();
        newStage.setTitle("Точки продаж");
        newStage.setScene(new Scene(root));
        newStage.setResizable(false);
        
        currentStage.close();
        
        newStage.show();

    }
}
