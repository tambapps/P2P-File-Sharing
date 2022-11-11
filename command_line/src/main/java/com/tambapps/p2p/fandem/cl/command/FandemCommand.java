package com.tambapps.p2p.fandem.cl.command;

import com.tambapps.p2p.fandem.cl.FandemMode;
import com.tambapps.p2p.fandem.util.FileUtils;
import com.tambapps.p2p.fandem.util.TransferListener;
import com.tambapps.p2p.speer.Peer;

public abstract class FandemCommand implements TransferListener {

  private final FandemMode mode;

  public FandemCommand(FandemMode mode) {
    this.mode = mode;
  }

  public abstract void execute();

  @Override
  public void onConnected(Peer selfPeer, Peer remotePeer) {
    System.out.println("Connected to peer " + remotePeer);
  }

  @Override
  public void onTransferStarted(String fileName, long fileSize) {
    String verb = switch (mode) {
      case SEND -> "Sending";
      case RECEIVE -> "Receiving";
    };
    System.out.format("\n%s %s", verb, fileName).println();
    System.out.print(progressString(0L, fileSize));
  }

  @Override
  public void onProgressUpdate(String fileName, int progress, long bytesProcessed,
                               long totalBytes) {
    System.out.print(progressString(bytesProcessed, totalBytes));
  }

  private String progressString(long bytesProcessed, long totalBytes) {
    String verb = switch (mode) {
      case SEND -> "Sent";
      case RECEIVE -> "Received";
    };
    return "\r%s %s / %s".formatted(verb,
        FileUtils.toFileSize(bytesProcessed),
        FileUtils.toFileSize(totalBytes));
  }
}
