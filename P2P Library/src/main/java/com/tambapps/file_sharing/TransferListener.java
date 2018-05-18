package com.tambapps.file_sharing;

public interface TransferListener {
    void onConnected(String remoteAddress, int remotePort,String fileName);
    void onProgressUpdate(int progress);
}
