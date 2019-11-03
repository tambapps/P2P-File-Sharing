package com.tambapps.p2p.fandem.desktop.controller;

import com.tambapps.p2p.fandem.desktop.App;
import com.tambapps.p2p.fandem.desktop.model.SharingTask;
import com.tambapps.p2p.fandem.desktop.utils.CollectionsUtils;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.IOException;

import static com.tambapps.p2p.fandem.desktop.App.sharingTasks;

public class AppController {

  @FXML
  private VBox tasksVBox;

  @FXML
  private void initialize() {
    CollectionsUtils.bindMapList(sharingTasks, this::generateTaskView, tasksVBox.getChildren());
  }

  private Region generateTaskView(SharingTask sharingTask) {
    FXMLLoader loader = new FXMLLoader(App.class.getResource("taskView.fxml"));
    Region taskView = null;
    try {
      taskView = loader.load();
    } catch (IOException e) {
      throw new RuntimeException("Coudln't load taskView", e);
    }
    taskView.setBackground(Background.EMPTY);
    TaskController controller = loader.getController();
    controller.setTask(sharingTask);
    return taskView;
  }
}
