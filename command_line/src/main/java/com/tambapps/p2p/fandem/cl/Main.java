package com.tambapps.p2p.fandem.cl;

import static com.tambapps.p2p.fandem.cl.Mode.RECEIVE;
import static com.tambapps.p2p.fandem.cl.Mode.SEND;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.tambapps.p2p.fandem.Fandem;
import com.tambapps.p2p.fandem.cl.command.Arguments;
import com.tambapps.p2p.fandem.cl.command.SendCommand;
import com.tambapps.p2p.fandem.cl.command.ReceiveCommand;

public class Main {

	public static void main(String[] args) {
		System.out.println("Fandem command line " + Fandem.VERSION);
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

		Mode mode = Mode.valueOf(command.toUpperCase());
		switch (mode) {
			case RECEIVE -> receiveCommand.execute();
			case SEND -> {
				// TODO replace this by custom validation
				if (sendCommand.getFiles().size() > 10) {
					System.out.println("You cannot send more than 10 files at once");
					return;
				}
				sendCommand.execute();
			}
		}
	}
}
