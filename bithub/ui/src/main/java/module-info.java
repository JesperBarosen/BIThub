module bithub.ui {
    requires bithub.core;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires org.controlsfx.controls;

    exports ui.client;

    opens ui to javafx.graphics, javafx.fxml;
    opens ui.controller to javafx.fxml;
}
