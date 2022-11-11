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
    System.out.format("\n%s %s", mode.ingString(), fileName).println();
    System.out.format(mode.progressFormat(), "0kb",
        FileUtils.toFileSize(fileSize));
  }

  @Override
  public void onProgressUpdate(String fileName, int progress, long bytesProcessed,
                               long totalBytes) {
    System.out.format(mode.progressFormat(),
        FileUtils.toFileSize(bytesProcessed),
        FileUtils.toFileSize(totalBytes));
  }

}
