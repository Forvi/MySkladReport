package org.example.myskladreport.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.example.myskladreport.HelloApplication;
import org.example.myskladreport.utils.SkladRequest;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class LoginTokenController implements Initializable {

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

    private SkladRequest skladRequest;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        skladRequest = new SkladRequest();
    }

    @FXML
    void onEnterButtonClick(ActionEvent event) throws IOException {
        String token = tokenField.getText();

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
 






}
