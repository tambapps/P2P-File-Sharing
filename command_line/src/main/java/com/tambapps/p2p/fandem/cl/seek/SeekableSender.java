package com.tambapps.p2p.fandem.cl.seek;

import com.tambapps.p2p.fandem.Fandem;
import com.tambapps.p2p.fandem.FileSender;
import com.tambapps.p2p.fandem.SenderPeer;
import com.tambapps.p2p.fandem.model.SendingFileData;
import com.tambapps.p2p.fandem.util.TransferListener;
import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.speer.datagram.service.PeriodicMulticastService;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

/**
 * Class allowing to send files by starting a ServerSocket. It also UDP multicasts the server's port and address,
 * allowing this sender to be seekable.
 */
public class SeekableSender implements Closeable {

  private final PeriodicMulticastService<List<SenderPeer>> greeterService;
  private final FileSender fileSender;

  private SeekableSender(FileSender fileSender, PeriodicMulticastService<List<SenderPeer>> greeterService) {
    this.fileSender = fileSender;
    this.greeterService = greeterService;
  }

  public static SeekableSender create(InetAddress address, int port, int timeout, TransferListener listener) {
    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    return new SeekableSender(new FileSender(Peer.of(address, port), listener, timeout), Fandem.multicastService(executor));
  }

  /**
   * Starts the ServerSocket to send files and multicasts data about server and files
   *
   * @param files the files to send
   * @throws IOException in case of I/O error
   */
  public void sendFiles(List<File> files) throws IOException {
    List<SendingFileData> fileData = files.stream()
        .map(SendingFileData::fromFile)
        .collect(Collectors.toList());

    greeterService.setData(List.of(SenderPeer.of(getPeer(), getDesktopName(), fileData)));
    greeterService.start(1000L);
    fileSender.sendFiles(files);
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
    } catch (UnknownHostException ex) {
      return System.getProperty("user.name") + " Desktop";
    }
  }

  public Peer getPeer() {
    return fileSender.getPeer();
  }

  @Override
  public void close() {
    greeterService.stop(true);
  }
}
