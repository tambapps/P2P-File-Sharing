package com.tambapps.file_sharing_app;

import com.tambapps.file_sharing_app.service.ReceiveService;
import com.tambapps.file_sharing_app.service.SendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;


@SpringBootApplication
public class FileSharingApplication {

	private final static Logger LOGGER = LoggerFactory.getLogger(FileSharingApplication.class);

	public static void main(String[] args) {
		if (args.length > 0) {
			if (args[0].equals("-send") || args[0].equals("-receive")) {
				List<String> options = new ArrayList<>();
				options.addAll(Arrays.asList(args).subList(1, args.length));

				if (args[0].equals("-send")) {
					if (options.size() < 2) {
						LOGGER.error("You must provide the ip address and at least one file");
						return;
					}

					SendService sendService = new SendService(new ConcurrentHashMap<>(),
							Executors.newFixedThreadPool(1), new Timer());
					String ip = parseOption("ip", options);
					for (int i = 2; i < args.length; i++) {
						try {
						    String filePath = URLDecoder.decode(parseOption("filePath", options), "UTF-8");
						    if (ip.contains(":")) { //is peer with port
                                sendService.manualSend(filePath, ip.substring(0, ip.indexOf(':')), Integer.parseInt(ip.substring(ip.indexOf(':')+1)));
						    } else {
                                sendService.manualSend(filePath, ip);
                            }
						} catch (IllegalArgumentException e) {
							LOGGER.error("Error while reading option", e);
						} catch (IOException e) {
							LOGGER.error("Error while sending file", e);
						}
					}
				} else {
					if (options.size() < 2) {
						LOGGER.error("You must provide the -downloadPath and the sending -peer");
						return;
					}
					ReceiveService receiveService = new ReceiveService(new ConcurrentHashMap<>(),
							Executors.newFixedThreadPool(1), new Timer());
					try {
						receiveService.manualReceive(parseOption("downloadPath", options), parseOption("peer", options));
					} catch (IllegalArgumentException e) {
						LOGGER.error("Error while reading option", e);
					}
				}

				return;
			}
		}
		SpringApplication.run(FileSharingApplication.class, args);
	}

	private static String parseOption(String name, List<String> options) {
		for (String option : options) {
			int index = option.indexOf("-" + name + "=");
			if (index >= 0) {
				return option.substring(option.indexOf('=') + 1);
			}
		}
		throw new IllegalArgumentException("Couldn't find option");
	}
}
