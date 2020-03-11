package com.tambapps.p2p.fandem.cl;

public enum Mode {
  SEND {
    @Override String pastTense() {
      return "Sent";
    }
  }, RECEIVE {
    @Override String pastTense() {
      return "Received";
    }
  };

  // couldn't find a better name
  String ingString() {
    String name = name();
    return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
  }

  abstract String pastTense();

  String progressFormat() {
    return "\r" + pastTense() + " %s / %s";
  }

  String commandName() {
    return name().toLowerCase();
  }
}
