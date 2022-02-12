package com.tambapps.p2p.fandem;

import com.tambapps.p2p.fandem.handshake.FandemSenderHandshake;
import com.tambapps.p2p.fandem.handshake.SenderHandshakeData;
import com.tambapps.p2p.fandem.model.SendingFileData;
import com.tambapps.p2p.fandem.util.TransferListener;
import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.speer.PeerConnection;
import com.tambapps.p2p.speer.PeerServer;
import com.tambapps.p2p.speer.handshake.Handshake;
import lombok.Getter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class FileSender extends FileSharer {

  @Getter
  private final Peer peer;
  @Getter
  private final int socketTimeout;
  private final AtomicReference<PeerConnection> connectionReference = new AtomicReference<>();

  public FileSender(Peer peer) {
    this(peer, null);
  }

  public FileSender(Peer peer, TransferListener listener) {
    this(peer, listener, 0);
  }

  public FileSender(Peer peer, TransferListener listener, int socketTimeout) {
    super(listener);
    this.peer = peer;
    this.socketTimeout = socketTimeout;
  }

  public void sendFiles(List<File> files) throws IOException {
    List<SendingFileData> sendingFileDataList = new ArrayList<>();
    for (File file : files) {
      sendingFileDataList.add(SendingFileData.fromFile(file));
    }
    send(sendingFileDataList);
  }

  public void send(List<SendingFileData> files) throws IOException {
    try (PeerServer server = peerServer(
        new FandemSenderHandshake(new SenderHandshakeData(files)));
        PeerConnection connection = server.accept()) {
        connectionReference.set(connection);
      if (listener != null) {
        listener.onConnected(connection.getSelfPeer(), connection.getRemotePeer());
      }
      for (SendingFileData fileData : files) {
        String fileName = fileData.getFileName();
        long fileSize = fileData.getFileSize();
        if (listener != null) {
          listener.onTransferStarted(fileName, fileSize);
        }
        try (InputStream inputStream = fileData.newInputStream()) {
          share(inputStream, connection.getOutputStream(), DEFAULT_BUFFER_SIZE, fileSize,
              fileData.getChecksum().orElse(null));
        }
      }
    }
    connectionReference.set(null);
  }

  public void cancel() {
    PeerConnection connection = connectionReference.get();
    if (connection != null) {
      try {
        connection.close();
      } catch (IOException e) {
        // ignore
      }
      connectionReference.set(null);
    }
  }

  private PeerServer peerServer(Handshake handshake) throws IOException {
    PeerServer server = new PeerServer(peer, handshake);
    server.setAcceptTimeout(socketTimeout);
    return server;
  }
}
