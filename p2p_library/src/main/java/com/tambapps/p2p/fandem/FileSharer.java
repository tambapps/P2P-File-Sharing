package com.tambapps.p2p.fandem;

import com.tambapps.p2p.fandem.concurrent.FutureShare;
import com.tambapps.p2p.fandem.concurrent.SharingCallable;
import com.tambapps.p2p.fandem.listener.TransferListener;
import com.tambapps.p2p.fandem.task.FileProvider;
import com.tambapps.p2p.fandem.task.ReceivingTask;
import com.tambapps.p2p.fandem.task.SendingTask;
import com.tambapps.p2p.fandem.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.*;

/**
 * Util class to share files without having to deal with tasks
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
    return sendFile(new File(filePath), peer, SendingTask.DEFAULT_SOCKET_TIMEOUT, null);
  }

  public Future<Boolean> sendFile(String filePath, Peer peer, TransferListener transferListener) {
    return sendFile(new File(filePath), peer, SendingTask.DEFAULT_SOCKET_TIMEOUT, transferListener);
  }

  public Future<Boolean> sendFile(File file, Peer peer) {
    return sendFile(file, peer, SendingTask.DEFAULT_SOCKET_TIMEOUT, null);
  }

  public Future<Boolean> sendFile(File file, Peer peer, TransferListener transferListener) {
    return sendFile(file, peer, SendingTask.DEFAULT_SOCKET_TIMEOUT, transferListener);
  }

  public Future<Boolean> sendFile(File file, Peer peer, int socketTimout, TransferListener transferListener) {
    if (!file.isFile()) {
      throw new IllegalArgumentException(file.getPath() + " isn't a file");
    }
    SendCallable callable = new SendCallable(file, peer, socketTimout, transferListener);
    return new FutureShare(executorService.submit(callable), callable);
  }

  public Future<Boolean> receiveFile(FileProvider fileProvider, Peer peer, TransferListener transferListener) {
    ReceiveCallable callable = new ReceiveCallable(fileProvider, peer, transferListener);
    return new FutureShare(executorService.submit(callable), callable);
  }

  public Future<Boolean> receiveFile(File file, Peer peer) {
    return receiveFile(name -> file, peer, null);
  }

  public Future<Boolean> receiveFile(File file, Peer peer, TransferListener transferListener) {
    return receiveFile(name -> file, peer, transferListener);
  }

  public Future<Boolean> receiveFile(String filePath, Peer peer) {
    return receiveFile(new File(filePath), peer);
  }
  public Future<Boolean> receiveFile(String filePath, Peer peer, TransferListener transferListener) {
    return receiveFile(new File(filePath), peer, transferListener);
  }

  public Future<Boolean> receiveFileInDirectory(File directory, Peer peer, TransferListener transferListener) {
    if (!directory.exists()) {
      throw new IllegalArgumentException(directory + " doesn't exist");
    }
    if (!directory.isDirectory()) {
      throw new IllegalArgumentException(directory + " isn't a directory");
    }
    return receiveFile(name -> FileUtils.newAvailableFile(directory, name), peer, transferListener);
  }

  public Future<Boolean> receiveFileInDirectory(File directory, Peer peer) {
    return receiveFileInDirectory(directory, peer, null);
  }

  private static class SendCallable implements SharingCallable {

    private final SendingTask task;
    private File file;

    SendCallable(File file, Peer peer, int socketTimout, TransferListener transferListener) {
      this.task = new SendingTask(transferListener, peer, socketTimout);
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

  private static class ReceiveCallable implements SharingCallable {

    private final ReceivingTask task;
    private Peer peer;

    ReceiveCallable(FileProvider fileProvider, Peer peer, TransferListener transferListener) {
      this.task = new ReceivingTask(transferListener, fileProvider);
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
