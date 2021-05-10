package com.tambapps.p2p.fandem.handshake;

import lombok.Value;

import java.util.Map;
import java.util.Optional;

@Value
public class SenderHandshakeData {

  String fileName;
  long fileSize;
  // nullable
  Optional<String> checksum;

  public static SenderHandshakeData from(Map<String, Object> properties) {
    return new SenderHandshakeData((String) properties.get(FandemSenderHandshake.FILE_NAME_KEY),
        (long) properties.get(FandemSenderHandshake.FILE_SIZE_KEY),
        Optional.ofNullable((String) properties.get(FandemSenderHandshake.CHECKSUM_KEY)));
  }
}
