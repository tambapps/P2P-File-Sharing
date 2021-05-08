package com.tambapps.p2p.fandem.handshake;

import lombok.AllArgsConstructor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

@AllArgsConstructor
public class FandemReceiverHandshake extends FandemHandshake {

  private final boolean checksum;

  public static final String BUFFER_SIZE_KEY = "buffer_size";

  @Override
  protected void read(Map<String, Object> properties, DataInputStream inputStream) throws IOException {
    properties.put(BUFFER_SIZE_KEY, inputStream.readInt());
  }

  @Override
  protected void write(DataOutputStream outputStream) throws IOException {
    outputStream.writeBoolean(checksum);
  }
}
