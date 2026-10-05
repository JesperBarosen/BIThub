module bithub.ui {
  requires bithub.core;
  requires bithub.file;
  requires javafx.controls;
  requires javafx.fxml;
  requires javafx.graphics;

  opens ui to javafx.graphics, javafx.fxml;
}
