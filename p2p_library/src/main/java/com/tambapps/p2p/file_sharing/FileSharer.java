package com.tambapps.p2p.file_sharing;

import com.tambapps.p2p.file_sharing.concurrent.FutureShare;
import com.tambapps.p2p.file_sharing.concurrent.ShareCallable;
import com.tambapps.p2p.file_sharing.task.ReceivingTask;
import com.tambapps.p2p.file_sharing.task.SendingTask;
import com.tambapps.p2p.file_sharing.task.SharingTask;
import com.tambapps.p2p.file_sharing.util.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.*;

public class FileSharer {

  private final ExecutorService executorService;

  public FileSharer() {
    this(Runtime.getRuntime().availableProcessors());
  }

  public FileSharer(int nbThreads) {
    this.executorService = Executors.newFixedThreadPool(nbThreads);
  }

  public Future<Boolean> sendFile(String filePath, Peer peer) throws FileNotFoundException {
    File file = new File(filePath);
    if (!file.exists()) {
      throw new FileNotFoundException("File with path '" + filePath + "' was not found");
    }
    return sendFile(file, peer, SendingTask.DEFAULT_SOCKET_TIMEOUT);
  }

  public Future<Boolean> sendFile(File file, Peer peer) {
    return sendFile(file, peer, SendingTask.DEFAULT_SOCKET_TIMEOUT);
  }

  public Future<Boolean> sendFile(File file, Peer peer, int socketTimout) {
    SendCallable callable = new SendCallable(file, peer, socketTimout);
    return new FutureShare(executorService.submit(callable), callable);
  }

  public Future<Boolean> receiveFile(File file, Peer peer) {
    ReceiveCallable callable = new ReceiveCallable(file, peer);
    return new FutureShare(executorService.submit(callable), callable);
  }



  static class SendCallable implements ShareCallable {
    private final SendingTask task;
    private File file;

    public SendCallable(File file, Peer peer, int socketTimout) {
      this.task = new SendingTask(peer, socketTimout);
      this.file = file;
    }

    @Override
    public void cancel() {
      task.cancel();
    }

    @Override
    public Boolean call() {
      try {
        task.send(file);
        return true;
      } catch (IOException e) {
        return false;
      }
    }
  }

  static class ReceiveCallable implements ShareCallable {

    private final ReceivingTask task;
    private Peer peer;

    public ReceiveCallable(File file, Peer peer) {
      this.task = new ReceivingTask(file);
      this.peer = peer;
    }

    @Override
    public void cancel() {
      task.cancel();
    }

    @Override
    public Boolean call() {
      try {
        task.receiveFrom(peer);
        return true;
      } catch (IOException e) {
        return false;
      }
    }
  }
}
