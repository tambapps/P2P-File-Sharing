package com.tambapps.p2p.fandem.handshake;

import com.tambapps.p2p.speer.exception.HandshakeFailException;

import java.util.Map;

public class FandemSenderHandshake extends FandemHandshake {

  public static final String FILE_NAME_KEY = "file_name";
  public static final String FILE_SIZE_KEY = "file_size";

  public FandemSenderHandshake(String fileName, long fileSize) {
    super(newMap(FILE_NAME_KEY, fileName, FILE_SIZE_KEY, fileSize));
  }

  @Override
  protected void validate(Map<String, Object> properties) throws HandshakeFailException {
    super.validate(properties);
    if (!(properties.get(FandemReceiverHandshake.SEND_CHECKSUM_KEY) instanceof Boolean)) {
      throw new HandshakeFailException("Sender should send checksum (boolean)");
    }
  }

  @Override
  protected Object map(Map<String, Object> properties) {
    return ReceiverHandshakeData.from(properties);
  }
}
