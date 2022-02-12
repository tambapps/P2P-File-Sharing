package com.tambapps.p2p.fandem.desktop.service;

import com.tambapps.p2p.fandem.FileSender;
import com.tambapps.p2p.fandem.SenderPeer;
import com.tambapps.p2p.fandem.desktop.controller.TaskViewController;
import com.tambapps.p2p.fandem.desktop.model.SharingTask;
import com.tambapps.p2p.fandem.model.SendingFileData;
import com.tambapps.p2p.fandem.util.TransferListener;
import com.tambapps.p2p.speer.Peer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Service
public class FileSenderService {

  private static final String DESKTOP_NAME = getDesktopName();
  private final ExecutorService executorService;
  private final MulticastSenderPeersService multicastSenderPeersService;

  @Value("${socket.timeout.seconds}")
  private int socketTimeoutSeconds;

  public FileSenderService(@Qualifier("executorService") ExecutorService executorService,
      MulticastSenderPeersService multicastSenderPeersService) {
    this.executorService = executorService;
    this.multicastSenderPeersService = multicastSenderPeersService;
  }

  public SharingTask sendFile(Peer peer, List<File> files, TaskViewController controller) {
    SharingTask sendingTask = new SharingTask(true);
    sendingTask.files = files;
    List<SendingFileData> fileData = new ArrayList<>();
    for (File file : files) {
      try {
        fileData.add(SendingFileData.fromFile(file));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    SenderPeer senderPeer = new SenderPeer(peer.getAddress(), peer.getPort(), DESKTOP_NAME, fileData);
    multicastSenderPeersService.addSenderPeer(senderPeer);
    FileSender fileSender = new FileSender(peer, controller, socketTimeoutSeconds * 1000);

    Future<?> future = executorService.submit(() -> {
      try {
        fileSender.send(fileData);
        controller.onEnd();
      } catch (IOException e) {
        controller.onError(e);
      }
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

  /**
   * Listener that will stop multicast sender peer once the transfer has started
   */
  private class CustomTransferListener implements TransferListener {

    private final TransferListener baseListener;
    private final SenderPeer senderPeer;

    private CustomTransferListener(TransferListener baseListener,
        SenderPeer senderPeer) {
      this.baseListener = baseListener;
      this.senderPeer = senderPeer;
    }

    @Override
    public void onConnected(Peer selfPeer, Peer remotePeer) {
      multicastSenderPeersService.removeSenderPeer(senderPeer);
      baseListener.onConnected(selfPeer, remotePeer);
    }

    @Override
    public void onTransferStarted(String fileName, long fileSize) {
      baseListener.onTransferStarted(fileName, fileSize);
    }

    @Override
    public void onProgressUpdate(String fileName, int progress, long bytesProcessed,
        long totalBytes) {
      baseListener.onProgressUpdate(fileName, progress, bytesProcessed, totalBytes);
    }
  }
}
