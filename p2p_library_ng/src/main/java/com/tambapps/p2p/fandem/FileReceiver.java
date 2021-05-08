package com.tambapps.p2p.fandem;

import com.tambapps.p2p.fandem.exception.CorruptedFileException;
import com.tambapps.p2p.fandem.handshake.FandemReceiverHandshake;
import com.tambapps.p2p.fandem.handshake.FandemSenderHandshake;
import com.tambapps.p2p.fandem.util.FileProvider;
import com.tambapps.p2p.fandem.util.TransferListener;
import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.speer.PeerConnection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileReceiver extends FileSharer {

  private final boolean withChecksum;

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
    this.withChecksum = withChecksum;
  }

  public void receiveFrom(Peer peer, File file) throws IOException {
    receiveFrom(peer, (name -> file));
  }

  public void receiveFrom(Peer peer, FileProvider fileProvider) throws IOException {
    try (PeerConnection connection = PeerConnection.from(peer, handshake)) {
      long totalBytes = connection.readLong();
      String fileName = connection.readUTF();
      int bufferSize = connection.getAttribute(FandemSenderHandshake.CHECKSUM_KEY);
      if (listener != null) {
        listener.onConnected(peer, fileName, totalBytes);
      }
      File outputFile = fileProvider.newFile(fileName);
      try (FileOutputStream fos = new FileOutputStream(outputFile)) {
        share(connection.getInputStream(), fos, bufferSize, totalBytes);
      }
      if (withChecksum) {
        String expectedChecksum = connection.readUTF();
        String actualChecksum = computeChecksum(outputFile);
        if (!expectedChecksum.equals(actualChecksum)) {
          throw new CorruptedFileException();
        }
      }
    }
  }
}
