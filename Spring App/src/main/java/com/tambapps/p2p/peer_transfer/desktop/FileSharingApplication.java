package com.tambapps.p2p.peer_transfer.desktop;

import com.tambapps.p2p.file_sharing.FileReceiver;
import com.tambapps.p2p.file_sharing.FileSender;
import com.tambapps.p2p.file_sharing.IPUtils;
import com.tambapps.p2p.file_sharing.TransferListener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;


import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.URLDecoder;
import java.util.*;

@SpringBootApplication
public class FileSharingApplication {

	public static void main(String[] args) {
		if (args.length > 0) {
			if (args[0].equals("-send") || args[0].equals("-receive")) {
				List<String> options = new ArrayList<>();
				options.addAll(Arrays.asList(args).subList(1, args.length));

				if (args[0].equals("-send")) {
					String filePath;
					try {
						filePath = parseOption("filePath", options);
					} catch (IllegalArgumentException e) {
						System.err.println("You must provide at least one file");
						return;
					}

					String sPort = parseOptionOrDefault("port", options, null);

					String address = parseOptionOrDefault("ip", options, null);
					if (address == null) {
						try {
							address = Objects.requireNonNull(IPUtils.getIPAddress()).getHostAddress();
						} catch (SocketException|NullPointerException e) {
							System.err.println("Couldn't get ip address (are you connected to the internet?)");
							e.printStackTrace();
						}
					}

					FileSender sender;
					try {
						if (sPort == null) {
							sender = new FileSender(address);
						} else {
							sender = new FileSender(address, Integer.parseInt(sPort), 0);
						}
					} catch (IOException e) {
						System.err.println("Couldn't instantiate send service");
						e.printStackTrace();
						return;
					}

					sender.setTransferListener(new TransferListener() {
						final String progressFormat = "Sent %s / %s";
						@Override
						public void onConnected(String remoteAddress, int remotePort, String fileName,
												long fileSize) {
							System.out.println("Connected to peer " + remoteAddress + ":" + remotePort);
							System.out.print(String.format(progressFormat, "0kb",
									TransferListener.bytesToString(fileSize)));
						}

						@Override
						public void onProgressUpdate(int progress, long byteProcessed, long totalBytes) {
							System.out.print("\r" + String.format(progressFormat,
									TransferListener.bytesToString(byteProcessed),
									TransferListener.bytesToString(totalBytes)));
						}
					});

					while (filePath != null) {
						try {
							filePath = URLDecoder.decode(filePath, "UTF-8");
							File file = new File(filePath);
							System.out.println("Sending " + file.getName());
							System.out.println("Waiting for a connection on " + sender.getIp() + ":" + sender.getPort());
							sender.send(file);
							System.out.println();
							System.out.println(file.getName() + "was successfully sent");
						} catch (UnsupportedEncodingException e) {
							System.err.println("Couldn't resolve filePath " + filePath);
							e.printStackTrace();
						} catch (IOException e) {
							System.err.println("Error while sending file");
							e.printStackTrace();
						}

						filePath = parseOptionOrDefault("filePath", options, null);
						System.out.println();
					}
				} else {
					if (options.size() < 2) {
						System.out.println("You must provide the -downloadPath and the sending -peer");
						return;
					}

					try {
						FileReceiver receiver = new FileReceiver(parseOption("downloadPath", options));
						receiver.setTransferListener(new TransferListener() {
							final String progressFormat = "Received %s / %s";
							@Override
							public void onConnected(String remoteAddress, int remotePort, String fileName,
													long fileSize) {
								System.out.println("Connected to peer " + remoteAddress + ":" + remotePort);
								System.out.println("Receiving " + fileName);
								System.out.print(String.format(progressFormat, "0kb",
										TransferListener.bytesToString(fileSize)));
							}

							@Override
							public void onProgressUpdate(int progress, long byteProcessed, long totalBytes) {
								System.out.print("\r" + String.format(progressFormat,
										TransferListener.bytesToString(byteProcessed),
										TransferListener.bytesToString(totalBytes)));
							}
						});

						String peer = parseOption("peer", options);
						System.out.println("Connecting to " + peer);
						receiver.receiveFrom(peer);
						System.out.println();
						System.out.println("Received " + receiver.getReceivedFile().getName() + " successfully");
					} catch (IllegalArgumentException e) {
						System.err.println("Error while reading option");
						e.printStackTrace();
					} catch (IOException e) {
						System.err.println("Error while receiving file");
						e.printStackTrace();
					}
				}

				return;
			}
		}
		SpringApplication.run(FileSharingApplication.class, args);
	}

	private static String parseOption(String name, List<String> options) {
		String option = parseOptionOrDefault(name, options, null);
		if (option == null) {
			throw new IllegalArgumentException("Couldn't find option");
		}
		return option;
	}

	private static String parseOptionOrDefault(String name, List<String> options, String defValue) {
		for (String option : options) {
			int index = option.indexOf("-" + name + "=");
			if (index >= 0) {
				options.remove(option);
				return option.substring(option.indexOf('=') + 1);
			}
		}
		return defValue;
	}
}
