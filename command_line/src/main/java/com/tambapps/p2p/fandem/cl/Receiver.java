package com.tambapps.p2p.fandem.cl;

import com.tambapps.p2p.fandem.Peer;
import com.tambapps.p2p.fandem.listener.ReceivingListener;
import com.tambapps.p2p.fandem.task.ReceivingTask;
import com.tambapps.p2p.fandem.util.FileUtils;

import java.io.File;
import java.io.IOException;

public class Receiver {

  private final Peer peer;
  private final File directory;
  private final ReceivingListener listener;

  public Receiver(Peer peer, File directory, ReceivingListener listener) {
    this.peer = peer;
    this.directory = directory;
    this.listener = listener;
  }

  public void receive() throws IOException {
    new ReceivingTask(listener, FileUtils.availableFileInDirectoryProvider(directory))
        .receiveFrom(peer);
  }

}
