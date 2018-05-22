package com.tambapps.p2p.file_sharing;

public interface TransferListener {
    void onConnected(String remoteAddress, int remotePort,String fileName);
    void onProgressUpdate(int progress);
}
