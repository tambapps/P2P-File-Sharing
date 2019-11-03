package com.tambapps.p2p.fandem.desktop.view;

import com.tambapps.p2p.fandem.desktop.model.SharingTask;
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
