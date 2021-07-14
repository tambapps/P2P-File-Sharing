package com.tambapps.p2p.fandem.desktop.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
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

  public SendPaneController(@Qualifier("fileChooser") Supplier<File> fileChooser,
                            Supplier<Boolean> canAddTaskSupplier,
                            Consumer<File> sendTaskLauncher) {
    this.fileChooser = fileChooser;
    this.canAddTaskSupplier = canAddTaskSupplier;
    this.sendTaskLauncher = sendTaskLauncher;
  }

  @FXML
  private void pickFile() {
    File file = fileChooser.get();
    if (file == null) {
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
  }
}
