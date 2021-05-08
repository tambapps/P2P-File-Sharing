package com.tambapps.p2p.fandem;

import com.tambapps.p2p.fandem.util.TransferListener;
import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.speer.PeerConnection;
import com.tambapps.p2p.speer.PeerServer;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicReference;

public class FileSender extends FileSharer {

  private final Peer peer;
  private final int socketTimeout;
  private final AtomicReference<PeerServer> serverReference = new AtomicReference<>();

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

  public void send(File file) throws IOException {
    try (FileInputStream inputStream = new FileInputStream(file)) {
      send(inputStream, file.getName(), file.length());
    }
  }

  public void send(FileInputStream inputStream, String fileName, long fileSize) throws IOException {
    try (PeerServer server = peerServer();
        PeerConnection connection = server.accept();
        DataOutputStream outputStream = connection.getOutputStream()) {
      outputStream.writeLong(fileSize);
      outputStream.writeUTF(fileName);
      share(inputStream, outputStream, fileSize);
    }
    serverReference.set(null);
  }

  public void cancel() {
    PeerServer server = serverReference.get();
    if (server != null) {
      try {
        server.close();
      } catch (IOException e) {
        // ignore
      }
      serverReference.set(null);
    }
  }
  private PeerServer peerServer() throws IOException {
    PeerServer server = new PeerServer(peer, handshake);
    server.setAcceptTimeout(socketTimeout);
    serverReference.set(server);
    return server;
  }
}
