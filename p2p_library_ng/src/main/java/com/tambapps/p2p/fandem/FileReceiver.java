package com.tambapps.p2p.fandem;

import com.tambapps.p2p.fandem.handshake.FandemReceiverHandshake;
import com.tambapps.p2p.fandem.util.FileProvider;
import com.tambapps.p2p.fandem.util.TransferListener;
import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.speer.PeerConnection;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileReceiver extends FileSharer {

  public FileReceiver() {
    this(true, null);
  }

  public FileReceiver(TransferListener listener) {
    this(true, listener);
  }
  public FileReceiver(boolean withChecksum) {
    this(withChecksum, null);
  }

  public FileReceiver(boolean withChecksum, TransferListener listener) {
    super(new FandemReceiverHandshake(withChecksum), listener);
  }

  public void receiveFrom(Peer peer, File file) throws IOException {
    receiveFrom(peer, (name -> file));
  }

  public void receiveFrom(Peer peer, FileProvider fileProvider) throws IOException {
    try (PeerConnection connection = PeerConnection.from(peer, handshake);
        DataInputStream inputStream = connection.getInputStream()) {
      long totalBytes = inputStream.readLong();
      String fileName = inputStream.readUTF();
      if (listener != null) {
        listener.onConnected(peer, fileName, totalBytes);
      }
      File outputFile = fileProvider.newFile(fileName);
      try (FileOutputStream fos = new FileOutputStream(outputFile)) {
        share(inputStream, fos, totalBytes);
      }
    }
  }
}
