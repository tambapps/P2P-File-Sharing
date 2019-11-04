package com.tambapps.p2p.fandem.desktop.controller;

import com.tambapps.p2p.fandem.desktop.App;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;

import java.io.IOException;
import java.util.function.Consumer;

public class SharingPaneController {

  private Consumer<Region> taskViewConsumer;

  public void setTaskViewConsumer(Consumer<Region> taskViewConsumer) {
    this.taskViewConsumer = taskViewConsumer;
  }

  Region loadTaskView(Consumer<TaskController> controllerConsumer) {
    FXMLLoader loader = new FXMLLoader(App.class.getResource("taskView.fxml"));
    try {
      Region taskView = loader.load();
      taskView.setBackground(Background.EMPTY);
      controllerConsumer.accept(loader.getController());
      return taskView;
    } catch (IOException e) {
      throw new RuntimeException("Couldn't load taskView", e);
    }
  }

  void submitTaskView(Consumer<TaskController> controllerConsumer) {
    taskViewConsumer.accept(loadTaskView(controllerConsumer));
  }
}
