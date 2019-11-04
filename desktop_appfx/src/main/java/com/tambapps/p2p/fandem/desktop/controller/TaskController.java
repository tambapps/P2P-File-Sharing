package com.tambapps.p2p.fandem.desktop.controller;

import com.tambapps.p2p.fandem.Peer;
import com.tambapps.p2p.fandem.desktop.App;
import com.tambapps.p2p.fandem.desktop.model.SharingTask;
import com.tambapps.p2p.fandem.listener.SharingErrorListener;
import com.tambapps.p2p.fandem.listener.TransferListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.io.File;
import java.io.IOException;

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
      this.task = App.sharingService.sendFile(file, this);
      centerLabel.setText("Waiting for other peer on " + task.peer + " ...");
    } catch (IOException e) {
      setError(e.getMessage());
    }
  }

  public void initReceivingTask(File file, Peer peer) {
    this.task = App.sharingService.receiveFile(file, peer, this);
    centerLabel.setText("Connecting to " + task.remotePeer + " ...");
  }

  @FXML
  private void initialize() {
    progressBar.setVisible(false);
    leftLabel.setVisible(false);
    removeButton.setVisible(false);
  }

  private void setError(String error) {
    leftLabel.setVisible(false);
    progressBar.setVisible(false);
    cancelButton.setVisible(false);
    removeButton.setVisible(true);
    centerLabel.setVisible(true);
    centerLabel.setText(error);
  }

  @FXML
  private void cancel() {

  }

  @FXML
  private void remove() {
    App.sharingTasks.remove(task);
  }

  @Override
  public void onError(IOException e) {

  }

  @Override
  public void onConnected(Peer selfPeer, Peer remotePeer, String fileName, long fileSize) {

  }

  @Override
  public void onProgressUpdate(int progress, long byteProcessed, long totalBytes) {

  }
}
