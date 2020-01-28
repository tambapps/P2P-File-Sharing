package com.tambapps.p2p.fandem.cl;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.tambapps.p2p.fandem.Peer;
import com.tambapps.p2p.fandem.cl.command.Arguments;
import com.tambapps.p2p.fandem.cl.command.SendCommand;

import com.tambapps.p2p.fandem.exception.SniffException;
import com.tambapps.p2p.fandem.listener.ReceivingListener;
import com.tambapps.p2p.fandem.listener.SendingListener;
import com.tambapps.p2p.fandem.sniff.PeerSniffBlockingSupplier;
import com.tambapps.p2p.fandem.sniff.SniffPeer;
import com.tambapps.p2p.fandem.sniff.service.PeerSniffHandlerService;
import com.tambapps.p2p.fandem.task.ReceivingTask;
import com.tambapps.p2p.fandem.task.SendingTask;
import com.tambapps.p2p.fandem.util.FileUtils;
import com.tambapps.p2p.fandem.util.IPUtils;
import com.tambapps.p2p.fandem.cl.command.ReceiveCommand;

import java.io.File;
import java.io.IOException;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import java.util.Objects;
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
				Peer peer = getSendingPeer(receiveCommand.getPeer(), receiveCommand.getIp());
				if (peer != null) {
					receive(peer, receiveCommand.getDownloadPath(), receiveCommand.getCount());
				}
				break;
			case SEND:
				send(sendCommand);
				break;
		}
		EXECUTOR_SERVICE.shutdownNow();
	}

	private static Peer getSendingPeer(Peer peer, String optionalIp) {
		return peer != null ? peer : searchSendingPeer(optionalIp);
	}

	private static void send(SendCommand sendCommand) {
		InetAddress address;
		if (sendCommand.getIp() == null) {
			try {
				address = Objects.requireNonNull(IPUtils.getIpAddress());
			} catch (Exception e) {
				System.out.println("Couldn't get ip address (are you connected to the internet?)");
				return;
			}
		} else {
			try {
				address = InetAddress.getByName(sendCommand.getIp());
			} catch (UnknownHostException e) {
				System.out.println("Couldn't get ip address (is it well formatted?)");
				return;
			}
		}

		Integer port  = sendCommand.getPort();
		if (port == null) {
			port = IPUtils.getAvailablePort(address);
		}
		Peer peer = Peer.of(address, port);
		SendingListener listener = new SendingListener() {
			final String progressFormat = "\rSent %s / %s";
			@Override
			public void onConnected(Peer selfPeer, Peer remotePeer, String fileName, long fileSize) {
				System.out.println("Connected to peer " + remotePeer);
				System.out.format(progressFormat, "0kb",
					FileUtils.bytesToString(fileSize));
			}

			@Override
			public void onProgressUpdate(int progress, long byteProcessed, long totalBytes) {
				System.out.format(progressFormat,
					FileUtils.bytesToString(byteProcessed),
					FileUtils.bytesToString(totalBytes));
			}

			@Override
			public void onStart(Peer self, String fileName) {
				System.out.println("Sending " + fileName);
				System.out.println("Waiting for a connection on " + self);
				System.out.println("Hex string: " + self.toHexString().toUpperCase());
			}
		};
		final PeerSniffHandlerService sniffHandlerService = new PeerSniffHandlerService(EXECUTOR_SERVICE, peer, getDesktopName(), "");
		sniffHandlerService.start();
		for (String filePath : sendCommand.getFilePath()) {
			File file;
			try {
				file = new File(decodePath(filePath));
			} catch (UnsupportedEncodingException e) {
				System.out.println("Couldn't decode path " + filePath);
				continue;
			}
			sniffHandlerService.setFileName(file.getName());
			if (!file.exists()) {
				System.out.format("This file doesn't exist (%s)", filePath).println();
				continue;
			}
			if (!file.isFile()) {
				System.out.format("This isn't a file (%s)", filePath).println();
				continue;
			}

			try {
				new SendingTask(listener, peer, sendCommand.getTimeout()).send(file);
				System.out.println();
				System.out.println(file.getName() + " was successfully sent");
			} catch (IOException e) {
				System.out.println();
				System.out.format("Error while sending %s: %s",file.getName(), e.getMessage()).println();
				continue;
			}
			System.out.println();
		}
		sniffHandlerService.stop();
	}

	private static String decodePath(String path) throws UnsupportedEncodingException {
		return URLDecoder.decode(path, StandardCharsets.UTF_8.name());
	}

	private static String getDesktopName() {
		String name = System.getenv("COMPUTERNAME");
		if (name != null && !name.isEmpty()) {
			return name;
		}
		name = System.getenv("HOSTNAME");
		if (name != null && !name.isEmpty()) {
			return name;
		}
		try {
			return InetAddress.getLocalHost().getHostName();
		}
		catch (UnknownHostException ex) {
			return  System.getProperty("user.name") + "Desktop";
		}
	}
	private static void receive(Peer peer, String downloadPath, int count) {
		File dirFile = new File(downloadPath);
		if (!dirFile.exists()) {
			System.out.println(dirFile.getPath() + " doesn't exist");
			return;
		}

		ReceivingListener listener = new ReceivingListener() {
			final String progressFormat = "\rReceived %s / %s";
			@Override
			public void onConnected(Peer selfPeer, Peer remotePeer, String fileName, long fileSize) {
				System.out.println("Connected to peer " + remotePeer);
				System.out.println("Receiving " + fileName);
				System.out.format(progressFormat, "0kb",
					FileUtils.bytesToString(fileSize));
			}

			@Override
			public void onProgressUpdate(int progress, long byteProcessed, long totalBytes) {
				System.out.format(progressFormat,
					FileUtils.bytesToString(byteProcessed),
					FileUtils.bytesToString(totalBytes));
			}

			@Override
			public void onEnd(File file) {
				System.out.println();
				System.out.println("Received " + file.getName() + " successfully");
				System.out.println("Path: " + file.getPath());
			}
		};

		for (int i = 0; i < count; i++) {
			System.out.println("Connecting to " + peer);
			try {
				new ReceivingTask(listener, FileUtils.availableFileInDirectoryProvider(dirFile)).receiveFrom(peer);
			} catch (IOException e) {
				System.out.println();
				System.out.println("Error while receiving file: " + e.getMessage());
				continue;
			}
			System.out.println();
		}
	}

	private static Peer searchSendingPeer(String optionalIp) {
		InetAddress address;
		try {
			address = optionalIp == null ? IPUtils.getIpAddress() : InetAddress.getByName(optionalIp);
		} catch (IOException e) {
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
