package com.tambapps.p2p.fandem.cl;

import com.tambapps.p2p.fandem.Fandem;
import com.tambapps.p2p.fandem.cl.command.ReceiveCommand;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class MainIntegrationTest {

	private static final String IP_ADDRESS = "127.0.0.1";
	private static final String IP_ADDRESS_HEX_STRING = Fandem.toHexString(IP_ADDRESS);
	private static final String SNIFF_IP_ADDRESS = "127.0.0.2";

	private static final File OUTPUT_FILE = new File("./file-received.txt");

	@Test
	public void transferTest() throws Exception {
		test(() -> Main.main(String.format("receive -d=%s -peer=%s:8081", OUTPUT_FILE, IP_ADDRESS).split(" ")));
	}

	@Test
	public void transferTestHexString() throws Exception {
		test(() -> Main.main(String.format("receive -d=%s -peer=%s", OUTPUT_FILE, IP_ADDRESS_HEX_STRING).split(" ")));
	}

	// run these test individually (weird but when we run all test, this ne fails)
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
		when(command.getCount())
				.thenReturn(1);
		when(command.getPeer())
				.thenReturn(Optional.empty());
		when(command.getDownloadFile())
				.thenReturn(OUTPUT_FILE);
		test(() -> receiveMain.receive(command));
	}

	private void test(Runnable receiveRunnable) throws Exception {
		String filePath = getClass().getClassLoader()
				.getResource("file.txt")
				.getFile();

		new Thread(() -> Main.main(
				String.format("send %s -ip=%s -p=8081", filePath, IP_ADDRESS)
						.split(" "))).start();
		Thread.sleep(1000);

		File originFile = new File(URLDecoder.decode(filePath, "UTF-8"));

		receiveRunnable.run();

		assertTrue("Didn't correctly downloaded file", OUTPUT_FILE.exists());

		assertEquals("Content of received file differs from origin file",
				Files.readString(originFile.toPath()), Files.readString(OUTPUT_FILE.toPath()));
	}

	@After
	public void dispose() {
		if (OUTPUT_FILE.exists()) {
			OUTPUT_FILE.delete();
		}
	}

	private boolean contentEquals(File f1, File f2) throws IOException {
		try (InputStream is1 = new FileInputStream(f1);
				InputStream is2 = new FileInputStream(f2)) {
			final int EOF = -1;
			int i1 = is1.read();
			while (i1 != EOF) {
				int i2 = is2.read();
				if (i2 != i1) {
					return false;
				}
				i1 = is1.read();
			}
			return is2.read() == EOF;
		}
	}

}
