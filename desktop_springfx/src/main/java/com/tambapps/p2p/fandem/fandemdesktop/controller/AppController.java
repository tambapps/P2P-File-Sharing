package com.tambapps.p2p.fandem.fandemdesktop.controller;

import com.tambapps.p2p.fandem.Peer;
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

  @Value("classpath:/view/taskView.fxml")
  private Resource taskViewResource;

  private final ObservableList<SharingTask> sharingTasks;
  private final RegionLoader regionLoader;

  @FXML
  private VBox tasksVBox;

  public AppController(ObservableList<SharingTask> sharingTasks, RegionLoader regionLoader) {
    this.sharingTasks = sharingTasks;
    this.regionLoader = regionLoader;
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
