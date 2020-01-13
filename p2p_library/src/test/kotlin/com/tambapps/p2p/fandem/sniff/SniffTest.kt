package com.tambapps.p2p.fandem.sniff

import com.tambapps.p2p.fandem.Peer
import com.tambapps.p2p.fandem.util.IPUtils
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.lang.Exception
import java.lang.RuntimeException
import java.net.InetAddress
import java.net.ServerSocket
import java.util.concurrent.ExecutorCompletionService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class SniffTest: PeerSniffer.SniffListener {

  private val sniffedAddress = IPUtils.ipAddress.address
  private val snifferAddress = snifferAddress()
  private val sniffedPeer = Peer.of(InetAddress.getByAddress(sniffedAddress), Peer.DEFAULT_PORT)
  private val deviceName = "Desktop"
  private val filename = "file.file"
  private val peers: MutableList<SniffPeer> = arrayListOf()
  private val finished = AtomicBoolean(false)
  private lateinit var sniffHandler: PeerSniffHandler
  private lateinit var peerSniffer: PeerSniffer
  private var executor = Executors.newFixedThreadPool(4)
  private val completionService = ExecutorCompletionService<Boolean>(executor)
  private lateinit var serverSocket: ServerSocket

  private fun snifferAddress(): ByteArray {
    val address = sniffedAddress.copyOf()
    address[3]--
    return address
  }

  @Before
  fun init() {
    sniffHandler = PeerSniffHandler(sniffedPeer, deviceName, filename)
    peerSniffer = PeerSniffer(this, InetAddress.getByAddress(snifferAddress))
    finished.set(false)
    serverSocket = sniffHandler.newServerSocket()
  }

  @After
  fun clean() {
    serverSocket.close()
  }

  @Test
  fun testSniff() {
    test {
      peerSniffer.sniff()
      true
    }
  }

  @Test
  fun testSniffMultithreaded() {
    test {
      peerSniffer.sniff(executor)
      true
    }
  }

  @Test
  fun testSniffWhileNoneFound() {
    test {
      peerSniffer.sniffWhileNoneFound()
      true
    }
  }

  @Test
  fun testSniffWhileNoneFoundMultithreaded() {
    test {
      peerSniffer.sniffWhileNoneFound(executor)
      true
    }
  }

  fun test(sniffCallable: () -> Boolean) {
    completionService.submit {
      sniffHandler.handleSniff(serverSocket)
      true
    }
    completionService.submit(sniffCallable)
    assertTrue(completionService.take().get())

    assertEquals(1, peers.size)
    assertEquals(deviceName, peers[0].deviceName)
    assertEquals(Peer.DEFAULT_PORT, peers[0].getPort())
    assertEquals(filename, peers[0].fileName)
    assertTrue(finished.get())
  }

  override fun onPeerFound(peer: SniffPeer) {
    peers.add(peer)
  }

  override fun onError(e: Exception) {
    throw RuntimeException(e)
  }

  override fun onEnd() {
    finished.set(true)
  }
}