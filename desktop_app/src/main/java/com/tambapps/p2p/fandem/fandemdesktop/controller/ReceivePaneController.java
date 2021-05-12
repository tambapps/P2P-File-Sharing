package com.tambapps.p2p.fandem.fandemdesktop.controller;

import com.tambapps.p2p.fandem.Fandem;
import com.tambapps.p2p.fandem.SenderPeer;
import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.fandem.fandemdesktop.util.PropertyUtils;
import com.tambapps.p2p.speer.datagram.DatagramSupplier;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@Component
public class ReceivePaneController {

  private final Supplier<File> directoryChooser;
  private final ExecutorService executorService;
  private final Supplier<Boolean> canAddTaskSupplier;
  private final BiConsumer<File, Peer> receiveTaskLauncher;

  @FXML
  private Label pathLabel;
  @FXML
  private TextField hexCodeField;
  @FXML
  private Button searchPeerButton;
  @FXML
  private ProgressBar progressBar;
  @FXML
  private Label sniffText;
  @FXML
  private Button cancelSniffButton;

  DatagramSupplier<List<SenderPeer>> sniffSupplier;
  private final ObjectProperty<File> folderProperty = new SimpleObjectProperty<>();

  public ReceivePaneController(@Qualifier("directoryChooser") Supplier<File> directoryChooser,
                               @Qualifier("sniffExecutorService") ExecutorService executorService,
                               Supplier<Boolean> canAddTaskSupplier,
                               BiConsumer<File, Peer> receiveTaskLauncher) {
    this.directoryChooser = directoryChooser;
    this.executorService = executorService;
    this.canAddTaskSupplier = canAddTaskSupplier;
    this.receiveTaskLauncher = receiveTaskLauncher;
  }

  @FXML
  private void initialize() {
    PropertyUtils
      .bindMapNullableToStringProperty(folderProperty, File::getPath, pathLabel.textProperty());
    progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
    progressBar.setVisible(false);
    sniffText.setVisible(false);
    cancelSniffButton.setVisible(false);
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
  private void searchSender() {
    searchPeerButton.setVisible(false);
    progressBar.setVisible(true);
    sniffText.setVisible(true);
    cancelSniffButton.setVisible(true);
    executorService.submit(this::sniffSenderPeer);

  }

  private void sniffSenderPeer() {
    if (sniffSupplier == null) {
      try {
        sniffSupplier = Fandem.senderPeersSupplier();
      } catch (IOException e) {
        errorDialog(e);
        return;
      }
    }

    List<SenderPeer> senderPeers;
    try {
      senderPeers = sniffSupplier.get();
      if (senderPeers.isEmpty()) {
        return;
      }
      Platform.runLater(() -> {
        int i = 0;
        while (i < senderPeers.size() && !proposePeer(senderPeers.get(i))) {
          i++;
        }
      });
    }  catch (IOException e) {
      if (!(e instanceof SocketException)) {
        Platform.runLater(() -> errorDialog(e));
      }
    }
  }

  private boolean proposePeer(SenderPeer sniffedPeer) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
      sniffedPeer.getDeviceName() + " wants to send " + sniffedPeer.getFileName(),
      new ButtonType("Choose this Peer", ButtonBar.ButtonData.YES),
      new ButtonType("Continue research", ButtonBar.ButtonData.NO));
    alert.setTitle("Sender found");
    alert.setHeaderText(String.format("Sender: %s\nPeer key: %s",
      sniffedPeer.getDeviceName(), Fandem.toHexString(sniffedPeer)));

    Optional<ButtonBar.ButtonData> optButton = alert.showAndWait().map(ButtonType::getButtonData);
    switch (optButton.orElse(ButtonBar.ButtonData.OTHER)) {
      case YES:
        cancelSniff();
        hexCodeField.setText(Fandem.toHexString(sniffedPeer));
        searchPeerButton.setDisable(true);
        return true;
      case NO:
        executorService.submit(this::sniffSenderPeer);
        break;
    }
    return false;
  }

  @FXML
  private void cancelSniff() {
    sniffSupplier.close();
    progressBar.setVisible(false);
    sniffText.setVisible(false);
    cancelSniffButton.setVisible(false);
    searchPeerButton.setVisible(true);
  }

  private void errorDialog(IOException e) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("An error occurred");
    alert.setHeaderText("an error occurred while searching for sender");
    alert.setContentText(e.getMessage());
    alert.showAndWait();
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
    searchPeerButton.setDisable(false);
  }

}
