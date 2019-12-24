package com.tambapps.p2p.fandem

import java.net.InetAddress
import java.net.Socket
import java.net.UnknownHostException

/**
 * Representation of peer
 */

class Peer private constructor(val ip: InetAddress?, val port: Int) {
  override fun toString(): String {
    return ip!!.hostAddress.replace("/", "") + ":" + port
  }

  fun toHex(): String {
    val address = ip!!.address
    val builder = StringBuilder()
    for (field in address) { // TODO rewrite library in kotlin AND make to hex and from hex methods
    }
    return builder.toString()
  }

  companion object {
    fun of(address: InetAddress?, port: Int): Peer {
      return Peer(address, port)
    }

    fun of(socket: Socket): Peer {
      return Peer(socket.inetAddress, socket.port)
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