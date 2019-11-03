package com.tambapps.p2p.fandem.desktop.model;

import com.tambapps.p2p.fandem.Peer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.File;

public class SharingTask {
  public final ObjectProperty<Peer> peer = new SimpleObjectProperty<>();
  public final ObjectProperty<Peer> remotePeer = new SimpleObjectProperty<>();
  public final LongProperty bytesProcessed = new SimpleLongProperty();
  public final LongProperty totalBytes = new SimpleLongProperty();
  public final IntegerProperty percentage = new SimpleIntegerProperty();
  public final BooleanPropertyBase canceled = new SimpleBooleanProperty(false);
  public ObjectProperty<File> file = new SimpleObjectProperty<>();
  public final boolean sender;
  private Runnable canceler;

  public SharingTask(boolean sender) {
    this.sender = sender;
  }

  public void setCanceler(Runnable canceler) {
    this.canceler = canceler;
  }

  public void cancel() {
    if (canceled.get()) {
      return;
    }
    canceler.run();
    canceled.set(true);
  }
}
