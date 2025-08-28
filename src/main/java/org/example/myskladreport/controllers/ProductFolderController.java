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
import javafx.stage.Modality;
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
import org.example.myskladreport.models.ProductFolder;
import org.example.myskladreport.models.RetailStore;
import org.example.myskladreport.utils.FolderChooser;
import org.example.myskladreport.utils.ReportWriter;
import org.example.myskladreport.utils.SkladRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ProductFolderController implements Initializable {

    @FXML
    private Button exitButton;

    @FXML
    private TextField listSearchAvailable;

    @FXML
    private TextField listSearchSelected;

    @FXML
    private Button nextButton;

    @FXML
    private ListSelectionView<ProductFolder> listSelectionView;

    @FXML
    private Button questionButton;

    @FXML
    private Button selectButton;

    @FXML
    private Button backButton;

    List<ProductFolder> productFolders;

    private Stage stage;

    private final ObservableList<ProductFolder> availableProductFolders = FXCollections.observableArrayList();

    private final ObservableList<ProductFolder> selectedProductFolders = FXCollections.observableArrayList();

    private ObservableList<RetailStore> retailStoresSelected;

    private ObservableList<RetailStore> retailStoresAll;

    private SkladRequest skladRequest;

    private final String URL = "https://api.moysklad.ru/api/remap/1.2/entity/productfolder/";

    // ======== INIT =============
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        listSelectionView.setSourceHeader(new Label(""));
        listSelectionView.setTargetHeader(new Label(""));

        listSelectionView.setSourceItems(availableProductFolders);
        listSelectionView.setTargetItems(selectedProductFolders);

        setupSearchAvailable();
        setupSearchSelected();
    }
    
    private void loadData() {
        try {
            var responseGzip = this.skladRequest.sendGetRequest(URL);
            var response = this.skladRequest.unpackedGzip(responseGzip);
            
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode node = objectMapper.readTree(response);
            
            productFolders = this.skladRequest.getProductFoldersFromSklad(node);
            availableProductFolders.setAll(productFolders);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // ======== SETTERS =============

    public void setSelectedRetailStores(ObservableList<RetailStore> retailStoresSelected) {
        if (retailStoresSelected.isEmpty() || Objects.isNull(retailStoresSelected))
            throw new IllegalArgumentException("Retail Stores cannot be empty or null.");

        this.retailStoresSelected = retailStoresSelected;
    }

    public void setAvailableRetailStores(ObservableList<RetailStore> retailStoresAvailable) {
        if (retailStoresAvailable.isEmpty() || Objects.isNull(retailStoresAvailable))
            throw new IllegalArgumentException("Retail Stores cannot be empty or null.");

        this.retailStoresAll = retailStoresAvailable;
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
        newStage.centerOnScreen();
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

    @FXML
    protected void nextButtonHandler() {
        try {
            String path = FolderChooser.choose(stage, "Выберите папку");
            ReportWriter.write(this.retailStoresSelected, selectedProductFolders, skladRequest, path);
            successfulSaveModal(stage);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        
    }

    @FXML
    private void backButtonHandler() throws IOException {
        Stage stage = (Stage) backButton.getScene().getWindow();

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("retail-store.fxml"));
        Parent root = fxmlLoader.load();
        RetailStoreController retailStoreController = fxmlLoader.getController();
        retailStoreController.setListSearchSelected(retailStoresSelected);
        retailStoreController.setListSearchAvailable(retailStoresAll);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(HelloApplication.class.getResource("styles/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Точки продаж");
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
        availableProductFolders.clear();
        String lowerFilter = filter == null ? "" : filter.toLowerCase();
        List<ProductFolder> filtered = productFolders.stream()
            .filter(store -> store.getName().toLowerCase().contains(lowerFilter))
            .filter(store -> !selectedProductFolders.contains(store))
            .collect(Collectors.toList());

        availableProductFolders.addAll(filtered);
    }

    /**
     * <p>Фильтрует список выбранных элементов по подстроке.</p>
     * 
     * @param filter подстрока
     */
    private void filterSelectedList(String filter) {
        selectedProductFolders.clear();
        String lowerFilter = filter == null ? "" : filter.toLowerCase();
        List<ProductFolder> filtered = productFolders.stream()
            .filter(store -> store.getName().toLowerCase().contains(lowerFilter))
            .filter(store -> !availableProductFolders.contains(store))
            .collect(Collectors.toList());

        selectedProductFolders.addAll(filtered);
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

    public void successfulSaveModal(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("successfulSaveModal.fxml"));
            Parent root = fxmlLoader.load();
            Stage modalStage = new Stage();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(HelloApplication.class.getResource("styles/styles.css").toExternalForm());
            modalStage.initOwner(stage);
            modalStage.initModality(Modality.WINDOW_MODAL);
            modalStage.setScene(scene);
            modalStage.setResizable(false);
            modalStage.showAndWait(); 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
