package com.tambapps.p2p.fandem

import java.net.InetAddress
import java.net.Socket
import java.net.UnknownHostException

/**
 * Representation of peer
 */

data class Peer private constructor(val ip: InetAddress, val port: Int) {
  override fun toString(): String {
    return ip.hostAddress.replace("/", "") + ":" + port
  }

  fun toHexString(): String {
    val address = ip.address
    val builder = StringBuilder()
    for (field in address) {
      builder.append(toHexString(field))
    }
    if (port != DEFAULT_PORT) {
      builder.append(toHexString(port - DEFAULT_PORT))
    }
    return builder.toString().toUpperCase()
  }

  private fun toHexString(n: Byte): String {
    val hex = n.toString(16)
    return if (hex.length == 1) "0$hex" else hex
  }

  private fun toHexString(n: Int): String {
    val hex = n.toString(16)
    return if (hex.length == 1) "0$hex" else hex
  }

  companion object {
    const val DEFAULT_PORT = 8081
    fun of(address: InetAddress, port: Int): Peer {
      return Peer(address, port)
    }

    fun of(socket: Socket): Peer {
      return Peer(socket.inetAddress, socket.port)
    }

    fun fromHexString(hexString: String): Peer {
      val address = ByteArray(4)
      for (i in address.indices) {
        address[i] = hexString.substring(i * 2, i * 2 + 2).toByte(16)
      }
      val port: Int =
          if(hexString.length == 10)
            DEFAULT_PORT + hexString.substring(8, 10).toInt(16)
          else
            DEFAULT_PORT
      return of(InetAddress.getByAddress(address), port)
    }

    @Throws(UnknownHostException::class)
    fun parse(peer: String): Peer {
      val index = peer.indexOf(":")
      require(index >= 0) { "peer is malformed" }
      return Peer(InetAddress.getByName(peer.substring(0, index)), peer.substring(index + 1).toInt())
    }

    @Throws(UnknownHostException::class)
    fun of(address: String?, port: Int): Peer {
      return of(InetAddress.getByName(address), port)
    }
  }


}