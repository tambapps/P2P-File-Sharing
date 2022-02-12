package com.tambapps.p2p.fandem;

import com.tambapps.p2p.fandem.model.FileData;
import com.tambapps.p2p.speer.Peer;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.net.InetAddress;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Getter
public class SenderPeer extends Peer {

  public static final int DEFAULT_PORT = 8081;

  String deviceName;
  List<? extends FileData> files;

  public SenderPeer(InetAddress ip, int port, String deviceName, List<? extends FileData> files) {
    super(ip, port);
    this.deviceName = deviceName;
    this.files = files;
  }

  public String peerString() {
    return super.toString();
  }

  public boolean peerEquals(Peer peer) {
    return super.equals(peer);
  }
}
