package com.tambapps.p2p.fandem.cl;

public enum Mode {
  SEND {
    @Override public String pastTense() {
      return "Sent";
    }

    @Override public String ingString() {
      return "Sending";
    }
  }, RECEIVE {
    @Override public String pastTense() {
      return "Received";
    }

    @Override public String ingString() {
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
}
