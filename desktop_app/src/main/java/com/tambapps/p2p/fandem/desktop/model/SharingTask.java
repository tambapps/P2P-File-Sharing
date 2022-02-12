package com.tambapps.p2p.fandem.desktop.model;

import java.io.File;
import java.util.List;

public class SharingTask {
  public final boolean sender;
  public List<File> files;
  public boolean canceled = false;
  private Runnable canceler;

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
