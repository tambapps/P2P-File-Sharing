package com.tambapps.p2p.fandem;

import static com.tambapps.p2p.fandem.SenderPeer.DEFAULT_PORT;

import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.speer.datagram.DatagramSupplier;
import com.tambapps.p2p.speer.datagram.MulticastDatagramPeer;
import com.tambapps.p2p.speer.datagram.service.PeriodicMulticastService;
import com.tambapps.p2p.speer.util.Deserializer;
import com.tambapps.p2p.speer.util.PeerUtils;
import com.tambapps.p2p.speer.util.Serializer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

public final class Fandem {

  public static final InetAddress PEER_DISCOVERY_MULTICAST_ADDRESS = PeerUtils.getAddress("230.0.8.8");
  public static final int PEER_DISCOVERY_PORT = 50000;

  public static final String VERSION = "2.0";

  private Fandem() {
  }

  public static PeriodicMulticastService<List<SenderPeer>> multicastService(
      ScheduledExecutorService executor) {
    return new PeriodicMulticastService<>(executor, PEER_DISCOVERY_MULTICAST_ADDRESS, PEER_DISCOVERY_PORT, senderPeersSerializer());
  }

  public static DatagramSupplier<List<SenderPeer>> senderPeersSupplier() throws IOException {
    return new DatagramSupplier<>(multicastReceiverDatagram(), senderPeersDeserializer());
  }

  public static MulticastDatagramPeer multicastReceiverDatagram() throws IOException {
    MulticastDatagramPeer datagramPeer = new MulticastDatagramPeer(PEER_DISCOVERY_PORT);
    datagramPeer.joinGroup(PEER_DISCOVERY_MULTICAST_ADDRESS);
    return datagramPeer;
  }

  public static Serializer<List<SenderPeer>> senderPeersSerializer() {
    return (peers, os) -> {
      try (DataOutputStream outputStream = new DataOutputStream(os)) {
        outputStream.writeInt(peers.size());
        for (SenderPeer peer : peers) {
          outputStream.writeUTF(peer.peerString());
          outputStream.writeUTF(peer.getDeviceName());
          outputStream.writeUTF(peer.getFileName());
          outputStream.writeLong(peer.getFileSize());
        }
      }
    };
  }
  public static Deserializer<List<SenderPeer>> senderPeersDeserializer() {
    return is -> {
      try (DataInputStream inputStream = new DataInputStream(is)){
        int count = inputStream.readInt();
        List<SenderPeer> senderPeers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
          Peer peer = Peer.parse(inputStream.readUTF());
          String deviceName = inputStream.readUTF();
          String fileName = inputStream.readUTF();
          long fileSize = inputStream.readLong();
          senderPeers
              .add(new SenderPeer(peer.getAddress(), peer.getPort(), deviceName, fileName, fileSize));
        }
        return senderPeers;
      }
    };
  }

  public static Peer findAvailableSendingPeer() throws IOException {
    return Peer.findAvailablePeer(SenderPeer.DEFAULT_PORT);
  }

  public static Peer parsePeerFromHexString(String hexString) {
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
    return Peer.of(PeerUtils.getAddress(Arrays.stream(address)
        .mapToObj(Integer::toString)
        .collect(Collectors.joining("."))), port);
  }

  public static String toHexString(Peer peer) {
    String ipHex = toHexString(peer.getAddress());
    return peer.getPort() == DEFAULT_PORT ? ipHex : ipHex +
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
        .map(Fandem::toHexString)
        .collect(Collectors.joining());
  }

  public static boolean isCorrectPeerKey(String peerKey) {
    if (peerKey == null) {
      return false;
    }
    peerKey = peerKey.toUpperCase(Locale.US);
    if (peerKey.length() != 8 && peerKey.length() != 10) {
      return false;
    }
    for (int i = 0; i < peerKey.length(); i++) {
      char c = peerKey.charAt(i);
      if (!(c >= 'A' && c <= 'F' || c >= '0' && c <= '9')) {
        return false;
      }
    }
    return true;
  }

  public static String toHexString(int i) {
    String n = Integer.toHexString(i).toUpperCase(Locale.US);
    return n.length() > 1 ? n : "0" + n;
  }
}
