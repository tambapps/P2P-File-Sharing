package com.tambapps.p2p.fandem

import com.tambapps.p2p.fandem.exception.NoPortAvailableException
import com.tambapps.p2p.fandem.util.IPUtils
import java.io.IOException
import java.lang.IllegalArgumentException
import java.net.InetAddress
import java.net.Socket
import java.net.SocketException
import java.net.UnknownHostException

/**
 * Representation of peer
 */

data class Peer private constructor(val ip: InetAddress, val port: Int) {

  private val ipString: String
    get() {
      return IPUtils.toString(ip)
    }

  override fun toString(): String {
    return "$ipString:$port"
  }

  fun toHexString(): String {
    val ipHex = IPUtils.toHexString(ip)
    return if (port != DEFAULT_PORT) {
      ipHex + IPUtils.toHexString(port - DEFAULT_PORT)
    } else {
      ipHex
    }
  }

  fun ipFields(): IntArray {
    return ipString.split('.').map { s -> s.toInt() }.toIntArray()
  }
  companion object {
    const val DEFAULT_PORT = 8081

    @JvmStatic
    fun of(address: InetAddress, port: Int): Peer {
      return Peer(address, port)
    }

    @JvmStatic
    fun of(socket: Socket): Peer {
      return Peer(socket.inetAddress, socket.port)
    }

    @JvmStatic
    @Throws(IllegalArgumentException::class)
    fun fromHexString(hexString: String): Peer {
      if (hexString.length != 8 && hexString.length != 10 ||
          !hexString.all { it.isDigit() || it.toUpperCase() in 'A'..'F' }) {
        throw IllegalArgumentException("$hexString is malformed");
      }
      val address = IntArray(4)
      for (i in address.indices) {
        address[i] = hexString.substring(i * 2, i * 2 + 2).toInt(16)
      }
      val port: Int =
          if(hexString.length == 10)
            DEFAULT_PORT + hexString.substring(8, 10).toInt(16)
          else
            DEFAULT_PORT
      return of(InetAddress.getByName(address.joinToString(prefix = "", separator = ".", postfix = "") { it.toString() }), port)
    }

    @JvmStatic
    @Throws(UnknownHostException::class)
    fun parse(peer: String): Peer {
      val index = peer.indexOf(":")
      require(index >= 0) { "peer is malformed" }
      return Peer(InetAddress.getByName(peer.substring(0, index)), peer.substring(index + 1).toInt())
    }

    @JvmStatic
    @Throws(UnknownHostException::class)
    fun of(address: String?, port: Int): Peer {
      return of(InetAddress.getByName(address), port)
    }

    @JvmStatic
    fun isCorrectPeerKey(peerKey: String?): Boolean {
      if (peerKey == null) {
        return false
      }
      val peerKey = peerKey.toUpperCase()
      if (peerKey.length != 8 && peerKey.length != 10) {
        return false
      }
      for (element in peerKey) {
        if (!(element in 'A'..'F' || element in '0'..'9')) {
          return false
        }
      }
      return true
    }

    @JvmStatic
    @get:Throws(IOException::class)
    val availablePeer: Peer?
      get() {
        val address = IPUtils.ipAddress
        val port: Int
        try {
          port = IPUtils.getAvailablePort(address)
        } catch (e: NoPortAvailableException) {
          return null
        }
        return of(address, port)
      }
  }

}