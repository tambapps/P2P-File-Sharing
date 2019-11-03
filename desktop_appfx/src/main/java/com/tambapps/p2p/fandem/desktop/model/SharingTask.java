package com.tambapps.p2p.fandem.desktop.model;

import com.tambapps.p2p.fandem.Peer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.File;

public class SharingTask {
  public final ObjectProperty<Peer> peer = new SimpleObjectProperty<>();
  public final ObjectProperty<Peer> remotePeer = new SimpleObjectProperty<>();
  public final LongProperty bytesProcessed = new SimpleLongProperty();
  public final LongProperty totalBytes = new SimpleLongProperty();
  public final DoubleProperty percentage = new SimpleDoubleProperty();
  public ObjectProperty<File> file = new SimpleObjectProperty<>();
  public final boolean sender;
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
