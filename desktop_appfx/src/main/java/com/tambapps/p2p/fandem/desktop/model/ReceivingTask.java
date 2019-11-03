package com.tambapps.p2p.fandem.desktop.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.File;

public class ReceivingTask extends SharingTask {

  public ObjectProperty<File> file = new SimpleObjectProperty<>();

}
