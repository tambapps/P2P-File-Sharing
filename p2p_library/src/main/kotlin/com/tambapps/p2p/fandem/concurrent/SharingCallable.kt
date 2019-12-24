package com.tambapps.p2p.fandem.concurrent

import java.util.concurrent.Callable

interface SharingCallable : Callable<Boolean?> {
  fun cancel()
}