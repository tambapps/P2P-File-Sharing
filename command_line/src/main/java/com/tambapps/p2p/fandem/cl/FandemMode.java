package com.tambapps.p2p.fandem.cl;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

import static com.tambapps.p2p.fandem.cl.FandemCommandLine.RECEIVE_COMMAND;
import static com.tambapps.p2p.fandem.cl.FandemCommandLine.SEND_COMMAND;

@AllArgsConstructor
public enum FandemMode {
  SEND(SEND_COMMAND), RECEIVE(RECEIVE_COMMAND);

  @Getter
  private final String commandName;

  public static FandemMode fromCommandName(String commandName) {
    return Arrays.stream(values())
        .filter(c -> c.getCommandName().equals(commandName))
        .findFirst()
        .get();
  }
}
