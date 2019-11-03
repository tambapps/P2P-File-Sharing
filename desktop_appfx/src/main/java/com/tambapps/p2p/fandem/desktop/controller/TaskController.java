package com.tambapps.p2p.fandem.desktop.controller;

import com.tambapps.p2p.fandem.Peer;
import com.tambapps.p2p.fandem.desktop.App;
import com.tambapps.p2p.fandem.desktop.model.SharingTask;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.util.stream.Stream;

public class TaskController {

  @FXML
  private ProgressBar progressBar;
  @FXML
  private Label centerLabel;
  @FXML
  private Label leftLabel;
  @FXML
  private Button cancelButton;

  private SharingTask task;

  public void setTask(SharingTask sharingTask) {
    this.task = sharingTask;
    String text;
    if (task.sender) {
      text = "Waiting for other peer on " + task.peer.get() + " ...";
    } else {
      text = "Connecting to " + task.remotePeer.get() + " ...";
    }
    centerLabel.textProperty().set(text);
    sharingTask.remotePeer.addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        centerLabel.setVisible(false);
        progressBar.setVisible(true);
        leftLabel.setVisible(true);
        leftLabel.setText((sharingTask.sender ? "Sending to " : "Receiving from ") + newValue);
      }
    });

  }

  @FXML
  private void initialize() {
    progressBar.setVisible(false);
    leftLabel.setVisible(false);
  }
  private void unBindAll() {
    Stream.of(centerLabel.textProperty(), progressBar.progressProperty()).forEach(Property::unbind);
  }

  @FXML
  private void cancel() {
    if (!task.canceled) {
      task.cancel();
      unBindAll();
      centerLabel.setText("Task cancelled");
      leftLabel.setVisible(false);
      progressBar.setVisible(false);
      cancelButton.setText("Remove");
    } else {
      App.sharingTasks.remove(task);
    }

  }
}
