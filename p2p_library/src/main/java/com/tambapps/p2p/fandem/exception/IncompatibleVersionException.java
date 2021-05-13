package com.tambapps.p2p.fandem.exception;

import com.tambapps.p2p.speer.exception.HandshakeFailException;

public class IncompatibleVersionException extends HandshakeFailException {
  public IncompatibleVersionException(String message) {
    super(message);
  }
}
