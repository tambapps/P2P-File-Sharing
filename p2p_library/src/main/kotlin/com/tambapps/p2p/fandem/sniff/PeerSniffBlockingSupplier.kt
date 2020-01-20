package com.tambapps.p2p.fandem.sniff

import com.tambapps.p2p.fandem.exception.SniffException
import java.io.IOException
import java.net.InetAddress
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.LinkedBlockingDeque

class PeerSniffBlockingSupplier
@Throws(IOException::class) constructor(private val executor: ExecutorService,
                                        address: InetAddress) {
    private val peerSniffer: PeerSniffer
    private val peers: BlockingQueue<SniffPeer> = LinkedBlockingDeque()
    private var exception: SniffException? = null
    @Volatile
    private var future: Future<*>? = null

    init {
        peerSniffer = PeerSniffer(SniffListener(), address)
    }

    @Throws(SniffException::class, InterruptedException::class)
    fun get(): SniffPeer {
        if (exception != null) {
            throw exception!!
        }
        if (future == null) {
            future = executor.submit { peerSniffer.sniff() }
        }
        return peers.take()
    }

    fun nbFound(): Int {
        return peers.size
    }

    fun stop() {
        future?.cancel(true)
    }

    private inner class SniffListener: PeerSniffer.SniffListener {

        override fun onPeerFound(peer: SniffPeer) {
            peers.add(peer)
        }

        override fun onError(e: SniffException) {
            exception = e
        }

        override fun onEnd() {
            future = null
        }
    }
}