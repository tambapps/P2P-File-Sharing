package com.tambapps.p2p.file_sharing;

public interface TransferListener {

    void onConnected(Peer selfPeer, Peer remotePeer, String fileName, long fileSize);

    void onProgressUpdate(int progress, long byteProcessed, long totalBytes);

}
