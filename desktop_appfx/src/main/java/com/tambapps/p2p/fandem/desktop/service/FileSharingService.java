package com.tambapps.p2p.fandem.desktop.service;

import com.tambapps.p2p.fandem.FileSharer;
import com.tambapps.p2p.fandem.Peer;
import com.tambapps.p2p.fandem.desktop.controller.TaskController;
import com.tambapps.p2p.fandem.desktop.model.SharingTask;
import com.tambapps.p2p.fandem.util.FileUtils;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class FileSharingService {

  private final FileSharer fileSharer;

  public FileSharingService(ExecutorService executorService) {
    this.fileSharer = new FileSharer(executorService);
  }

  public SharingTask sendFile(Peer peer, File file, TaskController controller) {
    SharingTask sendingTask = new SharingTask(true);
    sendingTask.file = file;

    Future future = fileSharer.sendFile(file, peer, 30 * 1000, controller, controller);
    sendingTask.setCanceler(() -> future.cancel(true));
    return sendingTask;
  }

  public SharingTask receiveFile(File folder, Peer peer, TaskController controller) {
    SharingTask task = new SharingTask(false);

    Future future = fileSharer.receiveFile(name -> {
      File file = FileUtils.newAvailableFile(folder, name);
      task.file = file;
      return file;
    }, peer, controller, controller);
    task.setCanceler(() -> future.cancel(true));
    return task;
  }

}
