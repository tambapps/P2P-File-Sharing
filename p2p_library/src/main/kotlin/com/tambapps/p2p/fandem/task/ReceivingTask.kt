package com.tambapps.p2p.fandem.task

import com.tambapps.p2p.fandem.Peer
import com.tambapps.p2p.fandem.io.CustomDataInputStream
import com.tambapps.p2p.fandem.listener.ReceivingListener
import com.tambapps.p2p.fandem.listener.TransferListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.channels.SocketChannel

/**
 * A P2P Receive task
 */
class ReceivingTask(transferListener: TransferListener?, private val fileProvider: FileProvider) : SharingTask(transferListener) {
  @Volatile
  var outputFile: File? = null
    private set

  constructor(file: File?) : this(null, file) {}
  constructor(fileProvider: FileProvider) : this(null, fileProvider) {}
  constructor(transferListener: TransferListener?, file: File?) : this(transferListener, FileProvider { file }) {
  }

  @Throws(IOException::class)
  fun receiveFrom(address: InetAddress, port: Int) {
    receiveFrom(Peer.of(address, port))
  }

  @Throws(IOException::class)
  fun receiveFrom(peer: Peer) {
    SocketChannel.open().use { socketChannel ->
      socketChannel.connect(InetSocketAddress(peer.ip, peer.port))
      while (!socketChannel.isConnected) {
        try {
          Thread.sleep(2000)
        } catch (e: InterruptedException) {
          cancel()
          return
        }
        if (isCanceled) {
          return
        }
      }
      if (isCanceled) {
        return
      }
      socketChannel.socket().use { socket ->
        CustomDataInputStream(socket.getInputStream()).use { dis ->
          val totalBytes = dis.readLong()
          val bufferSize = dis.readInt()
          val fileName = dis.readString()
          transferListener?.onConnected(peer, Peer.of(socket.inetAddress, socket.port),
              fileName, totalBytes)
          outputFile = fileProvider.newFile(fileName)
          FileOutputStream(outputFile).use { fos -> share(bufferSize, dis, fos, totalBytes) }
          if (transferListener != null && transferListener is ReceivingListener) {
            transferListener.onEnd(outputFile)
          }
        }
      }
    }
  }

}