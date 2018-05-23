package com.tambapps.p2p.peer_transfer.desktop;

import com.tambapps.p2p.file_sharing.IOUtils;
import org.junit.After;
import org.junit.Test;


import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FileSharingApplicationTest {

	@Test
	public void transferTest() throws IOException, InterruptedException {
		String filePath = getClass().getClassLoader()
				.getResource("file.txt")
				.getFile();
		new Thread(() -> FileSharingApplication.main(("-send -filePath=" + filePath + " -ip=127.0.0.1:8081")
                .split(" "))).start();
		Thread.sleep(1000);
		FileSharingApplication.main(("-receive -downloadPath=./ -peer=127.0.0.1:8081").split(" "));

		File originFile = new File(URLDecoder.decode(filePath, "UTF-8"));

		File file = new File("./file.txt");

		assertNotNull("Shouldn't be null", file);
		assertTrue("Didn't correctly downloaded file", file.exists());
		assertTrue("Content of received file differs from origin file",
				IOUtils.contentEquals(originFile, file));
	}

	@After
	public void dispose() {
		File file = new File("./file.txt");
		if (file.exists()) {
			file.delete();
		}
	}

}
