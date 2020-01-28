package com.tambapps.p2p.fandem.listener

import com.tambapps.p2p.fandem.Peer

/**
 * A listener of sharing events
 */
interface TransferListener {

    fun onConnected(selfPeer: Peer, remotePeer: Peer, fileName: String, fileSize: Long)

    fun onProgressUpdate(progress: Int, bytesProcessed: Long, totalBytes: Long)

}