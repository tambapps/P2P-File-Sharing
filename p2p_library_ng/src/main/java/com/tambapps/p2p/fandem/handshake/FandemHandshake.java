package com.tambapps.p2p.fandem.handshake;

import com.tambapps.p2p.fandem.exception.IncompatibleVersionException;
import com.tambapps.p2p.speer.exception.HandshakeFailException;
import com.tambapps.p2p.speer.handshake.Handshake;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class FandemHandshake implements Handshake {

  private static final String VERSION = "2.0";

  @Override
  public Map<String, Object> apply(DataOutputStream outputStream, DataInputStream inputStream)
      throws IOException {
    outputStream.writeUTF("FANDEM");
    outputStream.writeUTF(VERSION);
    write(outputStream);

    if (!inputStream.readUTF().equals("FANDEM")) {
      throw new HandshakeFailException("Not a Fandem peer");
    }
    String version = inputStream.readUTF();
    String[] fields = version.split("\\.");
    if (fields.length <= 0 || !fields[0].equals(VERSION.split("\\.")[0])) {
      throw new IncompatibleVersionException(String.format("Version %s is not compatible with own version %s",
          version, VERSION));
    }
    Map<String, Object> properties = new HashMap<>();
    properties.put("version", version);
    read(properties, inputStream);
    return properties;
  }

  protected abstract void read(Map<String, Object> properties,
      DataInputStream inputStream) throws IOException;

  protected abstract void write(DataOutputStream outputStream) throws IOException;
}
