package com.tambapps.p2p.file_sharing;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class FileTransferTest {

    private FileSender sender;
    private FileReceiver receiver;

    private final File originFile;

    private ExecutorCompletionService<Boolean> completionService =
            new ExecutorCompletionService<>(Executors.newFixedThreadPool(2));

    public FileTransferTest() throws UnsupportedEncodingException {
        originFile  = new File(URLDecoder.decode(getClass().getClassLoader()
                .getResource("file.txt")
                .getFile(), "UTF-8"));
    }

    @Before
    public void init() throws IOException {
        sender = new FileSender(InetAddress.getLocalHost(), 1000);
        receiver = new FileReceiver("./");
    }

    @Test
    public void transferTest() throws IOException, InterruptedException, ExecutionException {
        completionService.submit(() -> {
            receiver.receiveFrom(sender.getIp(), sender.getPort());
            return true;
        });

        completionService.submit(() -> {
            sender.send(originFile);
            return true;
        });

        //check if tasks finished without throwing an exception
        for (int i = 0; i < 2; i++) {
            Boolean success = completionService.poll(2, TimeUnit.SECONDS).get();
            assertEquals("A task didn't ended well", Boolean.TRUE, success);
        }

        File file = receiver.getReceivedFile();
        assertNotNull("Shouldn't be null", file);
        assertTrue("Didn't correctly downloaded file", file.exists());
        assertTrue("Content of received file differs from origin file",
                IOUtils.contentEquals(originFile, file));
        assertEquals("Progress should be 100", 100, sender.getProgress());
        assertEquals("Progress should be 100", 100, receiver.getProgress());

        assertEquals("Bytes sent should be equal to file size", file.length(), sender.getBytesSent());
        assertEquals("Bytes received should be equal to file size", file.length(), receiver.getBytesReceived());
    }

    @Test
    public void cancelReceiver() throws Exception {
        completionService.submit(() -> {
            //fake file sender
            ServerSocket serverSocket = new ServerSocket(8080, 0, InetAddress.getLocalHost());
            serverSocket.accept();
            serverSocket.close();
            return true;
        });

        completionService.submit(() -> {
            receiver.receiveFrom(InetAddress.getLocalHost(), 8080);
            return true;
        });

        Thread.sleep(250);
        receiver.cancel();

        for (int i = 0; i < 2; i++) {
            Boolean success = completionService.poll(2, TimeUnit.SECONDS).get();
            assertEquals("A task didn't ended well", Boolean.TRUE, success);
        }

        assertTrue("Should be true", receiver.isCanceled());
        assertNull("Should be null", receiver.getReceivedFile());
    }

    @Test
    public void cancelSenderWhileWaiting() throws Exception {
        completionService.submit(() -> {
            sender.send(originFile);
            return true;
        });
        Thread.sleep(250);
        sender.cancel();

        assertEquals("This task didn't ended well", Boolean.TRUE,
                completionService.poll(2, TimeUnit.SECONDS).get());
        assertTrue("Should be true", sender.isCanceled());
    }

    @Test
    public void cancelSenderWhileSending() throws Exception {
        //closing without
        completionService.submit(() -> {
            sender.send(originFile);
            return true;
        });

        completionService.submit(() -> {
            Socket socket = new Socket(sender.getIp(), sender.getPort());
            socket.close();
            return true;
        });

        Thread.sleep(250);
        sender.cancel();

        for (int i = 0; i < 2; i++) {
            Boolean success = completionService.poll(2, TimeUnit.SECONDS).get();
            assertEquals("A task didn't ended well", Boolean.TRUE, success);
        }
        assertTrue("Should be true", sender.isCanceled());
    }

    @After
    public void dispose() {
        File file = new File("./file.txt");
        if (file.exists()) {
            file.delete();
        }
    }
}