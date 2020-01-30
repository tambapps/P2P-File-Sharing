package com.tambapps.p2p.fandem.cl;

import com.tambapps.p2p.fandem.Peer;
import com.tambapps.p2p.fandem.listener.ReceivingListener;
import com.tambapps.p2p.fandem.task.ReceivingTask;
import com.tambapps.p2p.fandem.util.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class Receiver implements ReceivingListener  {

  private final Peer peer;
  private final File directory;


  final String progressFormat = "\rReceived %s / %s";

  public Receiver(Peer peer, File directory) {
    this.peer = peer;
    this.directory = directory;
  }

  public void receive() throws IOException {
    new ReceivingTask(this, FileUtils.availableFileInDirectoryProvider(directory)).receiveFrom(peer);
  }

  @Override
  public void onConnected(@NotNull Peer selfPeer, @NotNull Peer remotePeer, @NotNull String fileName, long fileSize) {
    System.out.println("Connected to peer " + remotePeer);
    System.out.println("Receiving " + fileName);
    System.out.format(progressFormat, "0kb",
        FileUtils.bytesToString(fileSize));
  }

  @Override
  public void onProgressUpdate(int progress, long byteProcessed, long totalBytes) {
    System.out.format(progressFormat,
        FileUtils.bytesToString(byteProcessed),
        FileUtils.bytesToString(totalBytes));
  }

  @Override
  public void onEnd(File file) {
    System.out.println();
    System.out.println("Received " + file.getName() + " successfully");
    System.out.println("Path: " + file.getPath());
  }
}
