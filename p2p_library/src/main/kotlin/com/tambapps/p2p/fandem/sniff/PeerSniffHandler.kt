package com.tambapps.p2p.fandem.sniff

import com.tambapps.p2p.fandem.Peer
import com.tambapps.p2p.fandem.io.CustomDataOutputStream
import com.tambapps.p2p.fandem.util.IPUtils
import java.io.IOException
import java.net.ServerSocket

class PeerSniffHandler(private val peer: Peer,
                       private val deviceName: String,
                       private val fileName: String) {

  constructor(port: Int, deviceName: String, fileName: String) :
      this(Peer.of(IPUtils.ipAddress, port), deviceName, fileName)

  @Throws(IOException::class)
  fun handleSniff() {
    ServerSocket(PeerSniffer.PORT, SOCKET_BACKLOG, peer.ip).use { serverSocket ->
      handleSniffServerSocket(serverSocket)
    }
  }

  @Throws(IOException::class)
  internal fun handleSniffServerSocket(serverSocket: ServerSocket) {
    for (i in 0 until SOCKET_BACKLOG) {
      val socket = serverSocket.accept()
      CustomDataOutputStream(socket.getOutputStream()).use { dis ->
        dis.writeString(deviceName)
        dis.writeInt(peer.port)
        dis.writeString(fileName)
      }
    }

  }
  companion object {
    private const val SOCKET_BACKLOG = 50
  }
}