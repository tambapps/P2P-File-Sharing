package com.tambapps.p2p.fandem.concurrent

import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class FutureShare(private val future: Future<Boolean?>, private val callable: SharingCallable) : Future<Boolean> {
  override fun cancel(b: Boolean): Boolean {
    callable.cancel()
    return future.cancel(b)
  }

  override fun isCancelled(): Boolean {
    return future.isCancelled
  }

  override fun isDone(): Boolean {
    return future.isDone
  }

  @Throws(InterruptedException::class, ExecutionException::class)
  override fun get(): Boolean {
    return future.get()?: false
  }

  @Throws(InterruptedException::class, ExecutionException::class, TimeoutException::class)
  override fun get(l: Long, timeUnit: TimeUnit): Boolean {
    return future[l, timeUnit]?: false
  }

}