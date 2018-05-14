package com.tambapps.file_sharing_app;

import com.tambapps.file_sharing_app.service.ReceiveService;
import com.tambapps.file_sharing_app.service.SendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;


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
					for (int i = 2; i < args.length; i++) {
						try {
							sendService.manualSend(parseOption("filePath", options), parseOption("ip", options));
						} catch (IllegalArgumentException e) {
							LOGGER.error("Error while reading option", e);
						} catch (IOException e) {
							LOGGER.error("Error while sending file", e);
						}
					}
				} else {
					if (options.size() < 2) {
						LOGGER.error("You must provide a download path and the sending peer");
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
