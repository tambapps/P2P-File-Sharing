package com.tambapps.p2p.fandem.task

import com.tambapps.p2p.fandem.Peer
import com.tambapps.p2p.fandem.exception.TransferCanceledException
import com.tambapps.p2p.fandem.io.CustomDataOutputStream
import com.tambapps.p2p.fandem.listener.SendingListener
import com.tambapps.p2p.fandem.listener.TransferListener
import com.tambapps.p2p.fandem.util.FileUtils
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.net.SocketException

/**
 * A P2P Sending Task
 */
class SendingTask(transferListener: TransferListener?, private val peer: Peer,
                  private val socketTimeout: Int, private val bufferSize: Int) : SharingTask(transferListener) {
  private var serverSocket //as class variable to allow cancel
      : ServerSocket? = null

  @JvmOverloads
  constructor(address: InetAddress, port: Int = 0, socketTimeout: Int = 0, bufferSize: Int = BUFFER_SIZE) : this(null, Peer.of(address, port), socketTimeout, bufferSize) {
  }

  constructor(transferListener: TransferListener?, address: InetAddress,
              port: Int, socketTimeout: Int, bufferSize: Int) : this(transferListener, Peer.of(address, port), socketTimeout, bufferSize) {
  }

  constructor(senderPeer: Peer, socketTimeout: Int) : this(senderPeer.ip, senderPeer.port, socketTimeout) {}
  constructor(transferListener: TransferListener?, senderPeer: Peer, socketTimeout: Int) : this(transferListener, senderPeer.ip, senderPeer.port, socketTimeout, BUFFER_SIZE) {}
  constructor(transferListener: TransferListener?, senderPeer: Peer) : this(transferListener, senderPeer.ip, senderPeer.port, DEFAULT_SOCKET_TIMEOUT, BUFFER_SIZE) {}
  constructor(senderPeer: Peer) : this(senderPeer.ip, senderPeer.port, DEFAULT_SOCKET_TIMEOUT) {}

  @Throws(IOException::class)
  fun send(filePath: String) {
    send(File(FileUtils.decodePath(filePath)))
  }

  @Throws(IOException::class)
  fun send(file: File) {
    FileInputStream(file).use { fileInputStream -> send(fileInputStream, file.name, file.length()) }
  }

  @Throws(IOException::class)
  fun sendCancelSilent(fis: InputStream?, fileName: String,
           fileSize: Long) {
    try {
        send(fis, fileName, fileSize)
    } catch (e: TransferCanceledException) {

    }
  }

  @Throws(IOException::class)
  fun send(fis: InputStream?, fileName: String,
           fileSize: Long) {
    try {
      createServerSocket(fileName).use { serverSocket ->
        serverSocket.accept().use { socket ->
          transferListener?.onConnected(peer, Peer.of(socket), fileName, fileSize)
          CustomDataOutputStream(socket.getOutputStream()).use { dos ->
            dos.writeLong(fileSize)
            dos.writeInt(bufferSize)
            dos.writeString(fileName)
            share(bufferSize, fis!!, dos, fileSize)
          }
        }
      }
    } catch (e: SocketException) { //socket closed because of cancel()
      cancel()
      throw TransferCanceledException(e)
    }
  }

  @Throws(IOException::class)
  private fun createServerSocket(fileName: String): ServerSocket {
    val serverSocket = ServerSocket(peer.port, 1, peer.ip)
    this.serverSocket = serverSocket
    if (transferListener != null && transferListener is SendingListener) {
      transferListener.onStart(Peer.of(serverSocket.inetAddress, serverSocket.localPort), fileName)
    }
    serverSocket.soTimeout = socketTimeout
    return serverSocket
  }

  override fun cancel() {
    if (serverSocket != null) {
      try {
        serverSocket!!.close()
      } catch (ignored: IOException) {
      } finally {
        serverSocket = null
      }
    }
    super.cancel()
  }

  companion object {
    const val BUFFER_SIZE = 4096
    const val DEFAULT_SOCKET_TIMEOUT = 30000
  }

}