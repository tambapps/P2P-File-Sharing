package com.tambapps.p2p.fandem.handshake;

import com.tambapps.p2p.speer.exception.HandshakeFailException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

public class FandemReceiverHandshake extends FandemHandshake {

  public static final String SEND_CHECKSUM_KEY = "send_checksum";

  private final boolean checksum;

  public FandemReceiverHandshake(boolean checksum) {
    super(newMap(SEND_CHECKSUM_KEY, checksum));
    this.checksum = checksum;
  }

  @Override
  public Object apply(DataOutputStream outputStream, DataInputStream inputStream)
      throws IOException {
    writeAttributes(properties, outputStream);
    Map<String, Object> attributes = readAttributes(inputStream);
    validate(attributes);
    return SenderHandshakeData.from(attributes);
  }

  @Override
  protected void validate(Map<String, Object> properties) throws HandshakeFailException {
    super.validate(properties);
    if (!(properties.get(FandemSenderHandshake.FILE_NAME_KEY) instanceof String)) {
      throw new HandshakeFailException("Sender should send file_name (string)");
    }
    if (!(properties.get(FandemSenderHandshake.FILE_SIZE_KEY) instanceof Long)) {
      throw new HandshakeFailException("Sender should send file_size (long)");
    }

    if (checksum &&
    !(properties.get(FandemSenderHandshake.CHECKSUM_KEY) instanceof String)) {
      throw new HandshakeFailException("Sender should send checksum (string)");
    }

    if (!(properties.get(FandemSenderHandshake.FILE_SIZE_KEY) instanceof Long)) {
      throw new HandshakeFailException("Sender should send file_size (long)");
    }
  }
}
