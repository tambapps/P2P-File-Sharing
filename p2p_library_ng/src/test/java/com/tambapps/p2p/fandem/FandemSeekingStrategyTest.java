package com.tambapps.p2p.fandem;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tambapps.p2p.speer.Peer;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class FandemSeekingStrategyTest {

  @Test
  public void test() {
    InetAddress address = InetAddress.getLoopbackAddress();
    FandemSeekingStrategy strategy = new FandemSeekingStrategy(address);

    List<Peer> peers = new ArrayList<>();
    for (Peer peer : strategy) {
      peers.add(peer);
      System.out.println(peer);
    }
    // this strategy should return unique peers
    assertEquals(peers.stream().distinct().count(), peers.size());
    // when resetting the result returned should be the same
    strategy.reset();

  }

}
