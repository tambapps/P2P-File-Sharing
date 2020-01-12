package com.tambapps.p2p.fandem.sniff

import com.tambapps.p2p.fandem.Peer
import com.tambapps.p2p.fandem.util.IPUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.lang.RuntimeException
import java.net.InetAddress
import java.util.concurrent.ExecutorCompletionService
import java.util.concurrent.Executors

class SniffTest: PeerSniffer.SniffListener {

  private val sniffedAddress = IPUtils.ipAddress.address
  private val snifferAddress = snifferAddress()
  private val sniffedPeer = Peer.of(InetAddress.getByAddress(sniffedAddress), Peer.DEFAULT_PORT)
  private val deviceName = "Desktop"
  private val filename = "file.file"
  private val peers: MutableList<SniffPeer> = arrayListOf()
  private val completionService = ExecutorCompletionService<Boolean>(Executors.newFixedThreadPool(2))

  private fun snifferAddress(): ByteArray {
    val address = sniffedAddress.copyOf()
    address[3]--
    return address
  }

  @Test
  fun testSniff() {
    val peerSniffer = PeerSniffer(this, InetAddress.getByAddress(snifferAddress))
    val sniffHandler = PeerSniffHandler(sniffedPeer, deviceName, filename)

    val future = completionService.submit {
      sniffHandler.handleSniff()
      true
    }

    Thread.sleep(1000)
    completionService.submit {
      peerSniffer.sniff()
      true
    }
    assertTrue(completionService.take().get())

    assertEquals(1, peers.size)
    assertEquals(deviceName, peers[0].deviceName)
    assertEquals(Peer.DEFAULT_PORT, peers[0].getPort())
    assertEquals(filename, peers[0].fileName)
  }

  override fun onPeerFound(peer: SniffPeer) {
    peers.add(peer)
  }

  override fun onError(e: IOException) {
    throw RuntimeException(e)
  }

  override fun onEnd() {

  }
}