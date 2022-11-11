package com.tambapps.p2p.fandem.cl;

import java.util.Locale;

public enum FandemMode {
  SEND {
    @Override
    public String pastTense() {
      return "Sent";
    }

    @Override
    public String ingString() {
      return "Sending";
    }
  }, RECEIVE {
    @Override
    public String pastTense() {
      return "Received";
    }

    @Override
    public String ingString() {
      return "Receiving";
    }
  };

  // couldn't find a better name
  public abstract String ingString();

  public abstract String pastTense();

  public String progressFormat() {
    return "\r" + pastTense() + " %s / %s";
  }

  public String commandName() {
    return name().toLowerCase();
  }

  public static FandemMode fromCommandName(String name) {
    return valueOf(name.toUpperCase(Locale.ENGLISH));
  }
}
