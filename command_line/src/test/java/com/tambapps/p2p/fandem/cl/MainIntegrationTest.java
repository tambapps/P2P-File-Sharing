package com.tambapps.p2p.fandem.cl;

import org.junit.After;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MainIntegrationTest {

	private static final String IP_ADDRESS = "127.0.0.1";
	private static final String SNIFF_IP_ADDRESS = "127.0.0.2";

	@Test
	public void transferTest() throws Exception {
		test(String.format("receive -d=./ -peer=%s:8081", IP_ADDRESS));
	}

	// run these test individually (weird but when we run all test, this ne fails)
	@Test
	public void transferWithSniffTest() throws Exception {
		System.setIn(new ByteArrayInputStream("y\n".getBytes()));
		test("receive -ip=" + SNIFF_IP_ADDRESS);
	}

	private void test(String receiveArgs) throws Exception {
		String filePath = getClass().getClassLoader()
				.getResource("file.txt")
				.getFile();

		new Thread(() -> Main.main(
				String.format("send %s -ip=%s -p=8081", filePath, IP_ADDRESS)
						.split(" "))).start();
		Thread.sleep(1000);

		Main.main(receiveArgs.split(" "));

		File originFile = new File(URLDecoder.decode(filePath, "UTF-8"));

		File file = new File("./file.txt");

		assertNotNull("Shouldn't be null", file);
		assertTrue("Didn't correctly downloaded file", file.exists());
		assertTrue("Content of received file differs from origin file",
				contentEquals(originFile, file));
	}

	@After
	public void dispose() {
		File file = new File("./file.txt");
		if (file.exists()) {
			file.delete();
		}
	}

	private boolean contentEquals(File f1, File f2) throws IOException {
		InputStream is1 = new FileInputStream(f1);
		InputStream is2 = new FileInputStream(f2);

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
