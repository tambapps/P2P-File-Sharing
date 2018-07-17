package com.tambapps.p2p.peer_transfer.desktop;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.tambapps.p2p.file_sharing.*;

import com.tambapps.p2p.peer_transfer.desktop.command.Arguments;
import com.tambapps.p2p.peer_transfer.desktop.command.ReceiveCommand;
import com.tambapps.p2p.peer_transfer.desktop.command.SendCommand;

import java.io.File;
import java.io.IOException;

import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import java.util.Objects;

public class FileSharingLauncher {
	private final static String RECEIVE = "receive";
	private final static String SEND = "send";

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
	}

	private static void send(SendCommand sendCommand) {
		String address = sendCommand.getIp();
		if (address == null) {
			try {
				address = Objects.requireNonNull(IPUtils.getIPAddress()).getHostAddress();
			} catch (SocketException|NullPointerException e) {
				System.out.println("Couldn't get ip address (are you connected to the internet?)");
			}
		}

		int port = sendCommand.getPort();
		FileSender fileSender;
		try {
			if (port == 0) {
				fileSender = new FileSender(address);
			} else {
				fileSender = new FileSender(new Peer(address, port));
			}
		} catch (IOException e) {
			System.out.println("Couldn't start sending");
			System.out.println("Error: " + e.getMessage());
			return;
		}

		fileSender.setTransferListener(new TransferListener() {
			final String progressFormat = "\rSent %s / %s";
			@Override
			public void onConnected(String remoteAddress, int remotePort, String fileName,
									long fileSize) {
				System.out.println("Connected to peer " + remoteAddress + ":" + remotePort);
				System.out.format(progressFormat, "0kb",
						TransferListener.bytesToString(fileSize));
			}

			@Override
			public void onProgressUpdate(int progress, long byteProcessed, long totalBytes) {
				System.out.format(progressFormat,
						TransferListener.bytesToString(byteProcessed),
						TransferListener.bytesToString(totalBytes));
			}
		});

		for (String filePath : sendCommand.getFilePath()) {
			File file;
			try {
				file = new File(decodePath(filePath));
			} catch (UnsupportedEncodingException e) {
				System.out.println("Couldn't decode path " + filePath);
				continue;
			}
			if (!file.exists()) {
				System.out.format("This file doesn't exist (%s)", filePath).println();
				continue;
			}
			if (!file.isFile()) {
				System.out.format("This isn't a file (%s)", filePath).println();
				continue;
			}
			System.out.println("Sending " + file.getName());
			System.out.println("Waiting for a connection on " + fileSender.getIp() + ":" + fileSender.getPort());
			try {
				fileSender.send(file);
				System.out.println();
				System.out.println(file.getName() + " was successfully sent");
			} catch (IOException e) {
				System.out.println();
				System.out.format("Error while sending %s: %s",file.getName(), e.getMessage()).println();
				continue;
			}
			System.out.println();
		}
	}

	private static String decodePath(String path) throws UnsupportedEncodingException {
		return URLDecoder.decode(path, StandardCharsets.UTF_8.name());
	}

	private static void receive(ReceiveCommand receiveCommand) {
		FileReceiver fileReceiver;
		try {
			fileReceiver = new FileReceiver(receiveCommand.getDownloadPath());
		} catch (IOException e) {
			System.out.println("Couldn't start receiving");
			System.out.println("Error: " + e.getMessage());
			return;
		}

		fileReceiver.setTransferListener(new TransferListener() {
			final String progressFormat = "\rReceived %s / %s";
			@Override
			public void onConnected(String remoteAddress, int remotePort, String fileName,
									long fileSize) {
				System.out.println("Connected to peer " + remoteAddress + ":" + remotePort);
				System.out.println("Receiving " + fileName);
				System.out.format(progressFormat, "0kb",
						TransferListener.bytesToString(fileSize));
			}

			@Override
			public void onProgressUpdate(int progress, long byteProcessed, long totalBytes) {
				System.out.format(progressFormat,
						TransferListener.bytesToString(byteProcessed),
						TransferListener.bytesToString(totalBytes));
			}
		});
		Peer peer = receiveCommand.getPeer();
		for (int i = 0; i < receiveCommand.getCount(); i++) {
			System.out.println("Connecting to " + peer);
			try {
				fileReceiver.receiveFrom(peer);
				System.out.println();
				System.out.println("Received " + fileReceiver.getReceivedFile().getName() + " successfully");
			} catch (IOException e) {
				System.out.println();
				System.out.println("Error while receiving file: " + e.getMessage());
				continue;
			}
			System.out.println();
		}
	}

	private static void printHelp(JCommander jCommander) {
		jCommander.usage(SEND);
		jCommander.usage(RECEIVE);
	}
}
