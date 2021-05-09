package com.tambapps.p2p.fandem;

import com.tambapps.p2p.speer.Peer;
import lombok.Getter;

import java.net.InetAddress;

@Getter
public class SenderPeer extends Peer {

  public static final int DEFAULT_PORT = 8081;

  private final String deviceName;
  private final String fileName;

  public SenderPeer(InetAddress ip, int port, String deviceName, String fileName) {
    super(ip, port);
    this.deviceName = deviceName;
    this.fileName = fileName;
  }

  public String peerString() {
    return super.toString();
  }

}
