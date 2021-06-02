package com.tambapps.p2p.fandem.desktop.controller;

import com.tambapps.p2p.fandem.desktop.util.PropertyUtils;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Component
public class SendPaneController {

  private final Supplier<File> fileChooser;
  private final Supplier<Boolean> canAddTaskSupplier;
  private final Consumer<File> sendTaskLauncher;

  @FXML
  private Label pathLabel;
  private ObjectProperty<File> fileProperty = new SimpleObjectProperty<>();

  public SendPaneController(@Qualifier("fileChooser") Supplier<File> fileChooser,
                            Supplier<Boolean> canAddTaskSupplier,
                            Consumer<File> sendTaskLauncher) {
    this.fileChooser = fileChooser;
    this.canAddTaskSupplier = canAddTaskSupplier;
    this.sendTaskLauncher = sendTaskLauncher;
  }

  @FXML
  private void initialize() {
    PropertyUtils
      .bindMapNullableToStringProperty(fileProperty, File::getPath, pathLabel.textProperty());
  }


  @FXML
  private void pickFile() {
    File file = fileChooser.get();
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
    if (!canAddTaskSupplier.get()) {
      Alert alert = new Alert(Alert.AlertType.INFORMATION,
        "Maximum tasks number reached. Wait until one task is over to start another.",
        ButtonType.OK);
      alert.show();
      return;
    }
    sendTaskLauncher.accept(file);
    pathLabel.setText(null);
  }
}
