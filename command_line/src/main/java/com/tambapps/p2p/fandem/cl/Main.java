package com.tambapps.p2p.fandem.cl;

import static com.tambapps.p2p.fandem.cl.Mode.RECEIVE;
import static com.tambapps.p2p.fandem.cl.Mode.SEND;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.tambapps.p2p.fandem.Peer;
import com.tambapps.p2p.fandem.cl.command.Arguments;
import com.tambapps.p2p.fandem.cl.command.SendCommand;

import com.tambapps.p2p.fandem.cl.exception.SendingException;
import com.tambapps.p2p.fandem.exception.SniffException;
import com.tambapps.p2p.fandem.listener.ReceivingListener;
import com.tambapps.p2p.fandem.listener.SendingListener;
import com.tambapps.p2p.fandem.sniff.PeerSniffBlockingSupplier;
import com.tambapps.p2p.fandem.sniff.SniffPeer;
import com.tambapps.p2p.fandem.util.FileUtils;
import com.tambapps.p2p.fandem.util.IPUtils;
import com.tambapps.p2p.fandem.cl.command.ReceiveCommand;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

import java.net.InetAddress;

import java.net.SocketTimeoutException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main implements ReceivingListener, SendingListener {

	private final ExecutorService executor =
			Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	private final Mode mode;

	public Main(Mode mode) {
		this.mode = mode;
	}

	public static void main(String[] args) {
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

		Mode mode = Mode.valueOf(command.toUpperCase());
		Main main = new Main(mode);
		switch (mode) {
			case RECEIVE:
				main.receive(receiveCommand);
				break;
			case SEND:
				main.send(sendCommand);
				break;
		}
		main.executor.shutdownNow();
	}

	void send(SendCommand command) {
		try (Sender sender = Sender.create(executor, command, this)) {
			for (File file : command.getFiles()) {
				sender.send(file);
				System.out.println();
				System.out.println(file.getName() + " was successfully sent");
				System.out.println();
			}
		} catch (SendingException e) {
			System.out.println(e.getMessage());
		} catch (SocketTimeoutException e) {
			System.out.println("No connection was made, canceling transfer");
		} catch (IOException e) {
			System.out.println("An error occurred while transferring file(s). Aborting transfer(s)");
		}
	}

	void receive(ReceiveCommand receiveCommand) {
		Peer peer = receiveCommand.getPeer().orElseGet(this::searchSendingPeer);
		if (peer != null) {
			Receiver receiver = new Receiver(peer, receiveCommand.getDownloadDirectory(), this);
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

	// for testing overriding
	InetAddress getIpAddress() throws IOException {
		return IPUtils.getIpAddress();
	}

	private Peer searchSendingPeer() {
		InetAddress address;
		try {
			address = getIpAddress();
		} catch (IOException e) { // should be IOException but IDE don't want
			System.out.println("Couldn't find ip address");
			return null;
		}

		System.out.println("Looking for a sending peer...");
		try (Scanner scanner = new Scanner(System.in)) {
			return searchSendingPeer(scanner, address);
		}
	}

	private Peer searchSendingPeer(Scanner scanner, InetAddress address) {
		PeerSniffBlockingSupplier sniffSupplier;
		try {
			sniffSupplier = new PeerSniffBlockingSupplier(executor, address);
		} catch (IOException e) {
			System.out.println("Couldn't start detecting sending peer: " + e.getMessage());
			return null;
		}

		while (true) {
			try {
				SniffPeer sniffPeer = sniffSupplier.get();
				System.out.format(
						"%s wants to send %s.\nReceive this file? (Tap 'y' for yes ,'n' for no or 's' to stop)",
						sniffPeer.getDeviceName(), sniffPeer.getFileName())
						.println();
				switch (scanner.nextLine().toLowerCase().charAt(0)) {
					case 'y':
						sniffSupplier.stop();
						return sniffPeer.getPeer();
					case 's':
						return null;
						// default do nothing
				}
			} catch (SniffException e) {
				System.out.println("Error while detecting sending peer: " + e.getMessage());
				return null;
			} catch (InterruptedException ignored) { }
		}
	}

	@Override
	public void onConnected(@NotNull Peer selfPeer, @NotNull Peer remotePeer,
			@NotNull String fileName, long fileSize) {
		System.out.println("Connected to peer " + remotePeer);
		System.out.format("%s %s", mode.ingString(), fileName).println();
		System.out.format(mode.progressFormat(), "0kb",
				FileUtils.bytesToString(fileSize));
	}

	@Override
	public void onProgressUpdate(int progress, long bytesProcessed, long totalBytes) {
		System.out.format(mode.progressFormat(),
				FileUtils.bytesToString(bytesProcessed),
				FileUtils.bytesToString(totalBytes));
	}

	// Sending only
	@Override
	public void onStart(Peer self, @NotNull String fileName) {
		System.out.println("Sending " + fileName);
		System.out.println("Waiting for a connection on " + self);
		System.out.println("Hex string: " + self.toHexString().toUpperCase());
	}

	// Receiving only
	@Override
	public void onEnd(@NotNull File file) {
		System.out.println();
		System.out.println("Received " + file.getName() + " successfully");
		System.out.println("Path: " + file.getPath());
	}

	private static void printHelp(JCommander jCommander) {
		jCommander.usage(SEND.commandName());
		jCommander.usage(RECEIVE.commandName());
	}

}
