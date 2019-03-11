package com.tambapps.p2p.file_sharing.concurrent;

import java.util.concurrent.Callable;

public interface SharingCallable extends Callable<Boolean> {

  void cancel();

}
