package com.tambapps.p2p.fandem.desktop.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
public class SendPaneController {

  private final Supplier<List<File>> fileChooser;
  private final Supplier<Boolean> canAddTaskSupplier;
  private final Consumer<List<File>> sendTaskLauncher;
  @FXML
  private Pane sendPane;
  @FXML
  private Button pickFileButton;
  @FXML
  private Label dragDropLabel;

  public SendPaneController(@Qualifier("fileChooser") Supplier<List<File>> fileChooser,
                            Supplier<Boolean> canAddTaskSupplier,
                            Consumer<List<File>> sendTaskLauncher) {
    this.fileChooser = fileChooser;
    this.canAddTaskSupplier = canAddTaskSupplier;
    this.sendTaskLauncher = sendTaskLauncher;
  }

  @FXML
  private void initialize() {
    sendPane.setOnDragOver(event -> {
      sendPane.setStyle("-fx-background-color: rgba(0,0,0,0.25);");
      pickFileButton.setText("Drop file(s)");
      dragDropLabel.setVisible(false);
      if (event.getGestureSource() != sendPane
          && event.getDragboard().hasFiles()) {
        /* allow for both copying and moving, whatever user chooses */
        event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
      }
      event.consume();
    });
    sendPane.setOnDragExited(event -> {
      onDragDropEnd();
      event.consume();
    });
    sendPane.setOnDragDropped(event -> {
      onDragDropEnd();
      Dragboard db = event.getDragboard();
      boolean success = false;
      if (db.hasFiles()) {
        success = true;
      }
      /* let the source know whether the string was successfully
       * transferred and used */
      event.setDropCompleted(success);
      event.consume();
      List<File> files = db.getFiles().stream()
          .filter(File::isFile)
          .toList();
      sendFile(files);
    });
  }

  @FXML
  private void pickFile() {
    List<File> files = fileChooser.get();
    if (files == null || files.isEmpty()) {
      return;
    }
    sendFile(files);
  }

  private boolean sendFile(List<File> file) {
    if (!canAddTaskSupplier.get()) {
      Alert alert = new Alert(Alert.AlertType.INFORMATION,
          "Maximum tasks number reached. Wait until one task is over to start another.",
          ButtonType.OK);
      alert.show();
      return false;
    }
    sendTaskLauncher.accept(file);
    return true;
  }

  private void onDragDropEnd() {
    sendPane.setStyle("-fx-background-color: #00000000;");
    pickFileButton.setText("Pick file");
    dragDropLabel.setVisible(true);
  }

}
