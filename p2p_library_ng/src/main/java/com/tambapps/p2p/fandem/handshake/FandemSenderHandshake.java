package com.tambapps.p2p.fandem.handshake;

import com.tambapps.p2p.speer.exception.HandshakeFailException;

import java.util.Map;

public class FandemSenderHandshake extends FandemHandshake {

  public static final String BUFFER_SIZE_KEY = "buffer_size";

  public FandemSenderHandshake(int bufferSize) {
    super(newMap(BUFFER_SIZE_KEY, bufferSize));
  }

  @Override
  protected void validate(Map<String, Object> properties) throws HandshakeFailException {
    super.validate(properties);
    if (!(properties.get(FandemReceiverHandshake.CHECKSUM_KEY) instanceof Boolean)) {
      throw new HandshakeFailException("Sender should send checksum(boolean)");
    }
  }
}
