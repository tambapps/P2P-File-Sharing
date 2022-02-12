package com.tambapps.p2p.fandem.desktop.service;

import com.tambapps.p2p.fandem.Fandem;
import com.tambapps.p2p.fandem.SenderPeer;
import com.tambapps.p2p.fandem.util.FileUtils;
import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.speer.datagram.service.MulticastReceiverService;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Service
public class PeerSniffingService implements MulticastReceiverService.DiscoveryListener<List<SenderPeer>> {

  private final BiConsumer<File, Peer> receiveTaskLauncher;
  private final MulticastReceiverService<List<SenderPeer>> receiverService;
  private final ObjectProperty<File> folderProperty;
  private final AtomicBoolean isProposingPeer = new AtomicBoolean(false);
  private final Set<SenderPeer> blacklist = new HashSet<>();

  public PeerSniffingService(BiConsumer<File, Peer> receiveTaskLauncher,
      MulticastReceiverService<List<SenderPeer>> receiverService,
      ObjectProperty<File> folderProperty) {
    this.receiveTaskLauncher = receiveTaskLauncher;
    this.receiverService = receiverService;
    this.folderProperty = folderProperty;
  }

  public void start() {
    receiverService.setListener(this);
    try {
      receiverService.start();
    } catch (IOException e) {
      onError(e);
    }
  }

  public void stop() {
    receiverService.stop();
  }

  private boolean proposePeer(SenderPeer senderPeer) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
        String.format("%s wants to send files\n%s", senderPeer.getDeviceName(),
            senderPeer.getFiles()
                .stream()
                .map(f -> String.format("%s (%s)", f.getFileName(), FileUtils.toFileSize(f.getFileSize())))
                .collect(Collectors.joining("- ", "- ", ""))),
        new ButtonType("Receive file", ButtonBar.ButtonData.YES),
        new ButtonType("Ignore", ButtonBar.ButtonData.NO));
    alert.setTitle("Sender found");
    alert.setHeaderText(String.format("Sender: %s\nPeer key: %s",
        senderPeer.getDeviceName(), Fandem.toHexString(senderPeer)));

    // so that we don't propose it more than once
    blacklist.add(senderPeer);
    Optional<ButtonBar.ButtonData> optButton = alert.showAndWait().map(ButtonType::getButtonData);
    switch (optButton.orElse(ButtonBar.ButtonData.OTHER)) {
      case YES:
        return true;
      case NO:
        break;
    }
    return false;
  }

  private void errorDialog(IOException e) {
    Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(),
        new ButtonType("Retry", ButtonBar.ButtonData.YES),
        new ButtonType("Ok", ButtonBar.ButtonData.NO));
    alert.setTitle("Couldn't search for senders");
    alert.setHeaderText("An error occurred while searching for senders");
    Optional<ButtonBar.ButtonData> optButton = alert.showAndWait().map(ButtonType::getButtonData);
    switch (optButton.orElse(ButtonBar.ButtonData.OTHER)) {
      case YES:
        stop();
        start();
      case NO:
        break;
    }
  }

  @Override
  public void onDiscovery(List<SenderPeer> senderPeers) {
    if (folderProperty.get() == null || isProposingPeer.get()) {
      // user hasn't picked a directory, let's not propose the peer
      // or we're already proposing a peer to the user
      return;
    }
    isProposingPeer.set(true);
    // need to be on UI thread
    Platform.runLater(() -> {
      for (SenderPeer senderPeer : senderPeers) {
        if (!blacklist.contains(senderPeer) && proposePeer(senderPeer)) {
          receiveTaskLauncher.accept(folderProperty.get(), senderPeer);
          break;
        }
      }
      isProposingPeer.set(false);
    });
  }

  @Override
  public void onError(IOException e) {
    Platform.runLater(() -> errorDialog(e));
  }

}
