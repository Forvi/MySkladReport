package org.example.myskladreport.controllers;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.controlsfx.control.CheckListView;
import org.controlsfx.control.PopOver;
import org.example.myskladreport.HelloApplication;
import org.example.myskladreport.models.ProductFolder;
import org.example.myskladreport.models.RetailStore;
import org.example.myskladreport.utils.FolderChooser;
import org.example.myskladreport.utils.ReportWriter;
import org.example.myskladreport.utils.SkladRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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

    @FXML
    private Button backButton;

    private ObservableList<RetailStore> retailStores;

    private final String URL = "https://api.moysklad.ru/api/remap/1.2/entity/productfolder/";

    ObservableList<ProductFolder> masterData;

    private FilteredList<ProductFolder> filteredData;

    private SkladRequest skladRequest;

    private Stage stage;
    
    // ======== INIT =============
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        masterData = FXCollections.observableArrayList();
        filteredData = new FilteredList<>(masterData, p -> true);
        checkListView.setItems(filteredData);

        searchHandler();
        lookSelectedHandler();
    }

    // ======== SETTERS =============
    public void setRetailStores(ObservableList<RetailStore> retailStores) {
        if (retailStores.isEmpty() || Objects.isNull(retailStores))
            throw new IllegalArgumentException("Retail Stores cannot be empty or null.");

        this.retailStores = retailStores;
    }

    public void setToken(String token) {
        this.skladRequest = new SkladRequest();
        this.skladRequest.setToken(token);
        loadData();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    // ======== HANDLERS =============
    private void loadData() {
        try {
            var responseGzip = this.skladRequest.sendGetRequest(URL);
            var response = this.skladRequest.unpackedGzip(responseGzip);
            
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode node = objectMapper.readTree(response);
            
            List<ProductFolder> productFolders = this.skladRequest.getProductFoldersFromSklad(node);
            masterData.clear();
            masterData.addAll(productFolders);
            
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

    @FXML
    protected void nextButtonHandler() {
        ObservableList<ProductFolder> productFolders = checkListView.getCheckModel().getCheckedItems();
        
        try {
            String path = FolderChooser.choose(stage, "Выберите папку");
            ReportWriter.write(this.retailStores, productFolders, skladRequest, path);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
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

    @FXML
    protected void questionButtonHandler() {
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

    @FXML
    private void backButtonHandler() throws IOException {
        Stage stage = (Stage) backButton.getScene().getWindow();

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("retail-store.fxml"));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(HelloApplication.class.getResource("styles/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Точки продаж");
    }
}


