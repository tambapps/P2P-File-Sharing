package com.tambapps.p2p.fandem.fandemdesktop.controller;

import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.fandem.fandemdesktop.model.SharingTask;
import com.tambapps.p2p.fandem.fandemdesktop.util.RegionLoader;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class AppController implements ListChangeListener<SharingTask> {

  private final RegionLoader regionLoader;

  @Value("classpath:/view/taskView.fxml")
  private Resource taskViewResource;
  @FXML
  private VBox tasksVBox;

  public AppController(RegionLoader regionLoader, ObservableList<SharingTask> sharingTasks) {
    this.regionLoader = regionLoader;
    sharingTasks.addListener(this);
  }

  @Override
  public void onChanged(Change<? extends SharingTask> change) {
    while (change.next()) {
      if (change.wasRemoved()) {
        tasksVBox.getChildren()
          .remove(change.getTo()); //taskView and sharing task share the same index
      }
    }
  }

  public void sendTask(File file) {
    try {
      Pair<Region, TaskViewController> pair = regionLoader.loadWithController(taskViewResource);
      TaskViewController controller = pair.getValue();
      tasksVBox.getChildren().add(pair.getKey());
      controller.sendTask(file);
    }  catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void receiveTask(File folder, Peer peer) {
    try {
      Pair<Region, TaskViewController> pair = regionLoader.loadWithController(taskViewResource);
      TaskViewController controller = pair.getValue();
      tasksVBox.getChildren().add(pair.getKey());
      controller.receiveTask(folder, peer);
    }  catch (IOException e) {
      e.printStackTrace();
    }
  }
}
