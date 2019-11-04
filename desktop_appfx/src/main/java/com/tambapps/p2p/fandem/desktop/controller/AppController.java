package com.tambapps.p2p.fandem.desktop.controller;

import com.tambapps.p2p.fandem.desktop.model.SharingTask;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import static com.tambapps.p2p.fandem.desktop.App.sharingTasks;

public class AppController implements ListChangeListener<SharingTask> {

  @FXML
  private VBox tasksVBox;

  @FXML
  private void initialize() {
    sharingTasks.addListener(this);
  }

  public void addTaskView(Region taskView) {
    tasksVBox.getChildren().add(taskView);
  }

  @Override
  public void onChanged(Change<? extends SharingTask> change) {
    while (change.next()) {
      if (change.wasRemoved()) {
        tasksVBox.getChildren().remove(change.getTo()); //taskView and sharing task share the same index
      }
    }
  }
}
