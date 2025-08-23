package org.example.myskladreport.utils;

import java.io.File;
import java.util.Objects;

import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class FolderChooser {

    public static String choose(Stage stage, String title) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        
        if (Objects.isNull(title)) 
            title = "Выберите папку";

        directoryChooser.setTitle(title);
        File selectedDirectory = directoryChooser.showDialog(stage);

        if (Objects.nonNull(selectedDirectory)) {
            return selectedDirectory.getAbsolutePath();
        } else {
            return null;
        }
    }

    public void writeReport() {
        
    }

}
