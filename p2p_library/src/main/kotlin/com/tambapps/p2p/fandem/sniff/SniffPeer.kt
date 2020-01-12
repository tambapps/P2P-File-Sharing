package com.tambapps.p2p.fandem.sniff

import com.tambapps.p2p.fandem.Peer
import java.net.InetAddress

data class SniffPeer(val peer: Peer,
                     val deviceName: String,
                     val fileName: String) {

  fun getIp(): InetAddress {
    return peer.ip
  }

  fun getPort(): Int {
    return peer.port
  }

}