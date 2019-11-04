package com.tambapps.p2p.fandem.desktop.controller;

import com.tambapps.p2p.fandem.Peer;
import com.tambapps.p2p.fandem.desktop.App;
import com.tambapps.p2p.fandem.desktop.model.SharingTask;
import com.tambapps.p2p.fandem.listener.SharingErrorListener;
import com.tambapps.p2p.fandem.listener.TransferListener;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

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
    } catch (IOException e) {
      //TODO handle me better
    }
    String text;
    if (task.sender) {
      text = "Waiting for other peer on " + task.peer + " ...";
    } else {
      text = "Connecting to " + task.remotePeer + " ...";
    }
    centerLabel.setText(text);
  }

  @FXML
  private void initialize() {
    progressBar.setVisible(false);
    leftLabel.setVisible(false);
    removeButton.setVisible(false);
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
