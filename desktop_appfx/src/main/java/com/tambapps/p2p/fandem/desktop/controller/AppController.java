package com.tambapps.p2p.fandem.desktop.controller;

import com.tambapps.p2p.fandem.desktop.utils.CollectionsUtils;
import com.tambapps.p2p.fandem.desktop.view.TaskView;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

import static com.tambapps.p2p.fandem.desktop.App.sharingTasks;

public class AppController {

  @FXML
  private VBox tasksVBox;

  @FXML
  private void initialize() {
    CollectionsUtils.bindMapList(sharingTasks, TaskView::generate, tasksVBox.getChildren());
  }

}
