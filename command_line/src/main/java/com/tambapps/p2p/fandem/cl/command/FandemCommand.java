package com.tambapps.p2p.fandem.cl.command;

import com.tambapps.p2p.fandem.util.FileUtils;
import com.tambapps.p2p.fandem.util.TransferListener;
import com.tambapps.p2p.speer.Peer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public abstract class FandemCommand implements TransferListener {

  private final Properties textProperties;


  public FandemCommand(String propertiesFileName) {
    this.textProperties = new Properties();
    try (InputStream inputStream = FandemCommand.class.getResourceAsStream("/" + propertiesFileName)) {
      textProperties.load(inputStream);
    } catch (IOException e) {
      System.err.println("Error while attempting to read properties: " + e.getMessage());
      System.exit(1);
    }
  }

  public abstract void execute();

  @Override
  public void onConnected(Peer selfPeer, Peer remotePeer) {
    System.out.println("Connected to peer " + remotePeer);
  }

  @Override
  public void onTransferStarted(String fileName, long fileSize) {
    System.out.format(textProperties.getProperty("file_processed"), fileName).println();
    System.out.print(progressString(0L, fileSize));
  }

  @Override
  public void onProgressUpdate(String fileName, int progress, long bytesProcessed,
                               long totalBytes) {
    System.out.print(progressString(bytesProcessed, totalBytes));
  }

  private String progressString(long bytesProcessed, long totalBytes) {
    return textProperties.getProperty("percentage_processed")
        .formatted(
            FileUtils.toFileSize(bytesProcessed),
            FileUtils.toFileSize(totalBytes));
  }
}
