package com.tambapps.p2p.fandem;

import com.tambapps.p2p.fandem.task.ReceivingTask;
import com.tambapps.p2p.fandem.task.SendingTask;
import com.tambapps.p2p.fandem.util.FileUtils;

import org.junit.Before;
import org.junit.Test;

import java.io.*;

import java.net.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FileTransferTest {

  private static final String FILE_PATH = "./file.txt";
  private static final File FILE = new File("./file.txt");
  private static final File ORIGIN_FILE;
  private static final Peer SENDER_PEER;

    static {
        try {
            SENDER_PEER = Peer.of(InetAddress.getLocalHost(), 8081);
        } catch (UnknownHostException e) {
            throw new RuntimeException("Couldn't start tests");
        }
        FILE.deleteOnExit();

      try {
        ORIGIN_FILE = new File(FileUtils.decodePath((FileTransferTest.class.getClassLoader()
          .getResource(FILE_PATH)
          .getFile())));
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException("Couldn't start tests");
      }
    }

    private SendingTask sender;
    private ReceivingTask receiver;
    private ExecutorCompletionService<Boolean> completionService =
            new ExecutorCompletionService<>(Executors.newFixedThreadPool(2));

    @Before
    public void init() throws IOException {
        sender = new SendingTask(SENDER_PEER, 1000);
        receiver = new ReceivingTask(FILE);
    }

    @Test
    public void transferTest() throws IOException, InterruptedException, ExecutionException {
        completionService.submit(() -> {
            sender.send(ORIGIN_FILE);
            return true;
        });

        Thread.sleep(250);

        completionService.submit(() -> {
            receiver.receiveFrom(SENDER_PEER);
            return true;
        });

        //check if tasks finished without throwing an exception
        for (int i = 0; i < 2; i++) {
            Boolean success = completionService.poll(2, TimeUnit.SECONDS).get();
            assertEquals("A task didn't ended well", Boolean.TRUE, success);
        }

        File file = receiver.getOutputFile();
        assertNotNull("Shouldn't be null", file);
        assertTrue("Didn't correctly downloaded file", file.exists());
        assertTrue("Content of received file differs from origin file",
                contentEquals(ORIGIN_FILE, file));
    }

    @Test
    public void cancelReceiver() throws Exception {
        completionService.submit(() -> {
            //fake file sender
            ServerSocket serverSocket = new ServerSocket(SENDER_PEER.getPort(), 0, SENDER_PEER.getIp());
            serverSocket.accept();
            return true;
        });

        Thread.sleep(250);
        completionService.submit(() -> {
            receiver.receiveFrom(SENDER_PEER.getIp(), SENDER_PEER.getPort());
            return true;
        });
        receiver.cancel();

        Thread.sleep(250);


        for (int i = 0; i < 2; i++) {
            Boolean success = completionService.poll(10, TimeUnit.SECONDS).get();
            assertEquals("A task didn't ended well", Boolean.TRUE, success);
        }

        assertTrue("Should be true", receiver.isCanceled());
    }

    @Test
    public void cancelSenderWhileWaiting() throws Exception {
        completionService.submit(() -> {
            sender.send(ORIGIN_FILE);
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
            sender.send(ORIGIN_FILE);
            return true;
        });

        Thread.sleep(250);

        completionService.submit(() -> {
            Socket socket = new Socket(SENDER_PEER.getIp(), SENDER_PEER.getPort());
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