package com.tambapps.p2p.fandem.sniff

import com.tambapps.p2p.fandem.exception.SniffException
import java.io.IOException
import java.net.InetAddress
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.atomic.AtomicReference

class PeerSniffBlockingSupplier
@Throws(IOException::class) constructor(private val executor: ExecutorService,
                                        address: InetAddress) {
    private val peerSniffer: PeerSniffer
    private val peers: BlockingQueue<SniffPeer> = LinkedBlockingDeque<SniffPeer>()
    private var exception: SniffException? = null
    private val atomicFuture = AtomicReference<Future<*>>(null)

    init {
        peerSniffer = PeerSniffer(SniffListener(), address)
    }

    val started: Boolean get() = atomicFuture.get() != null

    fun start() {
        atomicFuture.set(executor.submit { peerSniffer.sniff(executor) })
    }

    @Throws(SniffException::class, InterruptedException::class)
    fun get(): SniffPeer {
        if (exception != null) {
            throw exception!!
        }
        val future = atomicFuture.get()
        if (future == null) {
            atomicFuture.set(executor.submit { peerSniffer.sniff(executor) })
        }
        return peers.take()
    }

    fun nbFound(): Int {
        return peers.size
    }

    fun stop() {
        atomicFuture.get()?.cancel(true)
    }

    private inner class SniffListener: PeerSniffer.SniffListener {

        override fun onPeerFound(peer: SniffPeer) {
            peers.add(peer)
        }

        override fun onError(e: SniffException) {
            exception = e
        }

        override fun onEnd() {
            start()
        }
    }
}