package com.tambapps.p2p.fandem.desktop.service;

import com.tambapps.p2p.fandem.FileReceiver;
import com.tambapps.p2p.fandem.util.FileProvider;
import com.tambapps.p2p.fandem.util.FileUtils;
import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.fandem.desktop.controller.TaskViewController;
import com.tambapps.p2p.fandem.desktop.model.SharingTask;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Service
public class FileReceiverService {

  private final ExecutorService executorService;

  public FileReceiverService(@Qualifier("executorService") ExecutorService executorService) {
    this.executorService = executorService;
  }

  public SharingTask receiveFile(File folder, Peer peer, TaskViewController controller) {
    SharingTask task = new SharingTask(false);
    // will be filled as long as we received file
    task.files = new ArrayList<>();
    FileReceiver fileReceiver = new FileReceiver(controller);

    Future<?> future = executorService.submit(() -> {
      try {
        fileReceiver.receiveFrom(peer, (FileProvider) name -> {
          File file = FileUtils.newAvailableFile(folder, name);
          task.files.add(file);
          return file;
        });
        controller.onEnd();
      } catch (IOException e) {
        controller.onError(e);
      }
    });
    task.setCanceler(() -> future.cancel(true));
    return task;
  }

}
