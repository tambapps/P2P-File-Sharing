package com.tambapps.p2p.fandem;

import lombok.Data;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Representation of peer
 */
@Data
public class Peer {

  private final InetAddress ip;
  private final int port;

  public static Peer of(InetAddress address, int port) {
    return new Peer(address, port);
  }

  public static Peer of(Socket socket) {
    return new Peer(socket.getInetAddress(), socket.getPort());
  }

  public static Peer parse(String peer) throws UnknownHostException {
    int index = peer.indexOf(":");
    if (index < 0) {
      throw new IllegalArgumentException("peer is malformed");
    }
    return new Peer(InetAddress.getByName(peer.substring(0, index)), Integer.parseInt(peer.substring(index + 1)));
  }

  public static Peer of(String address, int port) throws UnknownHostException {
    return of(InetAddress.getByName(address), port);
  }

  private Peer(InetAddress ip, int port) {
    this.ip = ip;
    this.port = port;
  }

  @Override
  public String toString() {
    return ip.getHostAddress().replace("/", "") + ":" + port;
  }
}
