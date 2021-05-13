package com.tambapps.p2p.fandem.fandemdesktop.service;

import com.tambapps.p2p.fandem.SenderPeer;
import com.tambapps.p2p.speer.datagram.service.PeriodicMulticastService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class MulticastSenderPeersService {

  private final PeriodicMulticastService<List<SenderPeer>> multicastService;
  private final List<SenderPeer> senderPeers;

  public MulticastSenderPeersService(PeriodicMulticastService<List<SenderPeer>> multicastService) {
    this.multicastService = multicastService;
    this.senderPeers = multicastService.getData();
  }

  public void addSenderPeer(SenderPeer senderPeer) {
    senderPeers.add(senderPeer);
    if (!senderPeers.isEmpty() && !multicastService.isRunning()) {
      try {
        multicastService.start(1L);
      } catch (IOException e) {
        throw new RuntimeException("Couldn't start multicast service", e);
      }
    }
  }

  public void removeSenderPeer(SenderPeer senderPeer) {
    senderPeers.remove(senderPeer);
    if (senderPeers.isEmpty() && multicastService.isRunning()) {
      multicastService.stop();
    }
  }
}
