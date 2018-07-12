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
}
