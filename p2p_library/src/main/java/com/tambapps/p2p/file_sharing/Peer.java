package com.tambapps.p2p.file_sharing;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Representation of peer
 */
public class Peer {

  private final InetAddress ip;
  private final int port;

  public static Peer of(InetAddress address, int port) {
    return new Peer(address, port);
  }

  public static Peer of(Socket socket) {
    return new Peer(socket.getInetAddress(), socket.getPort());
  }

  public static Peer of(String address, int port) throws UnknownHostException {
    return of(InetAddress.getByName(address), port);
  }

  private Peer(InetAddress ip, int port) {
    this.ip = ip;
    this.port = port;
  }

  public InetAddress getIp() {
    return ip;
  }

  public int getPort() {
    return port;
  }

  public static Peer parse(String peer) throws UnknownHostException {
    int index = peer.indexOf(":");
    if (index < 0) {
      throw new IllegalArgumentException("peer is malformed");
    }
    return new Peer(InetAddress.getByName(peer.substring(0, index)), Integer.parseInt(peer.substring(index + 1)));
  }

  @Override
  public String toString() {
    return ip.getHostName() + ":" + port;
  }
}
