package com.tambapps.p2p.fandem.cl;

import static com.tambapps.p2p.fandem.cl.FandemCommandLine.RECEIVE_COMMAND;
import static com.tambapps.p2p.fandem.cl.FandemCommandLine.SEND_COMMAND;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.tambapps.p2p.fandem.Fandem;
import com.tambapps.p2p.fandem.cl.command.FandemCommand;
import com.tambapps.p2p.fandem.cl.command.SendCommand;
import com.tambapps.p2p.fandem.cl.command.ReceiveCommand;

import java.util.Map;

@Parameters(commandNames = {SEND_COMMAND, RECEIVE_COMMAND})
public class FandemCommandLine {

  public static final String SEND_COMMAND = "send";
  public static final String RECEIVE_COMMAND = "receive";

  @Parameter(names = {"-h", "--help"}, help = true)
  private boolean help;

  public static void main(String[] args) {
    new FandemCommandLine().run(args);
  }

  public void run(String[] args) {
    System.out.println("Fandem command-line " + Fandem.VERSION);
    Map<String, FandemCommand> commandMap = Map.of(
        SEND_COMMAND, new ReceiveCommand(),
        RECEIVE_COMMAND, new SendCommand()
    );
    JCommander jCommander = newCommander(commandMap);

    try {
      jCommander.parse(args);
    } catch (ParameterException e) {
      System.out.println("Error: " + e.getMessage());
      jCommander.usage();
      return;
    }

    if (help) {
      jCommander.usage();
      return;
    }

    String command = jCommander.getParsedCommand();
    if (command == null) {
      System.out.println("You must enter a command");
      jCommander.usage();
      return;
    }

    FandemCommand fandemCommand = commandMap.get(command);
    fandemCommand.execute();
  }

  private JCommander newCommander(Map<String, FandemCommand> commandMap) {
    JCommander.Builder builder = JCommander.newBuilder();
    commandMap.forEach(builder::addCommand);
    return builder
        .addObject(this)
        .build();
  }
}
