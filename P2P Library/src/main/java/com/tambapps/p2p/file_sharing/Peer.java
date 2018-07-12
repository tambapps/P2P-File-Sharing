package com.tambapps.p2p.file_sharing;

public class Peer {
  private String ip;
  private int port;

  public Peer(String ip, int port) {
    this.ip = ip;
    this.port = port;
  }

  public String getIp() {
    return ip;
  }

  public int getPort() {
    return port;
  }

  public static Peer parse(String peer) {
    int index = peer.indexOf(":");
    if (index < 0) {
      throw new IllegalArgumentException("peer is malformed");
    }
    return new Peer(peer.substring(0, index), Integer.parseInt(peer.substring(index + 1)));
  }

  @Override
  public String toString() {
    return ip + ":" + port;
  }
}
