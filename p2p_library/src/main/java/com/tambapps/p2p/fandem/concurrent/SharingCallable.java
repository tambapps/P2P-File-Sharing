package com.tambapps.p2p.fandem.concurrent;

import java.util.concurrent.Callable;

public interface SharingCallable extends Callable<Boolean> {

  void cancel();

}
