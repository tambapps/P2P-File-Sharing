package com.tambapps.p2p.fandem.desktop.controller;

import static com.tambapps.p2p.fandem.desktop.App.MAX_SHARING_TASKS;
import static com.tambapps.p2p.fandem.desktop.App.sharingTasks;

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

public class SendPaneController extends SharingPaneController {

  @FXML
  private Label pathLabel;

  private ObjectProperty<File> fileProperty = new SimpleObjectProperty<>();

  @FXML
  private void initialize() {
    PropertyUtils
        .bindMapNullableToStringProperty(fileProperty, File::getPath, pathLabel.textProperty());
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
      Alert alert =
          new Alert(Alert.AlertType.INFORMATION, "You haven't picked any file yet", ButtonType.OK);
      alert.show();
      return;
    }
    if (sharingTasks.size() >= MAX_SHARING_TASKS) {
      Alert alert = new Alert(Alert.AlertType.INFORMATION,
          "Maximum tasks number reached. Wait until one task is over to start another.",
          ButtonType.OK);
      alert.show();
      return;
    }
    submitTaskView(controller -> controller.initSendingTask(file));
  }
}
