package com.tambapps.p2p.fandem;

import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.speer.greet.PeerGreetings;
import com.tambapps.p2p.speer.seek.PeerSeeking;

import java.util.ArrayList;
import java.util.List;

public final class Fandem {

  private Fandem() {}

  public static final String VERSION = "2.0";

  public static final int GREETING_PORT = 50000;

  public static PeerSeeking<SenderPeer> seeking() {
    return inputStream ->  {
      int count = inputStream.readInt();
      List<SenderPeer> senderPeers = new ArrayList<>();
      for (int i = 0; i < count; i++) {
        Peer peer = Peer.parse(inputStream.readUTF());
        String deviceName = inputStream.readUTF();
        String fileName = inputStream.readUTF();
        senderPeers.add(new SenderPeer(peer.getIp(), peer.getPort(), deviceName, fileName));
      }
      return senderPeers;
    };
  }

  public static PeerGreetings<SenderPeer> greetings() {
    return (peers, outputStream) -> {
      outputStream.writeInt(peers.size());
      for (SenderPeer peer : peers) {
        outputStream.writeUTF(peer.peerString());
        outputStream.writeUTF(peer.getDeviceName());
        outputStream.writeUTF(peer.getFileName());
      }
    };
  }
}
