package com.tambapps.p2p.fandem.cl;

import com.tambapps.p2p.fandem.FileReceiver;
import com.tambapps.p2p.fandem.util.FileUtils;
import com.tambapps.p2p.fandem.util.TransferListener;
import com.tambapps.p2p.speer.Peer;

import java.io.File;
import java.io.IOException;

public class Receiver {

  private final Peer senderPeer;
  private final FileReceiver fileReceiver;
  private final File directory;

  public Receiver(Peer senderPeer, File directory, TransferListener listener) {
    this.senderPeer = senderPeer;
    this.fileReceiver = new FileReceiver(listener);
    this.directory = directory;
  }

  public File receive() throws IOException {
    return fileReceiver.receiveFrom(senderPeer,
        FileUtils.availableFileInDirectoryProvider(directory));
  }

}
