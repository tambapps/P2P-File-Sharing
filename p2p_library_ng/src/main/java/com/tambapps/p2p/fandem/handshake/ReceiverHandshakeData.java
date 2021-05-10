package com.tambapps.p2p.fandem.handshake;

import lombok.Value;

import java.util.Map;

@Value
public class ReceiverHandshakeData {

  public static ReceiverHandshakeData from(Map<String, Object> properties) {
    return new ReceiverHandshakeData((boolean) properties.get(FandemReceiverHandshake.SEND_CHECKSUM_KEY));
  }
  boolean sendChecksum;
}
