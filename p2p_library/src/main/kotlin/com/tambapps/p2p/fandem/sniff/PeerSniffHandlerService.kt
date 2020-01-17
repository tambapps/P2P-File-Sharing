package com.tambapps.p2p.fandem.sniff

import com.tambapps.p2p.fandem.Peer
import java.io.IOException
import java.net.ServerSocket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future


class PeerSniffHandlerService(private val executorService: ExecutorService,
                              private val sniffHandler: PeerSniffHandler) {
  @Volatile
  private var serverSocket: ServerSocket? = null
  private var future: Future<*>? = null

  constructor(executorService: ExecutorService, peer: Peer, deviceName: String, fileName: String):
      this(executorService, PeerSniffHandler(peer, deviceName, fileName))

  fun start() {
    future = executorService.submit {
      try {
        serverSocket = sniffHandler.newServerSocket()
        sniffHandler.handleSniff(serverSocket!!)
      } catch (e: IOException) {

      }
    }
  }

  fun stop() {
    if (serverSocket != null) {
      try {
        serverSocket!!.close()
      } catch (e: IOException) { }
      serverSocket = null
    }
    if (future != null) {
      val future: Future<*> = this.future!!
      if (!future.isCancelled) {
        future.cancel(true)
      }
      this.future = null
    }
  }

  fun setFileName(fileName: String) {
    this.sniffHandler.setFileName(fileName)
  }

}
