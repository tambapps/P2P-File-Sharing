package com.tambapps.file_sharing;

public interface TransferListener {
    void onConnected(String remoteAddress, int remotePort);
    void onProgressUpdate(int progress);

}
