package com.tambapps.p2p.fandem.cl;

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

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main implements ReceivingListener, SendingListener {
	private static final String RECEIVE = "receive";
	private static final String SEND = "send";

	static final String RECEIVE_PROGRESS_FORMAT = "\rReceived %s / %s";
	static final String SEND_PROGRESS_FORMAT = "\rSent %s / %s";


	private static ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	private final String progressFormat;

	public Main(String progressFormat) {
		this.progressFormat = progressFormat;
	}

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

		Main main = new Main(command.equals(SEND) ? SEND_PROGRESS_FORMAT : RECEIVE_PROGRESS_FORMAT);
		switch (command) {
			case RECEIVE:
				main.receive(receiveCommand);
				break;
			case SEND:
				main.send(sendCommand);
				break;
		}
		EXECUTOR_SERVICE.shutdownNow();
	}

	void send(SendCommand command) {
		try (Sender sender = Sender.create(EXECUTOR_SERVICE, command, this)) {
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

	// for testing purpose
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

		try (Scanner scanner = new Scanner(System.in)) {
			return searchSendingPeer(scanner, address);
		}
	}

	private Peer searchSendingPeer(Scanner scanner, InetAddress address) {
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
				System.out.format(
						"%s wants to send %s.\nReceive this file? (Tap 'y' for yes ,'n' for no or 's' to stop)",
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

	@Override
	public void onConnected(@NotNull Peer selfPeer, @NotNull Peer remotePeer,
			@NotNull String fileName, long fileSize) {
		System.out.println("Connected to peer " + remotePeer);
		System.out.println("Receiving " + fileName);
		System.out.format(progressFormat, "0kb",
				FileUtils.bytesToString(fileSize));
	}

	@Override
	public void onProgressUpdate(int progress, long bytesProcessed, long totalBytes) {
		System.out.format(progressFormat,
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
		jCommander.usage(SEND);
		jCommander.usage(RECEIVE);
	}

}
