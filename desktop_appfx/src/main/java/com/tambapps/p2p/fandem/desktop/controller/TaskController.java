package com.tambapps.p2p.fandem.desktop.controller;

import com.tambapps.p2p.fandem.Peer;
import com.tambapps.p2p.fandem.desktop.App;
import com.tambapps.p2p.fandem.desktop.model.SharingTask;
import com.tambapps.p2p.fandem.listener.SharingErrorListener;
import com.tambapps.p2p.fandem.listener.TransferListener;

import com.tambapps.p2p.fandem.util.IPUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;

public class TaskController implements TransferListener, SharingErrorListener {

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

  public void initSendingTask(File file) {
    try {
      Peer peer = IPUtils.getAvailablePeer();
      this.task = App.sharingService.sendFile(peer, file, this);
      centerLabel.setText("Waiting for other peer on " + peer + " ...");
    } catch (SocketException e) {
      setEndMessage("Couldn't retrieve IP. Are you connected to internet?");
    }
  }

  public void initReceivingTask(File file, Peer peer) {
    this.task = App.sharingService.receiveFile(file, peer, this);
    centerLabel.setText("Connecting to " + peer + " ...");
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
    App.sharingTasks.remove(task);
  }

  @Override
  public void onError(IOException e) {
    setEndMessage("An error occurred: " + e.getMessage());
  }

  @Override
  public void onConnected(Peer selfPeer, Peer remotePeer, String fileName, long fileSize) {
    String text;
    if (task.sender) {
      text = String.format("Sending %s to peer %s", fileName, remotePeer);
    } else {
      text = String.format("Receiving %s from peer %s", fileName, remotePeer);
    }
    Platform.runLater(() -> {
      leftLabel.setVisible(true);
      leftLabel.setText(text);
    });
  }

  @Override
  public void onProgressUpdate(int progress, long byteProcessed, long totalBytes) {
    double newProgress = ((double)byteProcessed) / ((double)totalBytes);
    if (newProgress > progressBar.getProgress() + 0.5) {
      Platform.runLater(() -> progressBar.setProgress(newProgress));
    }
    if (progress == 100) { // transfer finished
      Platform.runLater(() -> {
        String text;
        if (task.sender) {
          text = String.format("%s was successfully sent ", task.file);
        } else {
          text = String.format("%s was received successfully in %s", task.file.getName(),
              task.file.getParent());
        }
        setEndMessage(text);
      });
    }
  }
}
