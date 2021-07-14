package com.tambapps.p2p.fandem.exception;

import com.tambapps.p2p.speer.exception.HandshakeFailException;

public class IncompatibleVersionException extends HandshakeFailException {
  public IncompatibleVersionException() {
    super("Fandem version of other peer is not compatible with yours");
  }
}
