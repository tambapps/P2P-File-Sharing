package com.tambapps.p2p.fandem.cl;

import com.tambapps.p2p.fandem.Fandem;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MainIntegrationTest {

	private static final String IP_ADDRESS = "127.0.0.1";
	private static final String IP_ADDRESS_HEX_STRING = Fandem.toHexString(IP_ADDRESS);

	private static final File OUTPUT_DIRECTORY = new File("./");

	@Test
	public void transferTest() throws Exception {
		test(() -> Main.main("receive -d=%s -peer=%s:8081".formatted(OUTPUT_DIRECTORY, IP_ADDRESS).split(" ")));
	}

	@Test
	public void transferTestHexString() throws Exception {
		test(() -> Main.main("receive -d=%s -peer=%s".formatted(OUTPUT_DIRECTORY, IP_ADDRESS_HEX_STRING).split(" ")));
	}

	private void test(Runnable receiveRunnable) throws Exception {
		List<String> filePaths = Stream.of("file1.txt", "file2.txt")
				.map(name -> getClass().getClassLoader()
						.getResource(name)
						.getFile())
				.collect(Collectors.toList());

		new Thread(() -> Main.main(
				"send %s -ip=%s -p=8081".formatted(String.join(" ", filePaths), IP_ADDRESS)
						.split(" "))).start();
		Thread.sleep(1000);

		List<File> originFiles = filePaths.stream()
				.map(filePath -> new File(URLDecoder.decode(filePath, StandardCharsets.UTF_8)))
				.toList();

		receiveRunnable.run();

		for (File originFile : originFiles) {
			File downloadedFile = new File(OUTPUT_DIRECTORY, originFile.getName());
			assertTrue(downloadedFile.exists(), "Didn't downloaded file");
			assertEquals(Files.readString(originFile.toPath()), Files.readString(downloadedFile.toPath()),
					"Content of received file differs from origin file");
			assertTrue(downloadedFile.delete());
		}
	}
}
