package com.tambapps.p2p.fandem;

import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.speer.util.PeerUtils;
import lombok.Getter;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

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

  public static Peer fromHexString(String hexString) throws UnknownHostException {
    if (hexString.length() != 8 && hexString.length() != 10 ||
        !hexString.chars().allMatch(c -> Character.isDigit(c)
            || c >= 'A' && c <= 'F' ||
            c >= 'a' && c <= 'f')) {
      throw new IllegalArgumentException(String.format("'%s' is malformed", hexString));
    }
    int[] address = new int[4];
    for (int i = 0; i < address.length; i++) {
      address[i] = Integer.parseInt(hexString.substring(i * 2, i * 2 + 2), 16);
    }
    int port = hexString.length() == 8 ? DEFAULT_PORT :
        DEFAULT_PORT + Integer.parseInt(hexString.substring(8, 10), 16);
    return of(InetAddress.getByName(Arrays.stream(address)
        .mapToObj(Integer::toString)
        .collect(Collectors.joining("."))), port);
  }

  public String toHexString() {
    return toHexString(this);
  }

  public static String toHexString(Peer peer) {
    String ipHex = toHexString(peer.getIp());
    return peer.getPort() == DEFAULT_PORT ? ipHex :
        toHexString(peer.getPort() - DEFAULT_PORT);

  }

  public static String toHexString(String address) {
    return toHexString(PeerUtils.getAddress(address));
  }

  /**
   * Returns the hex string of the given ip
   *
   * @param address the address
   * @return the hex string of the given ip
   */
  public static String toHexString(InetAddress address) {
    return Arrays.stream(PeerUtils.toString(address).split("\\."))
        .map(Integer::parseInt)
        .map(SenderPeer::toHexString)
        .collect(Collectors.joining());
  }

  public static String toHexString(int i) {
    String n = Integer.toHexString(i).toUpperCase(Locale.US);
    return n.length() > 1 ? n : "0" + n;
  }
}
