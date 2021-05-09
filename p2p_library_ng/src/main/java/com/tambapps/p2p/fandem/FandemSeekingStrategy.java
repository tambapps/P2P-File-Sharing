package com.tambapps.p2p.fandem;

import com.tambapps.p2p.speer.seek.strategy.LastOctetSeekingStrategy;
import com.tambapps.p2p.speer.util.PeerUtils;

import java.net.InetAddress;

public class FandemSeekingStrategy extends LastOctetSeekingStrategy {

  public FandemSeekingStrategy(String address) {
    this(PeerUtils.getAddress(address));
  }

  public FandemSeekingStrategy(InetAddress address) {
    super(address, Fandem.GREETING_PORT, (byte) 127);
  }

  @Override
  protected byte nextLastOctet() {
    int i = getI();
    if (i % 2 == 0) {
      return (byte) (start - i / 2);
    } else {
      return (byte) (start + i / 2 + 1);
    }
  }
}
