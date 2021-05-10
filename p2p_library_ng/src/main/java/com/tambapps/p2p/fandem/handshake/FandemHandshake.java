package com.tambapps.p2p.fandem.handshake;

import static com.tambapps.p2p.fandem.Fandem.VERSION;

import com.tambapps.p2p.fandem.exception.IncompatibleVersionException;
import com.tambapps.p2p.speer.exception.HandshakeFailException;
import com.tambapps.p2p.speer.handshake.AbstractAttributeHandshake;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FandemHandshake extends AbstractAttributeHandshake {

  private static final String FANDEM_VERSION_KEY = "fandem_version";

  public FandemHandshake() {
    super(newMap());
  }

  public FandemHandshake(Map<String, Object> properties) {
    super(properties);
  }

  @Override
  public Object apply(DataOutputStream outputStream, DataInputStream inputStream)
      throws IOException {
    writeAttributes(properties, outputStream);
    Map<String, Object> attributes = readAttributes(inputStream);
    validate(attributes);
    return attributes;
  }

  protected void validate(Map<String, Object> properties) throws HandshakeFailException {
    if (!properties.containsKey(FANDEM_VERSION_KEY)) {
      throw new HandshakeFailException("Not a Fandem peer");
    }
    String version = String.valueOf(properties.get(FANDEM_VERSION_KEY));
    String[] fields = version.split("\\.");
    if (fields.length <= 0 || !fields[0].equals(VERSION.split("\\.")[0])) {
      throw new IncompatibleVersionException(String.format("Version %s is not compatible with own version %s",
          version, VERSION));
    }
  }

  static Map<String, Object> newMap(Object... objects) {
    Map<String, Object> map = new HashMap<>();
    map.put(FANDEM_VERSION_KEY, VERSION);
    for (int i = 0; i < objects.length / 2; i++) {
      map.put(String.valueOf(objects[i * 2]), objects[i * 2 + 1]);
    }
    return map;
  }
}
