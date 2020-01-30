package com.tambapps.p2p.fandem.cl;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.tambapps.p2p.fandem.Peer;
import com.tambapps.p2p.fandem.cl.command.Arguments;
import com.tambapps.p2p.fandem.cl.command.SendCommand;

import com.tambapps.p2p.fandem.cl.exception.SendingException;
import com.tambapps.p2p.fandem.cl.receive.Receiver;
import com.tambapps.p2p.fandem.cl.send.CommandLineSender;
import com.tambapps.p2p.fandem.exception.SniffException;
import com.tambapps.p2p.fandem.listener.ReceivingListener;
import com.tambapps.p2p.fandem.sniff.PeerSniffBlockingSupplier;
import com.tambapps.p2p.fandem.sniff.SniffPeer;
import com.tambapps.p2p.fandem.task.ReceivingTask;
import com.tambapps.p2p.fandem.util.FileUtils;
import com.tambapps.p2p.fandem.util.IPUtils;
import com.tambapps.p2p.fandem.cl.command.ReceiveCommand;

import java.io.File;
import java.io.IOException;

import java.net.InetAddress;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
	private static final String RECEIVE = "receive";
	private static final String SEND = "send";
	private static ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	public static void main(String[] args) {
		Arguments arguments = new Arguments();
		ReceiveCommand receiveCommand = new ReceiveCommand();
		SendCommand sendCommand = new SendCommand();
		JCommander jCommander = JCommander.newBuilder()
				.addObject(arguments)
				.addCommand(RECEIVE, receiveCommand)
				.addCommand(SEND, sendCommand)
				.build();

		try {
			jCommander.parse(args);
		} catch (ParameterException e) {
			System.out.println("Error: " + e.getMessage());
			printHelp(jCommander);
			return;
		}

		if (arguments.getHelp()) {
			printHelp(jCommander);
			return;
		}

		String command = jCommander.getParsedCommand();
		if (command == null) {
			System.out.println("You must enter a command");
			printHelp(jCommander);
			return;
		}
		switch (command) {
			case RECEIVE:
				receive(receiveCommand);
				break;
			case SEND:
				send(sendCommand);
				break;
		}
		EXECUTOR_SERVICE.shutdownNow();
	}

	private static void send(SendCommand command) {
		try (CommandLineSender sender = CommandLineSender.create(EXECUTOR_SERVICE, command)) {
			for (File file : command.getFiles()) {
				sender.send(file);
				System.out.println();
				System.out.println(file.getName() + " was successfully sent");
				System.out.println();
			}
		} catch (SendingException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println("An error occurred while transferring file(s). Aborting transfer(s)");
		}
	}

	private static void receive(ReceiveCommand receiveCommand) {
		Peer peer = receiveCommand.getPeer().orElseGet(Main::searchSendingPeer);
		if (peer != null) {
			Receiver receiver = new Receiver(peer, receiveCommand.getDownloadDirectory());
			for (int i = 0; i < receiveCommand.getCount(); i++) {
				System.out.println("Connecting to " + peer);
				try {
					receiver.receive();
				} catch (IOException e) {
					System.out.println();
					System.out.println("Error while receiving file: " + e.getMessage());
					continue;
				}
				System.out.println();
			}
		}
	}

	private static Peer searchSendingPeer() {
		InetAddress address;
		try {
			address = IPUtils.getIpAddress();
		} catch (Exception e) { // should be IOException but IDE don't want
			System.out.println("Couldn't find ip address");
			return null;
		}

		try (Scanner scanner = new Scanner(System.in)) {
			return searchSendingPeer(scanner, address);
		}
	}

	private static Peer searchSendingPeer(Scanner scanner, InetAddress address) {
		PeerSniffBlockingSupplier sniffSupplier;
		try {
			sniffSupplier = new PeerSniffBlockingSupplier(EXECUTOR_SERVICE, address);
		} catch (IOException e) {
			System.out.println("Couldn't start detecting sending peer: " + e.getMessage());
			return null;
		}

		while (true) {
			try {
				SniffPeer sniffPeer = sniffSupplier.get();
				System.out.format("%s wants to send %s.\nReceive this file? (Tap 'y' for yes ,'n' for no or 's' to stop)",
						sniffPeer.getDeviceName(), sniffPeer.getFileName())
						.println();
				switch (scanner.nextLine().toLowerCase().charAt(0)) {
					case 'y':
						return sniffPeer.getPeer();
					case 's':
						return null;
					default:
						continue;
				}
			} catch (SniffException e) {
				System.out.println("Error while detecting sending peer: " + e.getMessage());
				return null;
			} catch (InterruptedException ignored) { }
		}
	}

	private static void printHelp(JCommander jCommander) {
		jCommander.usage(SEND);
		jCommander.usage(RECEIVE);
	}
}
