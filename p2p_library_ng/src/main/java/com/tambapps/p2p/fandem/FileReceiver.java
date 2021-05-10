package com.tambapps.p2p.fandem;

import com.tambapps.p2p.fandem.exception.CorruptedFileException;
import com.tambapps.p2p.fandem.exception.ReceiverIOException;
import com.tambapps.p2p.fandem.handshake.FandemReceiverHandshake;
import com.tambapps.p2p.fandem.handshake.SenderHandshakeData;
import com.tambapps.p2p.fandem.util.FileProvider;
import com.tambapps.p2p.fandem.util.TransferListener;
import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.speer.PeerConnection;
import com.tambapps.p2p.speer.handshake.Handshake;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class FileReceiver extends FileSharer {

  private final AtomicReference<PeerConnection> connectionReference = new AtomicReference<>();
  private final Handshake handshake;
  private final int bufferSize;

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
    this(withChecksum, listener, 1024);
  }

  public FileReceiver(boolean withChecksum, TransferListener listener, int bufferSize) {
    super(listener);
    this.handshake = new FandemReceiverHandshake(withChecksum);
    this.bufferSize = bufferSize;
  }

  public File receiveFrom(Peer peer, File file) throws IOException {
    return receiveFrom(peer, (name -> file));
  }

  public File receiveFrom(Peer peer, FileProvider fileProvider) throws IOException {
    try (PeerConnection connection = PeerConnection.from(peer, handshake)) {
      connectionReference.set(connection);
      SenderHandshakeData data = connection.getHandshakeData();
      long totalBytes = data.getFileSize();
      String fileName = data.getFileName();
      if (listener != null) {
        listener.onConnected(connection.getSelfPeer(), connection.getPeer(), fileName, totalBytes);
      }
      Optional<String> optExpectedChecksum = data.getChecksum();
      File outputFile = fileProvider.newFile(fileName);
      if (!outputFile.exists() && !outputFile.createNewFile()) {
        throw new IOException("Couldn't create file " + outputFile);
      }
      try {
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
          share(connection.getInputStream(), fos, bufferSize, totalBytes);
        }
        if (optExpectedChecksum.isPresent()) {
          String expectedChecksum = optExpectedChecksum.get();
          String actualChecksum = computeChecksum(outputFile);
          if (!expectedChecksum.equals(actualChecksum)) {
            throw new CorruptedFileException(outputFile);
          }
        }
      } catch (IOException e) {
        throw new ReceiverIOException(e, outputFile);
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
