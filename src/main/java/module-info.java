module org.example.myskladreport {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    

    opens org.example.myskladreport to javafx.fxml;
    exports org.example.myskladreport;
}