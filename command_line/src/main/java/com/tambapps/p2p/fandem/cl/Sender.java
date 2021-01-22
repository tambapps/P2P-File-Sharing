package com.tambapps.p2p.fandem.cl;

import com.tambapps.p2p.fandem.Peer;
import com.tambapps.p2p.fandem.cl.command.SendCommand;
import com.tambapps.p2p.fandem.cl.exception.SendingException;
import com.tambapps.p2p.fandem.listener.SendingListener;
import com.tambapps.p2p.fandem.sniff.service.PeerSniffHandlerService;
import com.tambapps.p2p.fandem.task.SendingTask;
import com.tambapps.p2p.fandem.util.IPUtils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;

public class Sender implements Closeable {

  private static final String DESKTOP_NAME = getDesktopName();

  private final Peer peer;
  private final PeerSniffHandlerService sniffHandlerService;
  private final int timeout;
  private final SendingListener listener;

  private Sender(Peer peer, PeerSniffHandlerService sniffHandlerService, int timeout,
      SendingListener listener) {
    this.peer = peer;
    this.sniffHandlerService = sniffHandlerService;
    this.timeout = timeout;
    this.listener = listener;
    sniffHandlerService.start();
  }

  public void send(File file) throws IOException {
    sniffHandlerService.setFileName(file.getName());
    new SendingTask(listener, peer, timeout).send(file);
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
      return  System.getProperty("user.name") + " Desktop";
    }
  }

  public static Sender create(ExecutorService executor, SendCommand sendCommand,
      SendingListener listener)
      throws SendingException {
    InetAddress address = sendCommand.getIp()
        .orElseThrow(() ->
            new SendingException("Couldn't get ip address (are you connected to the internet?)"));
    int port  = sendCommand.getPort().orElseGet(() -> IPUtils.getAvailablePort(address));
    Peer peer = Peer.of(address, port);

    PeerSniffHandlerService
        sniffHandlerService = new PeerSniffHandlerService(executor, peer, DESKTOP_NAME);
    return new Sender(peer, sniffHandlerService, sendCommand.getTimeout(), listener);
  }

  @Override
  public void close() {
    sniffHandlerService.stop();
  }
}
