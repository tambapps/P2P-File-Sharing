package com.tambapps.p2p.fandem.fandemdesktop.controller;

import com.tambapps.p2p.fandem.Peer;
import com.tambapps.p2p.fandem.fandemdesktop.model.SharingTask;
import com.tambapps.p2p.fandem.fandemdesktop.util.NodeUtils;
import com.tambapps.p2p.fandem.fandemdesktop.util.PropertyUtils;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.UnknownHostException;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
public class ReceivePaneController {

  private final Stage stage;
  private final Supplier<Boolean> canAddTaskSupplier;
  private final BiConsumer<File, Peer> receiveTaskLauncher;

  @FXML
  private Label pathLabel;
  @FXML
  private TextField ipField0;
  @FXML
  private TextField ipField1;
  @FXML
  private TextField ipField2;
  @FXML
  private TextField ipField3;
  @FXML
  private TextField portField;

  private List<TextField> ipFields;
  private ObjectProperty<File> folderProperty = new SimpleObjectProperty<>();

  public ReceivePaneController(Supplier<Stage> stageSupplier,
                               Supplier<Boolean> canAddTaskSupplier,
                               BiConsumer<File, Peer> receiveTaskLauncher) {
    this.stage = stageSupplier.get();
    this.canAddTaskSupplier = canAddTaskSupplier;
    this.receiveTaskLauncher = receiveTaskLauncher;
  }

  @FXML
  private void initialize() {
    ipFields = List.of(ipField0, ipField1, ipField2, ipField3);
    ipFields.forEach(NodeUtils::numberTextField);
    NodeUtils.numberTextField(portField);
    PropertyUtils
      .bindMapNullableToStringProperty(folderProperty, File::getPath, pathLabel.textProperty());
  }

  @FXML
  private void pickFolder() {
    DirectoryChooser fileChooser = new DirectoryChooser();
    File file = fileChooser.showDialog(stage);
    if (file == null) {
      return;
    }
    folderProperty.set(file);
  }

  @FXML
  private void receiveFile() {
    File file = folderProperty.get();
    if (ipFields.stream().anyMatch(ipField -> ipField.textProperty().get().isEmpty())) {
      Alert alert =
        new Alert(Alert.AlertType.INFORMATION, "You must provide the sender's IP", ButtonType.OK);
      alert.show();
      return;
    }
    if (portField.textProperty().get().isEmpty()) {
      Alert alert = new Alert(Alert.AlertType.INFORMATION, "You must provide the sender's port",
        ButtonType.OK);
      alert.show();
      return;
    }
    if (file == null) {
      Alert alert = new Alert(Alert.AlertType.INFORMATION, "You haven't picked a directory yet",
        ButtonType.OK);
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
    try {
      Peer peer = Peer.of(getAddress(), Integer.parseInt(portField.textProperty().get()));
      receiveTaskLauncher.accept(file, peer);
      folderProperty.set(null);
      ipFields.forEach(field -> field.setText(""));
      portField.setText("");
    } catch (UnknownHostException e) {
      Alert alert = new Alert(Alert.AlertType.ERROR, "Couldn't find the host", ButtonType.OK);
      alert.show();
    }
  }

  private String getAddress() {
    return ipFields.stream()
      .map(field -> field.textProperty().get())
      .collect(Collectors.joining("."));
  }

}
