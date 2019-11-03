package com.tambapps.p2p.fandem.desktop.view;

import com.tambapps.p2p.fandem.desktop.model.SharingTask;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;

public class TaskView extends Pane {

  public static TaskView generate(SharingTask task) {
    if (task.sender) {
      return new SendingView(task);
    } else {
      return new ReceivingView(task);
    }
  }
  

  private TaskView(SharingTask task) {
    setPrefWidth(1000);
    setPrefHeight(50);

    ProgressBar progressBar = new ProgressBar();
    progressBar.progressProperty().bind(task.percentage);

    Label infoLabel = new Label();
    if (task.sender) {
      infoLabel.textProperty().set("Waiting connection on " + task.peer.get() + " ...");
    } else {
      infoLabel.textProperty().set("Connecting to " + task.remotePeer.get() + " ...");
    }
    getChildren().add(progressBar);
    // TODO bind task properties
  }

  private static class SendingView extends TaskView {

    private SendingView(SharingTask task) {
      super(task);
      setStyle("-fx-background-color: #000000");

    }
  }

  private static class ReceivingView extends TaskView {

    private ReceivingView(SharingTask task) {
      super(task);
      setStyle("-fx-background-color: #ffffff");
    }
  }
}
