package com.tambapps.p2p.file_sharing.concurrent;

import java.util.concurrent.Callable;

public interface ShareCallable extends Callable<Boolean> {

  void cancel();

}
