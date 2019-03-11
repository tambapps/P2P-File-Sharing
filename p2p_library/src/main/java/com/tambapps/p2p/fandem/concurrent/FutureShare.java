package com.tambapps.p2p.fandem.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FutureShare implements Future<Boolean> {

  private final Future<Boolean> future;
  private final SharingCallable callable;

  public FutureShare(Future<Boolean> future, SharingCallable callable) {
    this.future = future;
    this.callable = callable;
  }

  @Override
  public boolean cancel(boolean b) {
    callable.cancel();
    return future.cancel(b);
  }

  @Override
  public boolean isCancelled() {
    return future.isCancelled();
  }

  @Override
  public boolean isDone() {
    return future.isDone();
  }

  @Override
  public Boolean get() throws InterruptedException, ExecutionException {
    return future.get();
  }

  @Override
  public Boolean get(long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
    return future.get(l, timeUnit);
  }
}