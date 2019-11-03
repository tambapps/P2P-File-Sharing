package com.tambapps.p2p.fandem.desktop.service;

import com.tambapps.p2p.fandem.FileSharer;
import com.tambapps.p2p.fandem.Peer;
import com.tambapps.p2p.fandem.desktop.model.SharingTask;
import com.tambapps.p2p.fandem.listener.TransferListener;
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

  public SharingTask sendFile(File file) throws IOException {
    SharingTask sendingTask = new SharingTask(true);
    sendingTask.file.set(file);
    Peer peer;
    try {
      peer = IPUtils.getAvailablePeer();
    } catch (SocketException e) {
      throw new IOException("Couldn't retrieve IP. Are you connected to internet?");
    }
    sendingTask.peer.set(peer);
    Future future = fileSharer.sendFile(file, peer, new TransferListener() {
      @Override
      public void onConnected(Peer selfPeer, Peer remotePeer, String fileName, long fileSize) {
        sendingTask.remotePeer.set(remotePeer);
        sendingTask.totalBytes.set(fileSize);
      }

      @Override
      public void onProgressUpdate(int progress, long byteProcessed, long totalBytes) {
        sendingTask.percentage.setValue(((double)byteProcessed) / ((double)totalBytes));
        sendingTask.bytesProcessed.set(byteProcessed);
      }
    }, e -> {
      // TODO
    });
    sendingTask.setCanceler(() -> future.cancel(true));
    return sendingTask;
  }

  public SharingTask receiveFile(File folder, Peer peer) {
    SharingTask task = new SharingTask(false);
    task.remotePeer.set(peer);

    Future future = fileSharer.receiveFile(name -> {
      File file = new File(folder, name);
      task.file.set(file);
      return file;
    }, peer, new TransferListener() {
      @Override
      public void onConnected(Peer selfPeer, Peer remotePeer, String fileName, long fileSize) {
        task.remotePeer.set(remotePeer);
        task.peer.set(selfPeer);
        task.totalBytes.set(fileSize);
      }

      @Override
      public void onProgressUpdate(int progress, long byteProcessed, long totalBytes) {
        task.percentage.setValue(((double)byteProcessed) / ((double)totalBytes));
        task.bytesProcessed.set(byteProcessed);
      }
    }, e -> {

    });
    task.setCanceler(() -> future.cancel(true));
    return task;
  }

}
