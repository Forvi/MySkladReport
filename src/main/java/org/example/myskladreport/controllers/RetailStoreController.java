package org.example.myskladreport.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import org.controlsfx.control.CheckListView;
import org.controlsfx.control.PopOver;
import org.example.myskladreport.HelloApplication;
import org.example.myskladreport.models.RetailStore;
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

    @FXML
    private Button exitButton;

    private final String URL = "https://api.moysklad.ru/api/remap/1.2/entity/retailstore";

    ObservableList<RetailStore> masterData;

    private FilteredList<RetailStore> filteredData;

    private List<RetailStore> retailStores;

    private SkladRequest skladRequest;

    // ======== INIT =============
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        masterData = FXCollections.observableArrayList();
        lookSelectedHandler();
        searchHandler();
        checkListView.setItems(filteredData);
    }


    // ======== SETTERS =============
    public void setRetailStores(ObservableList<RetailStore> retailStores) {
        if (retailStores.isEmpty() || Objects.isNull(retailStores))
            throw new IllegalArgumentException("Retail Stores cannot be empty or null.");
    }

    public void setToken(String token) {
        this.skladRequest = new SkladRequest();
        this.skladRequest.setToken(token);
        loadData();
    }

    // ======== HANDLERS =============

    /** 
     * Загрузка всех точек продаж
     * Обработка кнопки "Выбрано"
     */
    private void loadData() {
        try {
            var responseGzip = this.skladRequest.sendGetRequest(URL);
            var response = this.skladRequest.unpackedGzip(responseGzip);
            
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode node = objectMapper.readTree(response);
            
            List<RetailStore> retailStores = this.skladRequest.getRetailStoresFromSklad(node);
            masterData.clear();
            masterData.addAll(retailStores);
            
            selectButton.setOnAction(e -> {
                lookSelectedHandler();
            });

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Показывает выбранные элементы либо его отсутствие 
     */
    private void lookSelectedHandler() {
        selectButton.setOnAction(e -> {
            ObservableList<RetailStore> selected = checkListView.getCheckModel().getCheckedItems();
            StringBuilder sb = new StringBuilder();

            for (var store : selected) {
                sb.append(store.getName()).append("\n");
            }

            Label content = new Label();
            if (sb.isEmpty()) {
                content.setText("Вы ничего не выбрали!");
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

    /**
     * Обработчик кнопки "?"
     * Показывает информацию
     */
    @FXML
    protected void questionButtonHandler() {
        Label text = new Label("- Выберите точки продаж, для которых Вы хотите просмотреть и выгрузить информацию.\n" + 
                                "- Для быстрого поиска введите полное или частичное название в текстовое поле.");
        VBox vbox = new VBox(text);
        vbox.setPadding(new Insets(15));
        PopOver popOver = new PopOver(vbox);
        questionButton.setOnAction(e -> {
            popOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
            popOver.show(questionButton);
        });
    }
    
    /**
     * Обработчик текстового поля
     * Поиск элементов по части названию
     */
    private void searchHandler() {
        filteredData = new FilteredList<>(masterData, p -> true);
        listSearch.setOnAction(event -> {
            String newVal = listSearch.getText();
            filteredData.setPredicate(store -> store.getName().toLowerCase().contains(newVal.toLowerCase()));
        });

        checkListView.setItems(filteredData);
    }

    /** 
     * Обработка кнопки "Далее"
     * Загружается следующее окно "Группы товаров" и передается вся информация
     * 
     * @throws IOException
     */
    @FXML
    private void nextButtonHandler() throws IOException {
        if (checkListView.getCheckModel().getCheckedItems().isEmpty()) {
            showEmptySelectedHandler();
        } else {
            ObservableList<RetailStore> retailStores = checkListView.getCheckModel().getCheckedItems();

            Stage currentStage = (Stage) nextButton.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("product-folder.fxml"));
            Parent root = fxmlLoader.load();
            
            String token = skladRequest.getToken();
            ProductFolderController productFolderController = fxmlLoader.getController();
            productFolderController.setRetailStores(retailStores);
            productFolderController.setToken(token);

            Stage newStage = new Stage();
            newStage.setTitle("Группы товаров");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(HelloApplication.class.getResource("styles/styles.css").toExternalForm());
            newStage.setScene(scene);
            newStage.setResizable(false);
            currentStage.close();
            
            productFolderController.setStage(newStage);
            newStage.show();
        }
    }

    /**
     * Обрабатывает кнопку "Выйти".
     * 
     * @throws IOException 
     */
    @FXML
    private void exitButtonHandler() throws IOException {
        Stage currentStage = (Stage) exitButton.getScene().getWindow();
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

    /**
     * Показывает отсутсвие выбранных точек продаж
     */
    private void showEmptySelectedHandler() {
        Label content = new Label("Вы ничего не выбрали!");

        content.setWrapText(true);
        VBox vbox = new VBox(content);
        vbox.setPadding(new Insets(12));

        PopOver popOver = new PopOver(vbox);
        popOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
        popOver.show(nextButton);
    }

}

