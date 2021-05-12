package com.tambapps.p2p.fandem.cl;

import com.tambapps.p2p.fandem.Fandem;
import com.tambapps.p2p.fandem.FileSender;
import com.tambapps.p2p.fandem.SenderPeer;
import com.tambapps.p2p.fandem.util.TransferListener;
import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.fandem.cl.command.SendCommand;
import com.tambapps.p2p.fandem.cl.exception.SendingException;
import com.tambapps.p2p.speer.datagram.service.PeriodicMulticastService;
import com.tambapps.p2p.speer.util.PeerUtils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

public class Sender implements Closeable {

  private static final String DESKTOP_NAME = getDesktopName();

  private final Peer peer;
  private final PeriodicMulticastService<List<SenderPeer>> greeterService;

  private final FileSender fileSender;

  private Sender(Peer peer, PeriodicMulticastService<List<SenderPeer>> greeterService, int timeout, TransferListener listener) {
    this.peer = peer;
    this.greeterService = greeterService;
    this.fileSender = new FileSender(peer, listener, timeout);
  }

  public void send(File file) throws IOException {
    greeterService.setData(List.of(new SenderPeer(peer.getAddress(), peer.getPort(), DESKTOP_NAME, file.getName(), file.length())));
    greeterService.start(1000L);
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

  public static Sender create(ScheduledExecutorService executor, SendCommand sendCommand,
      TransferListener listener)
      throws SendingException {
    // extract the sender peer
    InetAddress address = sendCommand.getIp()
        .orElseThrow(() ->
            new SendingException("Couldn't get ip address (are you connected to the internet?)"));
    int port = sendCommand.getPort().orElseGet(() -> PeerUtils.getAvailablePort(address, SenderPeer.DEFAULT_PORT));
    return new Sender(Peer.of(address, port), Fandem.multicastService(executor), sendCommand.getTimeout(), listener);
  }

  public Peer getPeer() {
    return peer;
  }

  @Override
  public void close() {
    greeterService.stop();
  }
}
