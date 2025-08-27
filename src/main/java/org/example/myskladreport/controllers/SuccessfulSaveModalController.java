package org.example.myskladreport.controllers;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;

public class SuccessfulSaveModalController {

    @FXML
    private Button closeButton;

    @FXML
    private void onCloseButtonClick(ActionEvent event) {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
