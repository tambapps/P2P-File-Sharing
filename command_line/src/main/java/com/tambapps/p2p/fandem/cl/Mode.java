package com.tambapps.p2p.fandem.cl;

public enum Mode {
  SEND {
    @Override String pastTense() {
      return "Sent";
    }

    @Override String ingString() {
      return "Sending";
    }
  }, RECEIVE {
    @Override String pastTense() {
      return "Received";
    }

    @Override String ingString() {
      return "Receiving";
    }
  };

  // couldn't find a better name
  abstract String ingString();

  abstract String pastTense();

  String progressFormat() {
    return "\r" + pastTense() + " %s / %s";
  }

  String commandName() {
    return name().toLowerCase();
  }
}
