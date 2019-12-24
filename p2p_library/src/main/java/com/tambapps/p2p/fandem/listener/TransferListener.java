package com.tambapps.p2p.fandem.listener;

import com.tambapps.p2p.fandem.Peer;

/**
 * A listener of sharing events
 */
public interface TransferListener {

  void onConnected(Peer selfPeer, Peer remotePeer, String fileName, long fileSize);

  void onProgressUpdate(int progress, long bytesProcessed, long totalBytes);

}