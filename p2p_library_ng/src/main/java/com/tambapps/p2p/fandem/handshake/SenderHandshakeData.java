package com.tambapps.p2p.fandem.handshake;

import lombok.Value;

import java.util.Map;

@Value
public class SenderHandshakeData {

  public static SenderHandshakeData from(Map<String, Object> properties) {
    return new SenderHandshakeData((String) properties.get(FandemSenderHandshake.FILE_NAME_KEY),
        (long) properties.get(FandemSenderHandshake.FILE_SIZE_KEY));
  }

  String fileName;
  long fileSize;
}
