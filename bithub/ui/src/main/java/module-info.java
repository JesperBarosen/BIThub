module bithub.ui {
  requires bithub.core;
  requires bithub.file;
  requires java.net.http;
  requires com.fasterxml.jackson.databind;
  requires javafx.controls;
  requires javafx.fxml;
  requires javafx.graphics;

  opens ui to javafx.graphics, javafx.fxml;
}
