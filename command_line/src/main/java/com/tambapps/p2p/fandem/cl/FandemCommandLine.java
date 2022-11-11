package com.tambapps.p2p.fandem.cl;

import static com.tambapps.p2p.fandem.cl.FandemCommandLine.RECEIVE_COMMAND;
import static com.tambapps.p2p.fandem.cl.FandemCommandLine.SEND_COMMAND;
import static com.tambapps.p2p.fandem.cl.FandemMode.RECEIVE;
import static com.tambapps.p2p.fandem.cl.FandemMode.SEND;

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

  private final Map<FandemMode, FandemCommand> commandMap = Map.of(
      RECEIVE, new ReceiveCommand(),
      SEND, new SendCommand()
  );

  @Parameter(names = {"-h", "--help"}, help = true)
  private boolean help;

  public static void main(String[] args) {
    new FandemCommandLine().run(args);
  }

  public void run(String[] args) {
    System.out.println("Fandem command-line " + Fandem.VERSION);
    JCommander jCommander = newCommander();

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

    FandemCommand fandemCommand = commandMap.get(FandemMode.fromCommandName(command));
    fandemCommand.execute();
  }

  private JCommander newCommander() {
    JCommander.Builder builder = JCommander.newBuilder();
    commandMap.forEach((mode, command) -> builder.addCommand(mode.getCommandName(), command));
    return builder
        .addObject(this)
        .build();
  }
}
