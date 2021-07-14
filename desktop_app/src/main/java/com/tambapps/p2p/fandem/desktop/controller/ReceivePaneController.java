package com.tambapps.p2p.fandem.desktop.controller;

import com.tambapps.p2p.fandem.Fandem;
import com.tambapps.p2p.fandem.desktop.service.PeerSniffingService;
import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.fandem.desktop.util.PropertyUtils;
import com.tambapps.p2p.speer.util.PeerUtils;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.InetAddress;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@Component
public class ReceivePaneController {

  private final Supplier<File> directoryChooser;
  private final Supplier<Boolean> canAddTaskSupplier;
  private final BiConsumer<File, Peer> receiveTaskLauncher;
  private final PeerSniffingService sniffingService;
  private final ObjectProperty<File> folderProperty;

  @FXML
  private Label pathLabel;
  @FXML
  private TextField hexCodeField;

  public ReceivePaneController(@Qualifier("directoryChooser") Supplier<File> directoryChooser,
      Supplier<Boolean> canAddTaskSupplier,
      BiConsumer<File, Peer> receiveTaskLauncher,
      PeerSniffingService sniffingService,
      ObjectProperty<File> folderProperty) {
    this.directoryChooser = directoryChooser;
    this.canAddTaskSupplier = canAddTaskSupplier;
    this.receiveTaskLauncher = receiveTaskLauncher;
    this.sniffingService = sniffingService;
    this.folderProperty = folderProperty;
  }

  @FXML
  private void initialize() {
    if (folderProperty.get() != null) {
      pathLabel.setText(folderProperty.get().getPath());
    }
    PropertyUtils
      .bindMapNullableToStringProperty(folderProperty, File::getPath, pathLabel.textProperty());
    sniffingService.start();

    // set key prefix if connected to a network
    String keyPrefix = getKeyPrefix();
    if (keyPrefix != null) {
      hexCodeField.setText(keyPrefix);
    }
  }

  @FXML
  private void pickFolder() {
    File file = directoryChooser.get();
    if (file == null) {
      return;
    }
    folderProperty.set(file);
  }

  private Peer verifiedPeer() throws IllegalArgumentException {
    String hexCode = hexCodeField.getText();
    if (hexCode == null || hexCode.isEmpty()) {
      throw new IllegalArgumentException("You must provide the hex code");
    }
    return Fandem.parsePeerFromHexString(hexCode);
  }

  @FXML
  private void receiveFile() {
    Peer peer;
    try {
      peer = verifiedPeer();
    } catch (IllegalArgumentException e) {
      Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
      alert.show();
      return;
    }
    File file = folderProperty.get();
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
    receiveTaskLauncher.accept(file, peer);
    folderProperty.set(null);
    hexCodeField.setText("");
  }

  // returns key prefix if connected
  private String getKeyPrefix() {
    InetAddress address = PeerUtils.getPrivateNetworkIpAddressOrNull();
    if (address == null) {
      return null;
    }
    String key = Fandem.toHexString(PeerUtils.getPrivateNetworkIpAddressOrNull());
    // subtract the last octet
    return key.substring(0, key.length() - 2);
  }
}
