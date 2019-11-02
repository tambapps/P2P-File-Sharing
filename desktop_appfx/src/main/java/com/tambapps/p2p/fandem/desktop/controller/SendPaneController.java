package com.tambapps.p2p.fandem.desktop.controller;

import com.tambapps.p2p.fandem.desktop.style.Colors;
import com.tambapps.p2p.fandem.desktop.utils.PropertyUtils;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class SendPaneController {

  private Stage stage;

  @FXML
  private Label pathLabel;
  @FXML
  private Pane root;

  private ObjectProperty<File> fileProperty = new SimpleObjectProperty<>();

  @FXML
  private void initialize() {
    root.setStyle(String.format("-fx-background-color: linear-gradient(to top, %s, %s)",
      Colors.GRADIENT_BOTTOM, Colors.GRADIENT_TOP));
    pathLabel.textProperty().bind(PropertyUtils.mapProperty(fileProperty, (f) -> f == null ? "" : f.getPath()));
  }

  public void setStage(Stage stage) {
    this.stage = stage;
  }

  @FXML
  private void pickFile() {
    FileChooser fileChooser = new FileChooser();
    File file = fileChooser.showOpenDialog(stage);
    if (file == null) {
      return;
    }
    fileProperty.set(file);
  }
}
