package com.tambapps.p2p.fandem;

import com.tambapps.p2p.speer.Peer;
import lombok.Getter;

import java.net.InetAddress;

@Getter
public class SenderPeer extends Peer {

  public static final int DEFAULT_PORT = 8081;

  private final String deviceName;
  private final String fileName;
  private final long fileSize;

  public SenderPeer(InetAddress ip, int port, String deviceName, String fileName, long fileSize) {
    super(ip, port);
    this.deviceName = deviceName;
    this.fileName = fileName;
    this.fileSize = fileSize;
  }

  public String peerString() {
    return super.toString();
  }

}
