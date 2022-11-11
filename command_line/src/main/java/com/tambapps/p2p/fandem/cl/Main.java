package com.tambapps.p2p.fandem.cl;

import static com.tambapps.p2p.fandem.cl.FandemMode.RECEIVE;
import static com.tambapps.p2p.fandem.cl.FandemMode.SEND;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.tambapps.p2p.fandem.Fandem;
import com.tambapps.p2p.fandem.cl.command.Arguments;
import com.tambapps.p2p.fandem.cl.command.FandemCommand;
import com.tambapps.p2p.fandem.cl.command.SendCommand;
import com.tambapps.p2p.fandem.cl.command.ReceiveCommand;

public class Main {

  public static void main(String[] args) {
    System.out.println("Fandem command-line " + Fandem.VERSION);
    Arguments arguments = new Arguments();
    ReceiveCommand receiveCommand = new ReceiveCommand();
    SendCommand sendCommand = new SendCommand();
    JCommander jCommander = JCommander.newBuilder()
        .addObject(arguments)
        .addCommand(RECEIVE.commandName(), receiveCommand)
        .addCommand(SEND.commandName(), sendCommand)
        .build();

    try {
      jCommander.parse(args);
    } catch (ParameterException e) {
      System.out.println("Error: " + e.getMessage());
      jCommander.usage();
      return;
    }

    if (arguments.getHelp()) {
      jCommander.usage();
      return;
    }

    String command = jCommander.getParsedCommand();
    if (command == null) {
      System.out.println("You must enter a command");
      jCommander.usage();
      return;
    }

    FandemCommand fandemCommand = switch (FandemMode.fromCommandName(command)) {
      case RECEIVE -> receiveCommand;
      case SEND -> sendCommand;
    };
    fandemCommand.execute();
  }

}
