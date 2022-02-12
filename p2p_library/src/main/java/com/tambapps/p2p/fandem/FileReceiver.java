package com.tambapps.p2p.fandem;

import com.tambapps.p2p.fandem.handshake.FandemReceiverHandshake;
import com.tambapps.p2p.fandem.handshake.SenderHandshakeData;
import com.tambapps.p2p.fandem.model.FileData;
import com.tambapps.p2p.fandem.util.OutputStreamProvider;
import com.tambapps.p2p.fandem.util.FileProvider;
import com.tambapps.p2p.fandem.util.RecordingFileProvider;
import com.tambapps.p2p.fandem.util.TransferListener;
import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.speer.PeerConnection;
import com.tambapps.p2p.speer.handshake.Handshake;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class FileReceiver extends FileSharer {

  private final AtomicReference<PeerConnection> connectionReference = new AtomicReference<>();
  private final Handshake handshake;
  private final int bufferSize;

  public FileReceiver() {
    this(null);
  }


  public FileReceiver(TransferListener listener) {
    this(listener, DEFAULT_BUFFER_SIZE);
  }

  public FileReceiver(TransferListener listener, int bufferSize) {
    super(listener);
    this.handshake = new FandemReceiverHandshake();
    this.bufferSize = bufferSize;
  }

  public List<File> receiveInDirectory(Peer peer, File directory) throws IOException {
    return receiveFrom(peer, (FileProvider) (name -> new File(directory, name)));
  }

  public List<File> receiveFrom(Peer peer, FileProvider fileProvider) throws IOException {
    RecordingFileProvider recordingFileProvider = new RecordingFileProvider(fileProvider);
    receiveFrom(peer, recordingFileProvider.toOutputStreamProvider());
    return recordingFileProvider.getFiles();
  }

  // just for Android 11+ :( ?? but whyyyy?????
  public void receiveFrom(Peer peer, OutputStreamProvider outputStreamProvider) throws IOException {
    try (PeerConnection connection = PeerConnection.from(peer, handshake)) {
      connectionReference.set(connection);
      SenderHandshakeData data = connection.getHandshakeData();
      if (listener != null) {
        listener.onConnected(connection.getSelfPeer(), connection.getRemotePeer());
      }

      for (FileData fileData : data.getFiles()) {
        String fileName = fileData.getFileName();
        long fileSize = fileData.getFileSize();
        Optional<String> optExpectedChecksum = fileData.getChecksum();
        try (OutputStream fos = outputStreamProvider.newOutputStream(fileName)) {
          if (listener != null) {
            listener.onTransferStarted(fileName, fileSize);
          }
          share(connection.getInputStream(), fos, bufferSize, fileName, fileSize, optExpectedChecksum.orElse(null));
        }
      }
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
