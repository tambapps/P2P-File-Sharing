package com.tambapps.p2p.fandem.cl;

import static com.tambapps.p2p.fandem.cl.Mode.RECEIVE;
import static com.tambapps.p2p.fandem.cl.Mode.SEND;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.tambapps.p2p.fandem.Fandem;
import com.tambapps.p2p.fandem.SenderPeer;
import com.tambapps.p2p.fandem.util.FileUtils;
import com.tambapps.p2p.fandem.util.TransferListener;
import com.tambapps.p2p.fandem.cl.command.Arguments;
import com.tambapps.p2p.fandem.cl.command.SendCommand;

import com.tambapps.p2p.fandem.cl.exception.SendingException;
import com.tambapps.p2p.fandem.cl.command.ReceiveCommand;
import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.speer.datagram.DatagramSupplier;
import com.tambapps.p2p.speer.exception.HandshakeFailException;
import com.tambapps.p2p.speer.util.PeerUtils;

import java.io.File;
import java.io.IOException;

import java.net.InetAddress;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

public class Main implements TransferListener {

	private final ScheduledExecutorService senderExecutor =
			Executors.newSingleThreadScheduledExecutor();
	private final Mode mode;

	public Main(Mode mode) {
		this.mode = mode;
	}

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
		main.senderExecutor.shutdown();
	}

	void send(SendCommand command) {
		try (Sender sender = Sender.create(senderExecutor, command, this)) {
			try {
				System.out.println("Will send files " + command.getFiles().stream().map(File::getName).collect(
						Collectors.joining(", ")));
				System.out.format("Waiting for a connection on %s (hex string %s)", sender.getPeer(), Fandem.toHexString(sender.getPeer()))
						.println();
				sender.send(command.getFiles());
				System.out.println();
				System.out.println("File(s) were successfully sent");
			} catch (SocketException e) {
				System.out.println();
				System.out.println("Transfer was cancelled.");
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
		Peer peer = receiveCommand.getPeer().orElseGet(this::seekSendingPeer);
		if (peer != null) {
			Receiver receiver = new Receiver(peer, receiveCommand.getDownloadDirectory(), this);
			System.out.println("Connecting to " + peer);
			try {
				List<File> files = receiver.receive();
				System.out.println();
				System.out.println("Received successfully files " +
						files.stream()
						.map(File::getName)
								.collect(Collectors.joining(", ")));
				System.out.println("Directory: " + receiveCommand.getDownloadDirectory().getAbsolutePath());
			} catch (HandshakeFailException e) {
				System.out.println("Error while communicating with other peer: " + e.getMessage());
			} catch (IOException e) {
				System.out.println();
				System.out.println("Error while receiving files: " + e.getMessage());
			}
		}
	}

	// for testing overriding
	InetAddress getIpAddress() throws IOException {
		return PeerUtils.getPrivateNetworkIpAddress();
	}

	private Peer seekSendingPeer() {
		InetAddress address;
		try {
			address = getIpAddress();
		} catch (IOException e) { // should be IOException but IDE don't want
			System.out.println("Couldn't find ip address");
			return null;
		}

		System.out.println("Looking for a sending peer...");
		try (Scanner scanner = new Scanner(System.in)) {
			return seekSendingPeer(scanner, address);
		}
	}

	private SenderPeer seekSendingPeer(Scanner scanner, InetAddress address) {
		DatagramSupplier<List<SenderPeer>> sniffSupplier = null;
		try {
			sniffSupplier = Fandem.senderPeersSupplier();
		} catch (IOException e) {
			System.out.println("Couldn't start seeking peers: " + e.getMessage());
			System.exit(1);
		}
		while (true) {
			try {
				List<SenderPeer> senderPeers = sniffSupplier.get();
				for (SenderPeer senderPeer : senderPeers) {
					System.out.format(
							"%s wants to send\n%s.\nReceive this file? (Tap 'y' for yes ,'n' for no or 's' to stop)",
							senderPeer.getDeviceName(),
									senderPeer.getFiles()
											.stream()
											.map(f -> String.format("%s (%s)", f.getFileName(), FileUtils.toFileSize(f.getFileSize())))
											.collect(Collectors.joining("\n- ", "- ", "")))
							.println();
					switch (scanner.nextLine().toLowerCase().charAt(0)) {
						case 'y':
							return senderPeer;
						case 's':
							return null;
						// default do nothing
					}
				}
			} catch (IOException e) {
				return null;
			}
		}
	}

	@Override
	public void onConnected(Peer selfPeer, Peer remotePeer) {
		System.out.println("Connected to peer " + remotePeer);
	}

	@Override
	public void onTransferStarted(String fileName, long fileSize) {
		System.out.format("\n%s %s", mode.ingString(), fileName).println();
		System.out.format(mode.progressFormat(), "0kb",
				FileUtils.toFileSize(fileSize));
	}

	@Override
	public void onProgressUpdate(String fileName, int progress, long bytesProcessed,
			long totalBytes) {
		System.out.format(mode.progressFormat(),
				FileUtils.toFileSize(bytesProcessed),
				FileUtils.toFileSize(totalBytes));
	}

	private static void printHelp(JCommander jCommander) {
		jCommander.usage(SEND.commandName());
		jCommander.usage(RECEIVE.commandName());
	}

}
