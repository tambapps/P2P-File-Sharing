package com.tambapps.p2p.fandem.handshake;

import com.tambapps.p2p.speer.exception.HandshakeFailException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;

public class FandemSenderHandshake extends FandemHandshake {

  public static final String FILE_NAME_KEY = "file_name";
  public static final String FILE_SIZE_KEY = "file_size";
  public static final String CHECKSUM_KEY = "checksum";

  private final Callable<String> checksumSupplier;

  public FandemSenderHandshake(String fileName, long fileSize,
      Callable<String> checksumSupplier) {
    super(newMap(FILE_NAME_KEY, fileName, FILE_SIZE_KEY, fileSize));
    this.checksumSupplier = checksumSupplier;
  }

  @Override
  public ReceiverHandshakeData apply(DataOutputStream outputStream, DataInputStream inputStream)
      throws IOException {
    Map<String, Object> attributes = readAttributes(inputStream);
    validate(attributes);
    ReceiverHandshakeData data = ReceiverHandshakeData.from(attributes);
    if (data.isSendChecksum()) {
      properties.put(CHECKSUM_KEY, getChecksum());
    }
    writeAttributes(properties, outputStream);
    return data;
  }

  private String getChecksum() throws IOException {
    try {
      return checksumSupplier.call();
    } catch (Exception e) {
      throw new IOException("Couldn't compute checksum", e);
    }
  }
  @Override
  protected void validate(Map<String, Object> properties) throws HandshakeFailException {
    super.validate(properties);
    if (!(properties.get(FandemReceiverHandshake.SEND_CHECKSUM_KEY) instanceof Boolean)) {
      throw new HandshakeFailException("Sender should send checksum (boolean)");
    }
  }

}
