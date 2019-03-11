package com.tambapps.p2p.fandem.cl;

import org.junit.After;
import org.junit.Test;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MainTest {

	@Test
	public void transferTest() throws IOException, InterruptedException {
		String filePath = getClass().getClassLoader()
				.getResource("file.txt")
				.getFile();

		new Thread(() -> Main.main(("send " + filePath + " -ip=127.0.0.1 -port=8081")
                .split(" "))).start();
		Thread.sleep(1000);

		Main.main("receive -download=./ -peer=127.0.0.1:8081".split(" "));

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
