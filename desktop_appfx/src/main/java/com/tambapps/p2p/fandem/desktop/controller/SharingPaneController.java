package com.tambapps.p2p.fandem.desktop.controller;

import com.tambapps.p2p.fandem.desktop.App;
import com.tambapps.p2p.fandem.desktop.model.SharingTask;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;

import java.io.IOException;
import java.util.function.BiConsumer;

public class SharingPaneController {

  protected BiConsumer<SharingTask, Region> taskRegionBiConsumer;

  public void setTaskRegionBiConsumer(BiConsumer<SharingTask, Region> taskRegionBiConsumer) {
    this.taskRegionBiConsumer = taskRegionBiConsumer;
  }


  Region loadTaskView() {
    FXMLLoader loader = new FXMLLoader(App.class.getResource("taskView.fxml"));
    try {
      Region taskView = loader.load();
      taskView.setBackground(Background.EMPTY);
      return taskView;
    } catch (IOException e) {
      throw new RuntimeException("Couldn't load taskView", e);
    }
  }
}
