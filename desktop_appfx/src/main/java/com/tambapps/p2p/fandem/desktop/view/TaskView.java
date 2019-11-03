package com.tambapps.p2p.fandem.desktop.view;

import com.tambapps.p2p.fandem.desktop.model.ReceivingTask;
import com.tambapps.p2p.fandem.desktop.model.SendingTask;
import com.tambapps.p2p.fandem.desktop.model.SharingTask;
import javafx.scene.layout.Pane;

public class TaskView extends Pane {

  public static TaskView generate(SharingTask task) {
    if (task instanceof SendingTask) {
      return new SendingView((SendingTask) task);
    } else {
      return new ReceivingView((ReceivingTask) task);
    }
  }

  private TaskView(SharingTask task) {
    setPrefWidth(1000);
    setPrefHeight(50);
    // TODO bind task properties
  }

  private static class SendingView extends TaskView {

    private SendingView(SendingTask task) {
      super(task);
      setStyle("-fx-background-color: #000000");

    }
  }

  private static class ReceivingView extends TaskView {

    private ReceivingView(ReceivingTask task) {
      super(task);
      setStyle("-fx-background-color: #ffffff");
    }
  }
}
