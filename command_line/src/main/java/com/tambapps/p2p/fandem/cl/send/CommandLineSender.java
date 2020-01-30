package com.tambapps.p2p.fandem.cl.send;

import com.tambapps.p2p.fandem.Peer;
import com.tambapps.p2p.fandem.cl.command.SendCommand;
import com.tambapps.p2p.fandem.cl.exception.SendingException;
import com.tambapps.p2p.fandem.listener.SendingListener;
import com.tambapps.p2p.fandem.sniff.service.PeerSniffHandlerService;
import com.tambapps.p2p.fandem.task.SendingTask;
import com.tambapps.p2p.fandem.util.FileUtils;
import com.tambapps.p2p.fandem.util.IPUtils;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;

public class CommandLineSender implements SendingListener, Closeable {

  private static final String PROGRESS_FORMAT = "\rSent %s / %s";
  private static final String DESKTOP_NAME = getDesktopName();

  private final Peer peer;
  private final PeerSniffHandlerService sniffHandlerService;
  private final int timeout;

  private CommandLineSender(Peer peer, PeerSniffHandlerService sniffHandlerService, int timeout) {
    this.peer = peer;
    this.sniffHandlerService = sniffHandlerService;
    this.timeout = timeout;
    sniffHandlerService.start();
  }

  public void send(File file) throws IOException {
    new SendingTask(this, peer, timeout).send(file);
  }

  @Override
  public void onConnected(@NotNull Peer selfPeer, @NotNull Peer remotePeer, @NotNull String fileName,
      long fileSize) {
    System.out.println("Connected to peer " + remotePeer);
    System.out.format(PROGRESS_FORMAT, "0kb",
        FileUtils.bytesToString(fileSize));
  }

  @Override
  public void onProgressUpdate(int progress, long byteProcessed, long totalBytes) {
    System.out.format(PROGRESS_FORMAT,
        FileUtils.bytesToString(byteProcessed),
        FileUtils.bytesToString(totalBytes));
  }

  @Override
  public void onStart(Peer self, @NotNull String fileName) {
    System.out.println("Sending " + fileName);
    System.out.println("Waiting for a connection on " + self);
    System.out.println("Hex string: " + self.toHexString().toUpperCase());
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

  public static CommandLineSender create(ExecutorService executor, SendCommand sendCommand)
      throws SendingException {
    InetAddress address = sendCommand.getIp()
        .orElseThrow(() ->
            new SendingException("Couldn't get ip address (are you connected to the internet?)"));
    int port  = sendCommand.getPort().orElseGet(() -> IPUtils.getAvailablePort(address));
    Peer peer = Peer.of(address, port);

    PeerSniffHandlerService
        sniffHandlerService = new PeerSniffHandlerService(executor, peer, DESKTOP_NAME);
    return new CommandLineSender(peer, sniffHandlerService, sendCommand.getTimeout());
  }

  @Override
  public void close() {
    sniffHandlerService.stop();
  }
}
