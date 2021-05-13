package com.tambapps.p2p.fandem.fandemdesktop.service;

import com.tambapps.p2p.fandem.FileReceiver;
import com.tambapps.p2p.fandem.FileSharer;
import com.tambapps.p2p.fandem.util.FileUtils;
import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.fandem.fandemdesktop.controller.TaskViewController;
import com.tambapps.p2p.fandem.fandemdesktop.model.SharingTask;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
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
    FileReceiver fileReceiver = new FileReceiver(controller);

    Future<?> future = executorService.submit(() -> {
      try {
        fileReceiver.receiveFrom(peer, name -> {
          File file = FileUtils.newAvailableFile(folder, name);
          task.file = file;
          return file;
        });
      } catch (IOException e) {
        controller.onError(e);
      }
    });
    task.setCanceler(() -> future.cancel(true));
    return task;
  }

}
