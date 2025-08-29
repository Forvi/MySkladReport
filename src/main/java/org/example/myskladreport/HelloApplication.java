package org.example.myskladreport;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloApplication extends Application {

    private static final Logger logger = LoggerFactory.getLogger(HelloApplication.class);

    @Override
    public void init() throws Exception {
        super.init();
        logger.info("Application initializing");
    }

    @Override
    public void start(Stage stage) {
        try {
            logger.info("Start method called, loading UI");

            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-password.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1024, 640);
            scene.getStylesheets().add(HelloApplication.class.getResource("styles/styles.css").toExternalForm());

            stage.setResizable(false);
            stage.setTitle("MySklad Report App");
            stage.setScene(scene);

            stage.show();
            stage.centerOnScreen();

            logger.info("Stage shown successfully");

        } catch (Exception e) {
            logger.error("Failed to start JavaFX application", e);
            System.exit(1);
        }
    }

    @Override
    public void stop() throws Exception {
        logger.info("Application stopping");
        super.stop();
    }

    public static void main(String[] args) {
        logger.info("Launching application");
        launch(args);
    }
}
