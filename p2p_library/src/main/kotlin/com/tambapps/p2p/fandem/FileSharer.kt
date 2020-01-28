package com.tambapps.p2p.fandem

import com.tambapps.p2p.fandem.concurrent.FutureShare
import com.tambapps.p2p.fandem.concurrent.SharingCallable
import com.tambapps.p2p.fandem.listener.SharingErrorListener
import com.tambapps.p2p.fandem.listener.TransferListener
import com.tambapps.p2p.fandem.task.FileProvider
import com.tambapps.p2p.fandem.task.ReceivingTask
import com.tambapps.p2p.fandem.task.SendingTask
import com.tambapps.p2p.fandem.util.FileUtils
import java.io.File
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

/**
 * Util class to share files without having to deal with tasks
 */
class FileSharer(private val executorService: ExecutorService) {
  fun sendFile(filePath: String, peer: Peer, errorListener: SharingErrorListener): Future<Boolean> {
    return sendFile(File(filePath), peer, SendingTask.DEFAULT_SOCKET_TIMEOUT, null, errorListener)
  }

  fun sendFile(filePath: String, peer: Peer, transferListener: TransferListener?, errorListener: SharingErrorListener): Future<Boolean> {
    return sendFile(File(filePath), peer, SendingTask.DEFAULT_SOCKET_TIMEOUT, transferListener, errorListener)
  }

  fun sendFile(file: File, peer: Peer, errorListener: SharingErrorListener): Future<Boolean> {
    return sendFile(file, peer, SendingTask.DEFAULT_SOCKET_TIMEOUT, null, errorListener)
  }

  fun sendFile(file: File, peer: Peer, transferListener: TransferListener?, errorListener: SharingErrorListener): Future<Boolean> {
    return sendFile(file, peer, SendingTask.DEFAULT_SOCKET_TIMEOUT, transferListener, errorListener)
  }

  fun sendFile(file: File, peer: Peer, socketTimout: Int, transferListener: TransferListener?, errorListener: SharingErrorListener): Future<Boolean> {
    require(file.isFile) { file.path + " isn't a file" }
    val callable = SendCallable(file, peer, socketTimout, transferListener, errorListener)
    return FutureShare(executorService.submit(callable), callable)
  }

  fun receiveFile(fileProvider: FileProvider, peer: Peer, transferListener: TransferListener?, errorListener: SharingErrorListener): Future<Boolean> {
    val callable = ReceiveCallable(fileProvider, peer, transferListener, errorListener)
    return FutureShare(executorService.submit(callable), callable)
  }

  fun receiveFile(file: File, peer: Peer, errorListener: SharingErrorListener): Future<Boolean> {
    return receiveFile(FileUtils.staticFileProvider(file), peer, null, errorListener)
  }

  fun receiveFile(file: File, peer: Peer, transferListener: TransferListener, errorListener: SharingErrorListener): Future<Boolean> {
    return receiveFile(FileUtils.staticFileProvider(file), peer, transferListener, errorListener)
  }

  fun receiveFile(filePath: String, peer: Peer, errorListener: SharingErrorListener): Future<Boolean> {
    return receiveFile(File(filePath), peer, errorListener)
  }

  fun receiveFile(filePath: String, peer: Peer, transferListener: TransferListener, errorListener: SharingErrorListener): Future<Boolean> {
    return receiveFile(File(filePath), peer, transferListener, errorListener)
  }

  fun receiveFileInDirectory(directory: File, peer: Peer, transferListener: TransferListener?,
                             errorListener: SharingErrorListener): Future<Boolean> {
    require(directory.exists()) { "$directory doesn't exist" }
    require(directory.isDirectory) { "$directory isn't a directory" }
    return receiveFile(FileUtils.availableFileInDirectoryProvider(directory),
            peer, transferListener, errorListener)
  }

  fun receiveFileInDirectory(directory: File, peer: Peer, errorListener: SharingErrorListener): Future<Boolean> {
    return receiveFileInDirectory(directory, peer, null, errorListener)
  }
}

private class SendCallable internal constructor(private val file: File, peer: Peer, socketTimout: Int, transferListener: TransferListener?, private val errorListener: SharingErrorListener) : SharingCallable {
  private val task: SendingTask = SendingTask(transferListener, peer, socketTimout)
  override fun cancel() {
    task.cancel()
  }

  override fun call(): Boolean {
    return try {
      task.send(file)
      true
    } catch (e: IOException) {
      errorListener.onError(e)
      false
    }
  }
}

private class ReceiveCallable internal constructor(fileProvider: FileProvider, private val peer: Peer, transferListener: TransferListener?, private val errorListener: SharingErrorListener) : SharingCallable {
  private val task: ReceivingTask = ReceivingTask(transferListener, fileProvider)
  override fun cancel() {
    task.cancel()
  }

  override fun call(): Boolean {
    return try {
      task.receiveFrom(peer)
      true
    } catch (e: IOException) {
      errorListener.onError(e)
      false
    }
  }
}