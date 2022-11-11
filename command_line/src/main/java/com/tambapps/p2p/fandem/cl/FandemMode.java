package com.tambapps.p2p.fandem.cl;

import java.util.Locale;

public enum FandemMode {
  SEND, RECEIVE;

  public String commandName() {
    return name().toLowerCase();
  }

  public static FandemMode fromCommandName(String name) {
    return valueOf(name.toUpperCase(Locale.ENGLISH));
  }
}
