package com.tambapps.p2p.fandem.fandemdesktop.service;

import com.tambapps.p2p.fandem.FileSharer;
import com.tambapps.p2p.fandem.Peer;
import com.tambapps.p2p.fandem.fandemdesktop.controller.TaskViewController;
import com.tambapps.p2p.fandem.fandemdesktop.model.SharingTask;
import com.tambapps.p2p.fandem.util.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Service
public class FileSharingService {

  @Value("${socket.timeout.seconds}")
  private int socketTimeoutSeconds;

  private final FileSharer fileSharer;

  public FileSharingService(ExecutorService executorService) {
    this.fileSharer = new FileSharer(executorService);
  }

  public SharingTask sendFile(Peer peer, File file, TaskViewController controller) {
    SharingTask sendingTask = new SharingTask(true);
    sendingTask.file = file;

    Future<Boolean> future = fileSharer.sendFile(file, peer, socketTimeoutSeconds * 1000, controller, controller);
    sendingTask.setCanceler(() -> future.cancel(true));
    return sendingTask;
  }

  public SharingTask receiveFile(File folder, Peer peer, TaskViewController controller) {
    SharingTask task = new SharingTask(false);

    Future<Boolean> future = fileSharer.receiveFile(name -> {
      File file = FileUtils.newAvailableFile(folder, name);
      task.file = file;
      return file;
    }, peer, controller, controller);
    task.setCanceler(() -> future.cancel(true));
    return task;
  }

}
