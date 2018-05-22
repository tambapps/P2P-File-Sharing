package com.tambapps.file_sharing;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import java.net.URLDecoder;
import java.util.concurrent.*;

public class FileTransferTest {

    private FileSender sender;
    private FileReceiver receiver;

    @Before
    public void init() throws IOException {
        sender = new FileSender("127.0.0.1", 1000);
        receiver = new FileReceiver("./");
    }

    @Test
    public void transferTest() throws IOException, InterruptedException, ExecutionException {
        File originFile = new File(URLDecoder.decode(getClass().getClassLoader()
                .getResource("file.txt")
                .getFile(), "UTF-8"));
        ExecutorService executor = Executors.newFixedThreadPool(2);
        ExecutorCompletionService<File> completionService = new ExecutorCompletionService<>(executor);

        completionService.submit(() -> {
            receiver.receiveFrom(sender.getIp(), sender.getPort());
            return receiver.getReceivedFile();
        });

        executor.submit(() -> {
            try {
                sender.send(originFile);
            } catch (IOException e) {
                throw new RuntimeException("Error while sending", e);
            }
            return null;
        });

        File file = completionService.poll(2, TimeUnit.SECONDS).get();
        if (file == null) {
            file = completionService.poll(2, TimeUnit.SECONDS).get();
        }

        assertNotNull("Shouldn't be null", file);
        assertTrue("Didn't correctly downloaded file", file.exists());
        assertTrue("Content of received file differs from origin file",
                IOUtils.contentEquals(originFile, file));
        assertEquals("Progress should be 100", 100, sender.getProgress());
        assertEquals("Progress should be 100", 100, receiver.getProgress());

        assertEquals("Bytes sent should be equal to file size", file.length(), sender.getBytesSent());
        assertEquals("Bytes received should be equal to file size", file.length(), receiver.getBytesReceived());
    }

    @After
    public void dispose() {
        File file = new File("./file.txt");
        if (file.exists()) {
            file.delete();
        }
    }
}