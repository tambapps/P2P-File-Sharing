package com.tambapps.p2p.fandem.desktop.service;

import com.tambapps.p2p.fandem.FileSharer;
import com.tambapps.p2p.fandem.Peer;
import com.tambapps.p2p.fandem.desktop.controller.TaskController;
import com.tambapps.p2p.fandem.desktop.model.SharingTask;
import com.tambapps.p2p.fandem.util.FileUtils;
import com.tambapps.p2p.fandem.util.IPUtils;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class FileSharingService {

  private final FileSharer fileSharer;

  public FileSharingService(ExecutorService executorService) {
    this.fileSharer = new FileSharer(executorService);
  }

  public SharingTask sendFile(File file, TaskController controller) throws IOException {
    SharingTask sendingTask = new SharingTask(true);
    sendingTask.file = file;
    Peer peer;
    try {
      peer = IPUtils.getAvailablePeer();
    } catch (SocketException e) {
      throw new IOException("Couldn't retrieve IP. Are you connected to internet?");
    }
    sendingTask.peer = peer;
    Future future = fileSharer.sendFile(file, peer, 30 * 1000, controller, controller);
    sendingTask.setCanceler(() -> future.cancel(true));
    return sendingTask;
  }

  public SharingTask receiveFile(File folder, Peer peer, TaskController controller) {
    SharingTask task = new SharingTask(false);
    task.remotePeer = peer;

    Future future = fileSharer.receiveFile(name -> {
      File file = FileUtils.newAvailableFile(folder, name);
      task.file = file;
      return file;
    }, peer, controller, controller);
    task.setCanceler(() -> future.cancel(true));
    return task;
  }

}
