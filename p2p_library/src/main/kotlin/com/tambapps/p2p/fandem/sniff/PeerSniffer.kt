package com.tambapps.p2p.fandem.sniff

import com.tambapps.p2p.fandem.Peer.Companion.of
import com.tambapps.p2p.fandem.io.CustomDataInputStream
import com.tambapps.p2p.fandem.util.IPUtils
import java.io.IOException
import java.net.ConnectException
import java.net.InetAddress
import java.net.Socket


class PeerSniffer(private val listener: SniffListener,
                                      private val ip: ByteArray,
                                      private val timeout: Int) {

  @Throws(IOException::class)
  constructor(listener: SniffListener,
              inetAddress: InetAddress): this(listener, inetAddress.address, DEFAULT_SNIFFER_TIMEOUT)

  @Throws(IOException::class)
  constructor(listener: SniffListener): this(listener, IPUtils.ipAddress.address, DEFAULT_SNIFFER_TIMEOUT)

  interface SniffListener {
    fun onPeerFound(peer: SniffPeer)
    fun onError(e: IOException)
    fun onEnd()
  }

  private var oneFound: Boolean = false

  fun sniffWhileNoneFound() {
    try {
      while (!oneFound) {
        doSniff()
      }
      listener.onEnd()
    } catch (e: IOException) {
      listener.onError(e)
    }
  }

  fun sniff() {
    try {
      doSniff()
      listener.onEnd()
    } catch (e: IOException) {
      listener.onError(e)
    }
  }

  @Throws(IOException::class)
  private fun doSniff() {

    val tempIp = ip.copyOf()
    for (i in 0..254) {
      tempIp[3] = i.toByte()
      val address = InetAddress.getByAddress(tempIp)
      if ((!(ip contentEquals tempIp)) && address.isReachable(timeout)) {
        try {
          Socket(address, PORT).use { socket ->
            CustomDataInputStream(socket.getInputStream()).use { dis ->
              val deviceName: String = dis.readString()
              val senderPort: Int = dis.readInt()
              val fileName: String = dis.readString()
              listener.onPeerFound(SniffPeer(of(address, senderPort), deviceName, fileName))
              if (!oneFound) {
                oneFound = true
              }
            }
          }
        } catch (e: ConnectException) {
          // not a peer
        }
      }
    }
  }

  companion object {
    const val DEFAULT_SNIFFER_TIMEOUT = 100
    const val PORT = 7999

  }
}