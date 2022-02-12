package com.tambapps.p2p.fandem.util;

import com.tambapps.p2p.speer.Peer;

/**
 * A listener of sharing events
 */
public interface TransferListener {

  void onConnected(Peer selfPeer, Peer remotePeer);

  void onTransferStarted(String fileName, long fileSize);

  void onProgressUpdate(int progress, long bytesProcessed, long totalBytes);

}