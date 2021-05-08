package com.tambapps.p2p.fandem.handshake;

import lombok.AllArgsConstructor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

@AllArgsConstructor
public class FandemSenderHandshake extends FandemHandshake {

  private final int bufferSize;

  public static final String CHECKSUM_KEY = "checksum";

  @Override
  protected void read(Map<String, Object> properties, DataInputStream inputStream) throws IOException {
    properties.put(CHECKSUM_KEY, inputStream.readBoolean());
  }

  @Override
  protected void write(DataOutputStream outputStream) throws IOException {
    outputStream.writeInt(bufferSize);
  }
}
