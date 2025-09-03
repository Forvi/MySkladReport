package org.example.myskladreport.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import org.controlsfx.control.ListSelectionView;
import org.controlsfx.control.PopOver;
import org.example.myskladreport.HelloApplication;
import org.example.myskladreport.models.RetailStore;
import org.example.myskladreport.utils.ErrorLogger;
import org.example.myskladreport.utils.SkladRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RetailStoreController implements Initializable {

    @FXML
    private Button exitButton;

    @FXML
    private TextField listSearchAvailable;

    @FXML
    private TextField listSearchSelected;

    @FXML
    private Button nextButton;

    @FXML
    private ListSelectionView<RetailStore> listSelectionView;

    @FXML
    private Button questionButton;

    @FXML
    private Button selectButton;

    List<RetailStore> retailStores;

    private final ObservableList<RetailStore> availableRetailStores = FXCollections.observableArrayList();

    private final ObservableList<RetailStore> selectedRetailStores = FXCollections.observableArrayList();

    private SkladRequest skladRequest;

    private final String URL = "https://api.moysklad.ru/api/remap/1.2/entity/retailstore";

    // ======== INIT =============
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        listSelectionView.setSourceHeader(new Label(""));
        listSelectionView.setTargetHeader(new Label(""));

        listSelectionView.setSourceItems(availableRetailStores);
        listSelectionView.setTargetItems(selectedRetailStores);

        setupSearchAvailable();
        setupSearchSelected();
    }
    
    private void loadData() {
        try {
            var responseGzip = this.skladRequest.sendGetRequest(this.URL);
            var response = this.skladRequest.unpackedGzip(responseGzip);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode node = objectMapper.readTree(response);

            retailStores = this.skladRequest.getRetailStoresFromSklad(node);
            availableRetailStores.setAll(retailStores);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // ======== SETTERS =============

    public void setTokenWithLoadData(String token) {
        if (token.isEmpty() || Objects.isNull(token))
            throw new IllegalArgumentException("Token cannot be empty or null.");

        this.skladRequest = new SkladRequest();
        this.skladRequest.setToken(token);
        loadData();
    }

    public void setToken(String token) {
        if (token.isEmpty() || Objects.isNull(token))
            throw new IllegalArgumentException("Token cannot be empty or null.");

        this.skladRequest = new SkladRequest();
        this.skladRequest.setToken(token);
    }

    public void setListSearchAvailable(ObservableList<RetailStore> availableRetailStores) {
        if (Objects.isNull(availableRetailStores))
            throw new IllegalArgumentException("Available Retail Stores cannot be empty or null.");

        this.availableRetailStores.clear();
        this.availableRetailStores.addAll(availableRetailStores);
    }

    public void setListSearchSelected(ObservableList<RetailStore> selectedRetailStores) {
        if (Objects.isNull(selectedRetailStores))
            throw new IllegalArgumentException("Selected Retail Stores cannot be empty or null.");
        
        this.selectedRetailStores.clear();
        this.selectedRetailStores.addAll(selectedRetailStores);
    }

    // ======== HANDLERS =============

    /**
     * <p>Обработчик кнопки "Выйти".</p>
     * <p>Загружает окно авторизации.</p>
     * 
     * @param event
     * @throws IOException
     */
    @FXML
    protected void exitButtonHandler(ActionEvent event) throws IOException {
        Stage currentStage = (Stage) exitButton.getScene().getWindow();
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
     * <p>Обработчик кнопки "?"</p>
     * 
     * @param event
     */
    @FXML
    protected void questionButtonHandler(ActionEvent event) {
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
     * <p>Обработка кнопки "Далее".</p>
     * <p>Загружается следующее окно "Группы товаров" и передается вся информация.</p>
     * 
     * @throws IOException
     */
    @FXML
    private void nextButtonHandler() throws IOException {
        try {
            if (selectedRetailStores.isEmpty()) {
                showEmptySelectedHandler();
            } else {
                Stage currentStage = (Stage) nextButton.getScene().getWindow();
                FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("product-folder.fxml"));
                Parent root = fxmlLoader.load();
                
                String token = skladRequest.getToken();
                ProductFolderController productFolderController = fxmlLoader.getController();
                productFolderController.setSelectedRetailStores(selectedRetailStores);
                productFolderController.setAvailableRetailStores(availableRetailStores);
                productFolderController.setToken(token);
    
                Stage newStage = new Stage();
                newStage.setTitle("Группы товаров");
                Scene scene = new Scene(root);
                scene.getStylesheets().add(HelloApplication.class.getResource("styles/styles.css").toExternalForm());
                newStage.setScene(scene);
                newStage.setResizable(false);
                currentStage.close();
                
                productFolderController.setStage(newStage);
                newStage.setX(currentStage.getX()); 
                newStage.setY(currentStage.getY());
                newStage.show();
            }
        } catch (Exception e) {
            ErrorLogger.logAndShowError(e);
        }
    }

    /**
     * <p>Производит поиск в списке доступных элементов.</p>
     */
    private void setupSearchAvailable() {
        listSearchAvailable.textProperty().addListener((obs, oldVal, newVal) -> {
            filterAvailableList(newVal);
        });
    }

    /**
     * <p>Производит поиск в списке выбранных элементов.</p>
     */
    private void setupSearchSelected() {
        listSearchSelected.textProperty().addListener((obs, oldVal, newVal) -> {
            filterSelectedList(newVal);
        });
    }

    /**
     * <p>Фильтрует список доступных элементов по подстроке.</p>
     * 
     * @param filter подстрока
     */
    private void filterAvailableList(String filter) {
        try {
            availableRetailStores.clear();
            String lowerFilter = filter == null ? "" : filter.toLowerCase();
            List<RetailStore> filtered = retailStores.stream()
                .filter(store -> store.getName().toLowerCase().contains(lowerFilter))
                .filter(store -> !selectedRetailStores.contains(store))
                .collect(Collectors.toList());
    
            availableRetailStores.addAll(filtered);
        } catch (Exception e) {
            ErrorLogger.logAndShowError(e);
        }
    }

    /**
     * <p>Фильтрует список выбранных элементов по подстроке.</p>
     * 
     * @param filter подстрока
     */
    private void filterSelectedList(String filter) {
        try {
            selectedRetailStores.clear();
            String lowerFilter = filter == null ? "" : filter.toLowerCase();
            List<RetailStore> filtered = retailStores.stream()
                .filter(store -> store.getName().toLowerCase().contains(lowerFilter))
                .filter(store -> !availableRetailStores.contains(store))
                .collect(Collectors.toList());
    
            selectedRetailStores.addAll(filtered);
        } catch (Exception e) {
            ErrorLogger.logAndShowError(e);
        }
    }

    /**
     * <p>Показывает отсутсвие выбранных точек продаж.</p>
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
