package com.tambapps.p2p.file_sharing;

/**
 * A listener of sharing events
 */
public interface TransferListener {

    void onConnected(Peer selfPeer, Peer remotePeer, String fileName, long fileSize);

    void onProgressUpdate(int progress, long byteProcessed, long totalBytes);

}
