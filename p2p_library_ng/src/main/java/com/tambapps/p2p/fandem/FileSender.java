package com.tambapps.p2p.fandem;

import com.tambapps.p2p.fandem.handshake.FandemReceiverHandshake;
import com.tambapps.p2p.fandem.handshake.FandemSenderHandshake;
import com.tambapps.p2p.fandem.util.TransferListener;
import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.speer.PeerConnection;
import com.tambapps.p2p.speer.ServerPeer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

public class FileSender extends FileSharer {

  private static final int BUFFER_SIZE = 1024;

  private final Peer peer;
  private final int socketTimeout;
  private final AtomicReference<ServerPeer> serverReference = new AtomicReference<>();

  public FileSender(Peer peer) {
    this(peer, null);
  }
  public FileSender(Peer peer, TransferListener listener) {
    this(peer, listener, 0);
  }

  public FileSender(Peer peer, TransferListener listener, int socketTimeout) {
    super(new FandemSenderHandshake(BUFFER_SIZE), listener);
    this.peer = peer;
    this.socketTimeout = socketTimeout;
  }

  public void send(File file) throws IOException {
    try (FileInputStream inputStream = new FileInputStream(file)) {
      send(inputStream, file.getName(), file.length(), () -> computeChecksum(file));
    }
  }

  public void send(FileInputStream inputStream, String fileName, long fileSize,
      Callable<String> checksumSupplier) throws IOException {
    try (ServerPeer server = ServerPeer();
        PeerConnection connection = server.accept()) {
      connection.writeLong(fileSize);
      connection.writeUTF(fileName);
      share(inputStream, connection.getOutputStream(), BUFFER_SIZE, fileSize);
      if (connection.getAttribute(FandemReceiverHandshake.BUFFER_SIZE_KEY)) {
        try {
          connection.writeUTF(checksumSupplier.call());
        } catch (Exception e) {
          throw new IOException(e);
        }
      }
    }
    serverReference.set(null);
  }

  public void cancel() {
    ServerPeer server = serverReference.get();
    if (server != null) {
      try {
        server.close();
      } catch (IOException e) {
        // ignore
      }
      serverReference.set(null);
    }
  }
  private ServerPeer ServerPeer() throws IOException {
    ServerPeer server = new ServerPeer(peer, handshake);
    server.setAcceptTimeout(socketTimeout);
    serverReference.set(server);
    return server;
  }
}
