package com.tambapps.file_sharing;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
            File file = receiver.receiveFrom(sender.getIp(), sender.getPort());
            file.deleteOnExit();
            return file;
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
                contentEquals(originFile, file));
        assertEquals("Progress should be 100", 100, sender.getProgress());
        assertEquals("Progress should be 100", 100, receiver.getProgress());
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