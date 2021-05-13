package com.tambapps.p2p.fandem.fandemdesktop.service;

import com.tambapps.p2p.fandem.FileSharer;
import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.fandem.fandemdesktop.controller.TaskViewController;
import com.tambapps.p2p.fandem.fandemdesktop.model.SharingTask;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Service
public class FileSharingService {

  private final ExecutorService executorService;
  @Value("${socket.timeout.seconds}")
  private int socketTimeoutSeconds;

  public FileSharingService(@Qualifier("executorService") ExecutorService executorService) {
    this.executorService = executorService;
  }

  public SharingTask sendFile(Peer peer, File file, TaskViewController controller) {
    // TODO use the TaskViewController as the SharingErrorListener
    //  also create a multicast sender peers service and add sending peer here
    SharingTask sendingTask = new SharingTask(true);
    sendingTask.file = file;

    Future<Boolean> future = null;// TODO fileSharer.sendFile(file, peer, socketTimeoutSeconds * 1000, controller, controller);
    sendingTask.setCanceler(() -> future.cancel(true));
    return sendingTask;
  }

  public SharingTask receiveFile(File folder, Peer peer, TaskViewController controller) {
    SharingTask task = new SharingTask(false);

    Future<Boolean> future = null; /* TODO fileSharer.receiveFile(name -> {
      File file = FileUtils.newAvailableFile(folder, name);
      task.file = file;
      return file;
    }, peer, controller, controller);
    */
    task.setCanceler(() -> future.cancel(true));
    return task;
  }

}
