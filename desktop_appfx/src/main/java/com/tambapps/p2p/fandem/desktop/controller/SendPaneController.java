package com.tambapps.p2p.fandem.desktop.controller;

import com.tambapps.p2p.fandem.desktop.App;
import com.tambapps.p2p.fandem.desktop.utils.PropertyUtils;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;

import static com.tambapps.p2p.fandem.desktop.App.*;

public class SendPaneController {

  @FXML
  private Label pathLabel;

  private ObjectProperty<File> fileProperty = new SimpleObjectProperty<>();

  @FXML
  private void initialize() {
    pathLabel.textProperty().bind(PropertyUtils.nullableToStringProperty(fileProperty, File::getPath));
  }

  @FXML
  private void pickFile() {
    FileChooser fileChooser = new FileChooser();
    File file = fileChooser.showOpenDialog(App.getStage());
    if (file == null) {
      return;
    }
    fileProperty.set(file);
  }

  @FXML
  private void sendFile() {
    File file = fileProperty.get();
    if (file == null) {
      if (sharingTasks.size() >= MAX_SHARING_TASKS) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "You haven't picked any file yet", ButtonType.OK);
        alert.show();
        return;
      }
    }
    if (sharingTasks.size() >= MAX_SHARING_TASKS) {
      Alert alert = new Alert(Alert.AlertType.INFORMATION, "Maximum tasks number reached. Wait until one task is over to start another.", ButtonType.OK);
      alert.show();
      return;
    }
    try {
      sharingTasks.add(sharingService.sendFile(file));
      fileProperty.set(null);
    } catch (IOException e) {
      Alert alert = new Alert(Alert.AlertType.ERROR, "Couldn't retrieve IP. Are you connected to internet?", ButtonType.OK);
      alert.show();
    }
  }
}
