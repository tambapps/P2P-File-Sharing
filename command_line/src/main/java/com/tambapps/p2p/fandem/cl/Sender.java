package com.tambapps.p2p.fandem.cl;

import com.tambapps.p2p.fandem.Fandem;
import com.tambapps.p2p.fandem.FileSender;
import com.tambapps.p2p.fandem.SenderPeer;
import com.tambapps.p2p.fandem.util.TransferListener;
import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.fandem.cl.command.SendCommand;
import com.tambapps.p2p.fandem.cl.exception.SendingException;
import com.tambapps.p2p.speer.greet.PeerGreeter;
import com.tambapps.p2p.speer.greet.PeerGreeterService;
import com.tambapps.p2p.speer.util.PeerUtils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;

public class Sender implements Closeable {

  private static final String DESKTOP_NAME = getDesktopName();

  private final Peer peer;
  private final PeerGreeterService<SenderPeer> greeterService;

  private final FileSender fileSender;

  private Sender(Peer peer, PeerGreeterService<SenderPeer> greeterService, int timeout, TransferListener listener) {
    this.peer = peer;
    this.greeterService = greeterService;
    this.fileSender = new FileSender(peer, listener, timeout);
  }

  public void send(File file) throws IOException {
    greeterService.getGreeter().getAvailablePeers().clear();
    greeterService.getGreeter().addAvailablePeer(new SenderPeer(peer.getIp(), peer.getPort(), DESKTOP_NAME, file.getName()));
    greeterService.start(Peer.of(peer.getIp(), Fandem.GREETING_PORT));
    fileSender.send(file);
  }

  private static String getDesktopName() {
    String name = System.getenv("COMPUTERNAME");
    if (name != null && !name.isEmpty()) {
      return name;
    }
    name = System.getenv("HOSTNAME");
    if (name != null && !name.isEmpty()) {
      return name;
    }
    try {
      return InetAddress.getLocalHost().getHostName();
    }
    catch (UnknownHostException ex) {
      return System.getProperty("user.name") + " Desktop";
    }
  }

  public static Sender create(ExecutorService executor, SendCommand sendCommand,
      TransferListener listener)
      throws SendingException {

    PeerGreeter<SenderPeer> greeter = new PeerGreeter<>(Fandem.greetings());
    PeerGreeterService<SenderPeer> greeterService = new PeerGreeterService<>(executor, greeter);

    // extract the sender peer
    InetAddress address = sendCommand.getIp()
        .orElseThrow(() ->
            new SendingException("Couldn't get ip address (are you connected to the internet?)"));
    int port = sendCommand.getPort().orElseGet(() -> PeerUtils.getAvailablePort(address, SenderPeer.DEFAULT_PORT));
    return new Sender(Peer.of(address, port), greeterService, sendCommand.getTimeout(), listener);
  }

  public Peer getPeer() {
    return peer;
  }

  @Override
  public void close() {
    greeterService.stop();
  }
}
