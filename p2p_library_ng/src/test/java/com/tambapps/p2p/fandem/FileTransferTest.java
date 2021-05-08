package com.tambapps.p2p.fandem;

import static org.junit.Assert.assertTrue;

import com.tambapps.p2p.speer.Peer;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FileTransferTest {

  private static final File FILE = new File("./file-received.txt");
  private static final File ORIGIN_FILE;
  private static final Peer SENDER_PEER;

  static {
    try {
      ORIGIN_FILE = new File(URLDecoder.decode(FileTransferTest.class.getClassLoader()
          .getResource("./file.txt").getFile(), StandardCharsets.UTF_8.name()));
      SENDER_PEER = Peer.of(InetAddress.getLocalHost(), 8081);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    FILE.deleteOnExit();
  }

  private final FileSender sender = new FileSender(SENDER_PEER);
  private final FileReceiver receiver = new FileReceiver();
  private final ExecutorCompletionService<Boolean>
      completionService = new ExecutorCompletionService<>(Executors.newFixedThreadPool(2));

  @Test
  public void transferTest() throws Exception {
    completionService.submit(() -> {
      sender.send(ORIGIN_FILE);
      return true;
    });
    Thread.sleep(250);

    completionService.submit(() -> {
      receiver.receiveFrom(SENDER_PEER, FILE);
      return true;
    });

    for (int i = 0; i < 2; i++) {
      assertTrue(completionService.poll(2, TimeUnit.SECONDS).get());
    }
    assertTrue(contentEquals(ORIGIN_FILE, FILE));
  }

  private boolean contentEquals(File f1, File f2) throws IOException {
    FileInputStream is1 = new FileInputStream(f1);
    FileInputStream is2 = new FileInputStream(f2);
    assertTrue(f1.length() > 0);
    assertTrue(f2.length() > 0);
    int EOF = -1;
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
