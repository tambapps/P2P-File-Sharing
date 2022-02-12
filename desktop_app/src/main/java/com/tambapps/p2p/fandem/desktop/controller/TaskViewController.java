package com.tambapps.p2p.fandem.desktop.controller;

import com.tambapps.p2p.fandem.Fandem;
import com.tambapps.p2p.fandem.desktop.service.FileSenderService;
import com.tambapps.p2p.fandem.desktop.util.SharingErrorListener;
import com.tambapps.p2p.fandem.util.TransferListener;
import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.fandem.desktop.model.SharingTask;
import com.tambapps.p2p.fandem.desktop.service.FileReceiverService;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope("prototype") /// allow to have a new instance each time we need this component
public class TaskViewController implements TransferListener, SharingErrorListener {

  private final FileSenderService fileSenderService;
  private final FileReceiverService fileReceiverService;
  private final ObservableList<SharingTask> sharingTasks;

  @FXML
  private ProgressBar progressBar;
  @FXML
  private Label centerLabel;
  @FXML
  private Label leftLabel;
  @FXML
  private Button cancelButton;
  @FXML
  private Button removeButton;

  private SharingTask task;

  public TaskViewController(
      FileSenderService fileSenderService,
      FileReceiverService fileReceiverService, ObservableList<SharingTask> sharingTasks) {
    this.fileSenderService = fileSenderService;
    this.fileReceiverService = fileReceiverService;
    this.sharingTasks = sharingTasks;
  }


  @FXML
  private void initialize() {
    progressBar.setVisible(false);
    leftLabel.setVisible(false);
    removeButton.setVisible(false);
  }

  private void setEndMessage(String message) {
    Platform.runLater(() -> {
      leftLabel.setVisible(false);
      progressBar.setVisible(false);
      cancelButton.setVisible(false);
      removeButton.setVisible(true);
      centerLabel.setVisible(true);
      centerLabel.setText(message);
    });
  }

  @FXML
  private void cancel() {
    task.cancel();
    setEndMessage("Task canceled");
  }

  @FXML
  private void remove() {
    sharingTasks.remove(task);
  }

  @Override
  public void onError(IOException e) {
    setEndMessage("An error occurred: " + e.getMessage());
  }

  @Override
  public void onConnected(Peer selfPeer, Peer remotePeer) {
    String text;
    if (task.sender) {
      text = String.format("Sending to peer %s", remotePeer);
    } else {
      text = String.format("Receiving from peer %s", remotePeer);
    }
    Platform.runLater(() -> {
      centerLabel.setText("");
      centerLabel.setVisible(false);
      progressBar.setVisible(true);
      leftLabel.setVisible(true);
      leftLabel.setText(text);
    });
  }

  @Override
  public void onTransferStarted(String fileName, long fileSize) {
    String text;
    fileName = formattedFileName(fileName, 10);
    if (task.sender) {
      text = String.format("Sending file %s", fileName);
    } else {
      text = String.format("Receiving file %s", fileName);
    }
    Platform.runLater(() -> leftLabel.setText(text));
  }

  @Override
  public void onProgressUpdate(String fileName, int progress, long byteProcessed,
      long totalBytes) {
    double newProgress = ((double)byteProcessed) / ((double)totalBytes);
    if (newProgress > progressBar.getProgress() + 0.025) {
      Platform.runLater(() -> progressBar.setProgress(newProgress));
    }
  }

  public void onEnd() {
    Platform.runLater(() -> {
      String text;
      String fileNames = task.files.stream()
          .map(file -> formattedFileName(file.getName(), 30))
          .collect(Collectors.joining(", "));
      if (task.sender) {
        text = String.format("%s was successfully sent ", fileNames);
      } else {
        text = String.format("%s was received successfully in %s", fileNames,
            task.files.get(0).getParentFile().getName());
      }
      setEndMessage(text);
    });
  }

  private String formattedFileName(String name, int maxLength) {
    if (name.length() > maxLength) {
      return name.substring(0, maxLength) + "...";
    }
    return name;
  }
  public void sendTask(List<File> files) {
    Peer peer;
    try {
      peer = Fandem.findAvailableSendingPeer();
    } catch (IOException e) {
      setEndMessage("Couldn't retrieve IP. Are you connected to internet?");
      return;
    }
    task = fileSenderService.sendFile(peer, files, this);
    sharingTasks.add(task);
    centerLabel.setText(String.format("Waiting for a connection (peer key: %s, file: %s)",
        Fandem.toHexString(peer),
        files.stream()
            .map(File::getName)
            .collect(Collectors.joining(", "))));
  }

  public void receiveTask(File folder, Peer peer) {
    task = fileReceiverService.receiveFile(folder, peer, this);
    sharingTasks.add(task);
    centerLabel.setText("Connecting to " + Fandem.toHexString(peer) + " ...");
  }

}
