package com.tambapps.p2p.fandem.desktop.service;

import com.tambapps.p2p.fandem.FileSender;
import com.tambapps.p2p.fandem.SenderPeer;
import com.tambapps.p2p.fandem.desktop.controller.TaskViewController;
import com.tambapps.p2p.fandem.desktop.model.SharingTask;
import com.tambapps.p2p.speer.Peer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Service
public class FileSenderService {

  private static final String DESKTOP_NAME = getDesktopName();
  private final ExecutorService executorService;
  private final MulticastSenderPeersService multicastSenderPeersService;

  @Value("${socket.timeout.seconds}")
  private int socketTimeoutSeconds;

  public FileSenderService(ExecutorService executorService,
      MulticastSenderPeersService multicastSenderPeersService) {
    this.executorService = executorService;
    this.multicastSenderPeersService = multicastSenderPeersService;
  }

  public SharingTask sendFile(Peer peer, File file, TaskViewController controller) {
    SharingTask sendingTask = new SharingTask(true);
    sendingTask.file = file;
    SenderPeer senderPeer = new SenderPeer(peer.getAddress(), peer.getPort(), DESKTOP_NAME, file.getName(), file.length());
    multicastSenderPeersService.addSenderPeer(senderPeer);
    FileSender fileSender = new FileSender(peer, controller, socketTimeoutSeconds * 1000);

    Future<?> future = executorService.submit(() -> {
      try {
        fileSender.send(file);
      } catch (IOException e) {
        controller.onError(e);
      }
      multicastSenderPeersService.removeSenderPeer(senderPeer);
    });

    sendingTask.setCanceler(() -> {
      future.cancel(true);
      multicastSenderPeersService.removeSenderPeer(senderPeer);
    });
    return sendingTask;
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
}
