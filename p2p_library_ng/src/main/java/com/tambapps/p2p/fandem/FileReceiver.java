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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class FileReceiver extends FileSharer {

  private final boolean withChecksum;
  private final AtomicReference<PeerConnection> connectionReference = new AtomicReference<>();

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

  public File receiveFrom(Peer peer, File file) throws IOException {
    return receiveFrom(peer, (name -> file));
  }

  public File receiveFrom(Peer peer, FileProvider fileProvider) throws IOException {
    try (PeerConnection connection = PeerConnection.from(peer, handshake)) {
      connectionReference.set(connection);
      long totalBytes = connection.readLong();
      String fileName = connection.readUTF();
      int bufferSize = connection.getAttribute(FandemSenderHandshake.BUFFER_SIZE_KEY);
      if (listener != null) {
        listener.onConnected(connection.getSelfPeer(), connection.getConnectedPeer(), fileName, totalBytes);
      }
      Optional<String> optExpectedChecksum = withChecksum ?
          Optional.of(connection.readUTF()) : Optional.empty();
      File outputFile = fileProvider.newFile(fileName);
      try (FileOutputStream fos = new FileOutputStream(outputFile)) {
        share(connection.getInputStream(), fos, bufferSize, totalBytes);
      }
      if (optExpectedChecksum.isPresent()) {
        String expectedChecksum = optExpectedChecksum.get();
        String actualChecksum = computeChecksum(outputFile);
        if (!expectedChecksum.equals(actualChecksum)) {
          throw new CorruptedFileException();
        }
      }
      return outputFile;
    }
  }

  public void cancel() {
    PeerConnection connection = connectionReference.get();
    if (connection != null) {
      try {
        connection.close();
      } catch (IOException e) {
        // ignore it, it's just a cancel
      }
      connectionReference.set(null);
    }
  }
}
