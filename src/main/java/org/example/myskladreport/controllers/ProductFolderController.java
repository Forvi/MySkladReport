package org.example.myskladreport.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.controlsfx.control.CheckListView;
import org.controlsfx.control.PopOver;
import org.example.myskladreport.models.ProductFolder;
import org.example.myskladreport.utils.SkladRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class ProductFolderController implements Initializable {

    @FXML
    private TextField listSearch;

    @FXML
    private CheckListView<ProductFolder> checkListView;

    @FXML
    private Button nextButton;

    @FXML
    private Button questionButton;

    @FXML
    private Button selectButton;

    private final String URL = "https://api.moysklad.ru/api/remap/1.2/entity/productfolder/";

    ObservableList<ProductFolder> masterData;

    private FilteredList<ProductFolder> filteredData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadData();
        searchHandler();
        lookSelectedHandler();
        infoHandler();
    }

    private void loadData() {
        String token = "4a097c0a92d9988ade36bd5cf47ee9c5722a8230";
        SkladRequest skladRequest = new SkladRequest(token);
        
        try {
            var responseGzip = skladRequest.sendGetRequest(URL);
            var response = skladRequest.unpackedGzip(responseGzip);
            
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode node = objectMapper.readTree(response);
            
            List<ProductFolder> productFolders = skladRequest.getProductFoldersFromSklad(node);
            masterData = FXCollections.observableArrayList(productFolders);
            
            selectButton.setOnAction(e -> {
                ObservableList<ProductFolder> selected = checkListView.getCheckModel().getCheckedItems();
                StringBuilder sb = new StringBuilder();

                for (var store : selected) {
                    sb.append(store.getName()).append("\n");
                }

                Label content = new Label(sb.toString());
                content.setWrapText(true);
                VBox vbox = new VBox(content);
                vbox.setPadding(new Insets(12));

                PopOver popOver = new PopOver(vbox);
                popOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
                popOver.show(selectButton);
            });

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

private void lookSelectedHandler() {
    selectButton.setOnAction(e -> {
        ObservableList<ProductFolder> selected = checkListView.getCheckModel().getCheckedItems();
        StringBuilder sb = new StringBuilder();

        for (var store : selected) {
            sb.append(store.getName()).append("\n");
        }

        Label content = new Label();
        if (sb.isEmpty()) {
            content.setText("Вы ничего не выбрали!");;
        } else {
            content.setText(sb.toString());
        }

        content.setWrapText(true);
        VBox vbox = new VBox(content);
        vbox.setPadding(new Insets(12));

        PopOver popOver = new PopOver(vbox);
        popOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
        popOver.show(selectButton);
    });
}

    private void infoHandler() {
        Label text = new Label("- Выберите группы товаров, для которых Вы хотите просмотреть и выгрузить информацию.\n" + 
                                "- Для быстрого поиска введите полное или частичное название в текстовое поле.");
        VBox vbox = new VBox(text);
        vbox.setPadding(new Insets(15));
        PopOver popOver = new PopOver(vbox);
        questionButton.setOnAction(e -> {
            popOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
            popOver.show(questionButton);
        });
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


