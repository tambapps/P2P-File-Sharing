package com.tambapps.p2p.fandem.task

import com.tambapps.p2p.fandem.exception.SharingException
import com.tambapps.p2p.fandem.listener.TransferListener
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Task for sharing a file with another peer
 */
abstract class SharingTask internal constructor(val transferListener: TransferListener?) {
  @Volatile
  var isCanceled = false
    private set

  @Throws(IOException::class)
  fun share(bufferSize: Int, `is`: InputStream, os: OutputStream, totalBytes: Long) {
    val buffer = ByteArray(bufferSize)
    var count: Int
    var lastProgress = 0
    var bytesProcessed: Long = 0
    var progress: Int
    while (`is`.read(buffer).also { count = it } > 0) {
      if (isCanceled) {
        return
      }
      bytesProcessed += count.toLong()
      os.write(buffer, 0, count)
      progress = Math.min(MAX_PROGRESS - 1.toLong(), MAX_PROGRESS * bytesProcessed / totalBytes).toInt()
      if (progress != lastProgress) {
        lastProgress = progress
        transferListener?.onProgressUpdate(progress, bytesProcessed, totalBytes)
      }
    }
    if (bytesProcessed != totalBytes) {
      throw SharingException("Transfer was not properly finished")
    } else transferListener?.onProgressUpdate(MAX_PROGRESS, bytesProcessed, totalBytes)
  }

  open fun cancel() {
    isCanceled = true
  }

  companion object {
    const val MAX_PROGRESS = 100
  }

}