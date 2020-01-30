package com.tambapps.p2p.fandem.cl.send;


import com.tambapps.p2p.fandem.Peer;
import com.tambapps.p2p.fandem.cl.command.SendCommand;
import com.tambapps.p2p.fandem.listener.SendingListener;
import com.tambapps.p2p.fandem.sniff.service.PeerSniffHandlerService;
import com.tambapps.p2p.fandem.task.SendingTask;
import com.tambapps.p2p.fandem.util.FileUtils;
import com.tambapps.p2p.fandem.util.IPUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

public class CommandLineSender implements SendingListener {

  private static final String PROGRESS_FORMAT = "\rSent %s / %s";
  private static final String DESKTOP_NAME = getDesktopName();

  private final ExecutorService executorService;

  public CommandLineSender(ExecutorService executorService) {
    this.executorService = executorService;
  }

  public void send(SendCommand sendCommand) {
    InetAddress address;
    if (sendCommand.getIp() == null) {
      try {
        address = Objects.requireNonNull(IPUtils.getIpAddress());
      } catch (Exception e) {
        System.out.println("Couldn't get ip address (are you connected to the internet?)");
        return;
      }
    } else {
      try {
        address = InetAddress.getByName(sendCommand.getIp());
      } catch (UnknownHostException e) {
        System.out.println("Couldn't get ip address (is it well formatted?)");
        return;
      }
    }

    Integer port  = sendCommand.getPort();
    if (port == null) {
      port = IPUtils.getAvailablePort(address);
    }
    Peer peer = Peer.of(address, port);
    final PeerSniffHandlerService
        sniffHandlerService = new PeerSniffHandlerService(executorService, peer, DESKTOP_NAME);
    sniffHandlerService.start();
    for (String filePath : sendCommand.getFilePath()) {
      getFileFromPath(filePath)
          .ifPresent(file -> sendFile(file, peer, sendCommand.getTimeout(), sniffHandlerService));
    }
    sniffHandlerService.stop();
  }

  private Optional<File> getFileFromPath(String filePath) {
    File file;
    try {
      file = new File(decodePath(filePath));
    } catch (UnsupportedEncodingException e) {
      System.out.println("Couldn't decode path " + filePath);
      return Optional.empty();
    }
    if (!file.exists()) {
      System.out.format("This file doesn't exist (%s)", filePath).println();
      return Optional.empty();
    }
    if (!file.isFile()) {
      System.out.format("This isn't a file (%s)", filePath).println();
      return Optional.empty();
    }
    return Optional.of(file);
  }

  private void sendFile(File file, Peer peer, int timeout,
      PeerSniffHandlerService sniffHandlerService) {
    sniffHandlerService.setFileName(file.getName());
    try {
      new SendingTask(this, peer, timeout).send(file);
      System.out.println();
      System.out.println(file.getName() + " was successfully sent");
      System.out.println();
    } catch (IOException e) {
      System.out.println();
      System.out.format("Error while sending %s: %s",file.getName(), e.getMessage()).println();
    }
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

  private String decodePath(String path) throws UnsupportedEncodingException {
    return URLDecoder.decode(path, StandardCharsets.UTF_8.name());
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
}
