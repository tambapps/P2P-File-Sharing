package com.tambapps.p2p.fandem

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.net.InetAddress

class PeerTest {

  companion object {
    private val LOCALHOST = InetAddress.getLocalHost()
  }

  @Test
  fun testEquals() {
    val peer1 = Peer.of(LOCALHOST, Peer.DEFAULT_PORT)
    val peer2 = Peer.of(LOCALHOST, Peer.DEFAULT_PORT)
    assertEquals(peer1, peer2)
  }

  @Test
  fun testNotEquals() {
    val peer1 = Peer.of(LOCALHOST, Peer.DEFAULT_PORT)
    val peer2 = Peer.of(LOCALHOST, Peer.DEFAULT_PORT + 1)
    assertNotEquals(peer1, peer2)
  }

  @Test
  fun testHex1() {
    val peer1 = Peer.of(LOCALHOST, Peer.DEFAULT_PORT)
    println(peer1.toHexString())
    val peer2 = Peer.fromHexString(peer1.toHexString())
    assertEquals(peer1, peer2)
  }

  @Test
  fun testHex2() {
    val peer1 = Peer.of(LOCALHOST, Peer.DEFAULT_PORT + 2)
    println(peer1.toHexString())
    val peer2 = Peer.fromHexString(peer1.toHexString())
    assertEquals(peer1, peer2)
  }
}