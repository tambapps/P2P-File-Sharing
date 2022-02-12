package com.tambapps.p2p.fandem;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tambapps.p2p.fandem.util.FileProvider;
import com.tambapps.p2p.speer.Peer;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FileTransferTest {

  private static final File DIRECTORY = new File("./");
  private static final File ORIGIN_FILE_1;
  private static final File ORIGIN_FILE_2;
  private static final List<File> ORIGIN_FILES;

  private static final Peer SENDER_PEER;

  static {
    try {
      ORIGIN_FILE_1 = new File(URLDecoder.decode(FileTransferTest.class.getClassLoader()
          .getResource("./file1.txt").getFile(), StandardCharsets.UTF_8.name()));
      ORIGIN_FILE_2 = new File(URLDecoder.decode(FileTransferTest.class.getClassLoader()
          .getResource("./file2.txt").getFile(), StandardCharsets.UTF_8.name()));
      ORIGIN_FILES = Arrays.asList(ORIGIN_FILE_1, ORIGIN_FILE_2);
      SENDER_PEER = Peer.of(InetAddress.getLocalHost(), 8081);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private final FileSender sender = new FileSender(SENDER_PEER);
  private final FileReceiver receiver = new FileReceiver();
  private final ExecutorCompletionService<Boolean>
      completionService = new ExecutorCompletionService<>(Executors.newFixedThreadPool(2));

  @Test
  public void transferTest() throws Exception {
    completionService.submit(() -> {
      sender.sendFiles(ORIGIN_FILES);
      return true;
    });
    Thread.sleep(250);

    completionService.submit(() -> {
      receiver.receiveFrom(SENDER_PEER, (FileProvider) (name) -> new File(DIRECTORY, "received-" + name));
      return true;
    });

    for (int i = 0; i < 2; i++) {
      assertTrue(completionService.poll(10, TimeUnit.SECONDS).get());
    }
    assertTrue(contentEquals(ORIGIN_FILE_1, new File(DIRECTORY, "received-" + ORIGIN_FILE_1.getName())));
    assertTrue(contentEquals(ORIGIN_FILE_2, new File(DIRECTORY, "received-" + ORIGIN_FILE_2.getName())));
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
