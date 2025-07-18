package org.example.myskladreport.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.controlsfx.control.CheckListView;
import org.example.myskladreport.models.RetailStore;
import org.example.myskladreport.utils.SkladRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class RetailStoreController implements Initializable {

    @FXML
    private TextField listSearch;

    @FXML
    private CheckListView<RetailStore> checkListView;

    @FXML
    private Button nextButton;

    @FXML
    private Button questionButton;

    @FXML
    private Button selectButton;

    private final String URL = "https://api.moysklad.ru/api/remap/1.2/report/profit/bysaleschannel";

    ObservableList<RetailStore> masterData;

    private FilteredList<RetailStore> filteredData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadData();
        searchHandler();
    }

    private void loadData() {
        String token = "4a097c0a92d9988ade36bd5cf47ee9c5722a8230";
        SkladRequest skladRequest = new SkladRequest(token);
        
        try {
            var responseGzip = skladRequest.sendGetRequest(URL);
            var response = skladRequest.unpackedGzip(responseGzip);
            
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode node = objectMapper.readTree(response);
            
            List<RetailStore> retailStores = skladRequest.getRetailStoresFromSklad(node);
            masterData = FXCollections.observableArrayList(retailStores);
            
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    private void searchHandler() {
        filteredData = new FilteredList<>(masterData, p -> true);
        listSearch.setOnAction(event -> {
            String newVal = listSearch.getText();
            filteredData.setPredicate(store -> store.getName().toLowerCase().contains(newVal.toLowerCase()));
        });

        checkListView.setItems(filteredData);
    }

}

