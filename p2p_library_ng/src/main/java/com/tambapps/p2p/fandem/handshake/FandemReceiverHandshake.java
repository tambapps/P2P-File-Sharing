package com.tambapps.p2p.fandem.handshake;

import com.tambapps.p2p.speer.exception.HandshakeFailException;

import java.util.Map;

public class FandemReceiverHandshake extends FandemHandshake {

  public static final String CHECKSUM_KEY = "checksum";

  public FandemReceiverHandshake(boolean checksum) {
    super(newMap(CHECKSUM_KEY, checksum));
  }

  @Override
  protected void validate(Map<String, Object> properties) throws HandshakeFailException {
    super.validate(properties);
    if (!(properties.get(FandemSenderHandshake.BUFFER_SIZE_KEY) instanceof Integer)) {
      throw new HandshakeFailException("Sender should send buffer_size(integer)");
    }
  }
}
