package com.tambapps.p2p.file_sharing;

import com.tambapps.p2p.file_sharing.concurrent.FutureShare;
import com.tambapps.p2p.file_sharing.concurrent.ShareCallable;
import com.tambapps.p2p.file_sharing.task.FileProvider;
import com.tambapps.p2p.file_sharing.task.ReceivingTask;
import com.tambapps.p2p.file_sharing.task.SendingTask;
import com.tambapps.p2p.file_sharing.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.*;

/**
 * Util class to share files without having to deals with tasks
 */
public class FileSharer {

  private final ExecutorService executorService;

  public FileSharer() {
    this(Runtime.getRuntime().availableProcessors());
  }

  public FileSharer(int nbThreads) {
    this.executorService = Executors.newFixedThreadPool(nbThreads);
  }

  public Future<Boolean> sendFile(String filePath, Peer peer) {
    return sendFile(new File(filePath), peer, SendingTask.DEFAULT_SOCKET_TIMEOUT);
  }

  public Future<Boolean> sendFile(File file, Peer peer) {
    return sendFile(file, peer, SendingTask.DEFAULT_SOCKET_TIMEOUT);
  }

  public Future<Boolean> sendFile(File file, Peer peer, int socketTimout) {
    if (!file.isFile()) {
      throw new IllegalArgumentException(file.getPath() + " isn't a file");
    }
    SendCallable callable = new SendCallable(file, peer, socketTimout);
    return new FutureShare(executorService.submit(callable), callable);
  }

  public Future<Boolean> receiveFile(FileProvider fileProvider, Peer peer) {
    ReceiveCallable callable = new ReceiveCallable(fileProvider, peer);
    return new FutureShare(executorService.submit(callable), callable);
  }

  public Future<Boolean> receiveFile(File file, Peer peer) {
    return receiveFile(name -> file, peer);
  }

  public Future<Boolean> receiveFile(String filePath, Peer peer) {
    return receiveFile(new File(filePath), peer);
  }

  public Future<Boolean> receiveFileInDirectory(File directory, Peer peer) {
    if (!directory.exists()) {
      throw new IllegalArgumentException(directory + " doesn't exist");
    }
    if (!directory.isDirectory()) {
      throw new IllegalArgumentException(directory + " isn't a directory");
    }
    return receiveFile(name -> FileUtils.newAvailableFile(directory, name), peer);
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

    public ReceiveCallable(FileProvider fileProvider, Peer peer) {
      this.task = new ReceivingTask(fileProvider);
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
