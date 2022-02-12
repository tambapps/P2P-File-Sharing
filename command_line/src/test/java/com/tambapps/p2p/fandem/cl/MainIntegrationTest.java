package com.tambapps.p2p.fandem.cl;

import com.tambapps.p2p.fandem.Fandem;
import com.tambapps.p2p.fandem.cl.command.ReceiveCommand;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class MainIntegrationTest {

	private static final String IP_ADDRESS = "127.0.0.1";
	private static final String IP_ADDRESS_HEX_STRING = Fandem.toHexString(IP_ADDRESS);
	private static final String SNIFF_IP_ADDRESS = "127.0.0.2";

	private static final File OUTPUT_DIRECTORY = new File("./");

	@Test
	public void transferTest() throws Exception {
		test(() -> Main.main(String.format("receive -d=%s -peer=%s:8081", OUTPUT_DIRECTORY, IP_ADDRESS).split(" ")));
	}

	@Test
	public void transferTestHexString() throws Exception {
		test(() -> Main.main(String.format("receive -d=%s -peer=%s", OUTPUT_DIRECTORY, IP_ADDRESS_HEX_STRING).split(" ")));
	}

	// run these test individually (weird but when we run all test, this ne fails)
	@Ignore("Can't run it with only one computer since broadcasting")
	@Test
	public void transferWithSniffTest() throws Exception {
		System.setIn(new ByteArrayInputStream("y\n".getBytes()));
		// need to 'mock' ip address that will be used for sniffing, since we're using localhost
		Main receiveMain = new Main(Mode.RECEIVE) {
			@Override InetAddress getIpAddress() throws IOException {
				return InetAddress.getByName(SNIFF_IP_ADDRESS);
			}
		};

		ReceiveCommand command = Mockito.mock(ReceiveCommand.class);
		when(command.getPeer())
				.thenReturn(Optional.empty());
		when(command.getDownloadDirectory())
				.thenReturn(OUTPUT_DIRECTORY);
		test(() -> receiveMain.receive(command));
	}

	private void test(Runnable receiveRunnable) throws Exception {
		List<String> filePaths = Stream.of("file1.txt", "file2.txt")
				.map(name -> getClass().getClassLoader()
						.getResource(name)
						.getFile())
				.collect(Collectors.toList());

		new Thread(() -> Main.main(
				String.format("send %s -ip=%s -p=8081", String.join(" ", filePaths), IP_ADDRESS)
						.split(" "))).start();
		Thread.sleep(1000);

		List<File> originFiles = filePaths.stream()
				.map(filePath -> new File(URLDecoder.decode(filePath, StandardCharsets.UTF_8)))
				.collect(Collectors.toList());

		receiveRunnable.run();

		for (File originFile : originFiles) {
			File downloadedFile = new File(OUTPUT_DIRECTORY, originFile.getName());
			assertTrue("Didn't downloaded file", downloadedFile.exists());
			assertEquals("Content of received file differs from origin file",
					Files.readString(originFile.toPath()), Files.readString(downloadedFile.toPath()));
			assertTrue(downloadedFile.delete());
		}
	}

}
