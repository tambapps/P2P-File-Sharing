package com.tambapps.p2p.fandem;

import static com.tambapps.p2p.fandem.SenderPeer.DEFAULT_PORT;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.tambapps.p2p.fandem.exception.IncompatibleVersionException;
import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.speer.datagram.DatagramSupplier;
import com.tambapps.p2p.speer.datagram.MulticastDatagramPeer;
import com.tambapps.p2p.speer.datagram.service.MulticastReceiverService;
import com.tambapps.p2p.speer.datagram.service.PeriodicMulticastService;
import com.tambapps.p2p.speer.io.Deserializer;
import com.tambapps.p2p.speer.io.Serializer;
import com.tambapps.p2p.speer.util.PeerUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

public final class Fandem {

  // using IPv6 because android multicast packet receiving doesn't work for IPv4 multicast addresses
  // (well at least I couldn't make it work) but it does work with an IPv6 multicast address
  public static final InetAddress PEER_DISCOVERY_MULTICAST_ADDRESS = PeerUtils.getAddress("ff02::1");
  public static final int PEER_DISCOVERY_PORT = 50000;
  private static final Gson GSON = new Gson();

  private static final Serializer<?> SERIALIZER = (object, outputStream) -> {
    try {
      DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
      dataOutputStream.writeUTF(GSON.toJson(object));
      dataOutputStream.flush();
    } catch (JsonIOException e) {
      throw new IOException(e);
    }
  };
  public static final String VERSION = "3.0";
  public static final int[] VERSION_FIELDS = Arrays.stream(VERSION.split("\\."))
      .mapToInt(Integer::parseInt)
      .toArray();

  private Fandem() {
  }

  public static  <T> Deserializer<T> deserializer(Class<T> clazz) {
    return inputStream -> {
      try {
        return GSON.fromJson(new DataInputStream(inputStream).readUTF(), clazz);
      } catch (JsonIOException| JsonSyntaxException e) {
        throw new IOException(e);
      }
    };
  }

  public static <T> Serializer<T> serializer() {
    return (Serializer<T>) SERIALIZER;
  }


  public static PeriodicMulticastService<List<SenderPeer>> multicastService(
      ScheduledExecutorService executor) {
    return new PeriodicMulticastService<>(executor, PEER_DISCOVERY_MULTICAST_ADDRESS, PEER_DISCOVERY_PORT, senderPeersSerializer(), new ArrayList<>());
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
    return serializer();
  }

  public static Deserializer<List<SenderPeer>> senderPeersDeserializer() {
    final Type type = new TypeToken<List<SenderPeer>>(){}.getType();
    return is -> {
      List<SenderPeer> senderPeers = GSON.fromJson(new DataInputStream(is).readUTF(), type);
      // filter own sender peers (if multicasting any)
      InetAddress address = PeerUtils.getPrivateNetworkIpAddressOrNull();
      return senderPeers.stream()
          .filter(p -> !p.getAddress().equals(address))
          .collect(Collectors.toList());
    };
  }

  public static MulticastReceiverService<List<SenderPeer>> senderPeersReceiverService(
      ExecutorService executorService,
      MulticastReceiverService.DiscoveryListener<List<SenderPeer>> listener) {
    return new MulticastReceiverService<>(executorService, PEER_DISCOVERY_MULTICAST_ADDRESS,
        PEER_DISCOVERY_PORT, senderPeersDeserializer(), listener);
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

  public static void checkVersionCompatibility(String version) throws IncompatibleVersionException {
    String[] fieldsString = version.split("\\.");
    if (fieldsString.length != 2) {
      throw new IncompatibleVersionException();
    }
    int[] fields = new int[2];
    for (int i = 0; i < fields.length; i++) {
      try {
        fields[i] = Integer.parseInt(fieldsString[i]);
      } catch (NumberFormatException e) {
        throw new IncompatibleVersionException();
      }
    }
    if (VERSION_FIELDS[0] != fields[0]) {
      throw new IncompatibleVersionException();
    }
  }
}
