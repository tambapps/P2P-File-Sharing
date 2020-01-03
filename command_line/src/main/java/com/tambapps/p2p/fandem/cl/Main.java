package com.tambapps.p2p.fandem.cl;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.tambapps.p2p.fandem.Peer;
import com.tambapps.p2p.fandem.cl.command.Arguments;
import com.tambapps.p2p.fandem.cl.command.SendCommand;

import com.tambapps.p2p.fandem.listener.ReceivingListener;
import com.tambapps.p2p.fandem.listener.SendingListener;
import com.tambapps.p2p.fandem.task.ReceivingTask;
import com.tambapps.p2p.fandem.task.SendingTask;
import com.tambapps.p2p.fandem.util.FileUtils;
import com.tambapps.p2p.fandem.util.IPUtils;
import com.tambapps.p2p.fandem.cl.command.ReceiveCommand;

import java.io.File;
import java.io.IOException;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import java.util.Objects;

public class Main {
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
		InetAddress address;
		if (sendCommand.getIp() == null) {
			try {
				address = Objects.requireNonNull(IPUtils.getIPAddress());
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
	}

	private static String decodePath(String path) throws UnsupportedEncodingException {
		return URLDecoder.decode(path, StandardCharsets.UTF_8.name());
	}

	private static void receive(ReceiveCommand receiveCommand) {
		File dirFile = new File(receiveCommand.getDownloadPath());
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

		Peer peer = receiveCommand.getPeer();
		for (int i = 0; i < receiveCommand.getCount(); i++) {
			System.out.println("Connecting to " + peer);
			try {
				new ReceivingTask(listener, FileUtils.newAvailableFileProvider(dirFile)).receiveFrom(peer);
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
