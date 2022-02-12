package com.tambapps.p2p.fandem.cl;

import com.tambapps.p2p.fandem.FileReceiver;
import com.tambapps.p2p.fandem.util.FileUtils;
import com.tambapps.p2p.fandem.util.TransferListener;
import com.tambapps.p2p.speer.Peer;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Receiver {

  private final Peer senderPeer;
  private final FileReceiver fileReceiver;
  private final File downloadFile;

  public Receiver(Peer senderPeer, File downloadFile, TransferListener listener) {
    this.senderPeer = senderPeer;
    this.fileReceiver = new FileReceiver(listener);
    this.downloadFile = downloadFile;
  }

  public List<File> receive() throws IOException {
    return downloadFile.isDirectory() ? fileReceiver.receiveFrom(senderPeer,
        FileUtils.availableFileInDirectoryProvider(downloadFile)) :
        fileReceiver.receiveInDirectory(senderPeer, downloadFile);
  }

}
