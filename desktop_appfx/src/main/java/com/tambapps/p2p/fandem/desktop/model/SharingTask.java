package com.tambapps.p2p.fandem.desktop.model;

import com.tambapps.p2p.fandem.Peer;

import java.io.File;

public class SharingTask {
  public final boolean sender;
  public Peer peer;
  public Peer remotePeer;
  public long bytesProcessed;
  public long totalBytes;
  public double percentage;
  public File file;
  private Runnable canceler;
  public boolean canceled = false;

  public SharingTask(boolean sender) {
    this.sender = sender;
  }

  public void setCanceler(Runnable canceler) {
    this.canceler = canceler;
  }

  public void cancel() {
    if (canceled) {
      return;
    }
    canceler.run();
    canceled = true;
  }

}
