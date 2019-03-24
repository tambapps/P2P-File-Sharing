package com.tambapps.p2p.fandem;

import com.tambapps.p2p.fandem.concurrent.FutureShare;
import com.tambapps.p2p.fandem.concurrent.SharingCallable;
import com.tambapps.p2p.fandem.listener.SharingErrorListener;
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

  public Future<Boolean> sendFile(String filePath, Peer peer, SharingErrorListener errorListener) {
    return sendFile(new File(filePath), peer, SendingTask.DEFAULT_SOCKET_TIMEOUT, null, errorListener);
  }

  public Future<Boolean> sendFile(String filePath, Peer peer, TransferListener transferListener, SharingErrorListener errorListener) {
    return sendFile(new File(filePath), peer, SendingTask.DEFAULT_SOCKET_TIMEOUT, transferListener, errorListener);
  }

  public Future<Boolean> sendFile(File file, Peer peer, SharingErrorListener errorListener) {
    return sendFile(file, peer, SendingTask.DEFAULT_SOCKET_TIMEOUT, null, errorListener);
  }

  public Future<Boolean> sendFile(File file, Peer peer, TransferListener transferListener, SharingErrorListener errorListener) {
    return sendFile(file, peer, SendingTask.DEFAULT_SOCKET_TIMEOUT, transferListener, errorListener);
  }

  public Future<Boolean> sendFile(File file, Peer peer, int socketTimout, TransferListener transferListener, SharingErrorListener errorListener) {
    if (!file.isFile()) {
      throw new IllegalArgumentException(file.getPath() + " isn't a file");
    }
    SendCallable callable = new SendCallable(file, peer, socketTimout, transferListener, errorListener);
    return new FutureShare(executorService.submit(callable), callable);
  }

  public Future<Boolean> receiveFile(FileProvider fileProvider, Peer peer, TransferListener transferListener, SharingErrorListener errorListener) {
    ReceiveCallable callable = new ReceiveCallable(fileProvider, peer, transferListener, errorListener);
    return new FutureShare(executorService.submit(callable), callable);
  }

  public Future<Boolean> receiveFile(File file, Peer peer, SharingErrorListener errorListener) {
    return receiveFile(name -> file, peer, null, errorListener);
  }

  public Future<Boolean> receiveFile(File file, Peer peer, TransferListener transferListener, SharingErrorListener errorListener) {
    return receiveFile(name -> file, peer, transferListener, errorListener);
  }

  public Future<Boolean> receiveFile(String filePath, Peer peer, SharingErrorListener errorListener) {
    return receiveFile(new File(filePath), peer, errorListener);
  }

  public Future<Boolean> receiveFile(String filePath, Peer peer, TransferListener transferListener, SharingErrorListener errorListener) {
    return receiveFile(new File(filePath), peer, transferListener, errorListener);
  }

  public Future<Boolean> receiveFileInDirectory(File directory, Peer peer, TransferListener transferListener,
                                                SharingErrorListener errorListener) {
    if (!directory.exists()) {
      throw new IllegalArgumentException(directory + " doesn't exist");
    }
    if (!directory.isDirectory()) {
      throw new IllegalArgumentException(directory + " isn't a directory");
    }
    return receiveFile(name -> FileUtils.newAvailableFile(directory, name), peer, transferListener, errorListener);
  }

  public Future<Boolean> receiveFileInDirectory(File directory, Peer peer, SharingErrorListener errorListener) {
    return receiveFileInDirectory(directory, peer, null, errorListener);
  }

  private static class SendCallable implements SharingCallable {

    private final SendingTask task;
    private final File file;
    private final SharingErrorListener errorListener;

    SendCallable(File file, Peer peer, int socketTimout, TransferListener transferListener, SharingErrorListener errorListener) {
      this.task = new SendingTask(transferListener, peer, socketTimout);
      this.file = file;
      this.errorListener = errorListener;
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
        errorListener.onError(e);
        return false;
      }
    }
  }

  private static class ReceiveCallable implements SharingCallable {

    private final ReceivingTask task;
    private final Peer peer;
    private final SharingErrorListener errorListener;

    ReceiveCallable(FileProvider fileProvider, Peer peer, TransferListener transferListener, SharingErrorListener errorListener) {
      this.errorListener = errorListener;
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
        errorListener.onError(e);
        return false;
      }
    }
  }
}
