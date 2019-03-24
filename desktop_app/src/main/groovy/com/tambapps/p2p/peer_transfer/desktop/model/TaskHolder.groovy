package com.tambapps.p2p.peer_transfer.desktop.model

import com.tambapps.p2p.fandem.Peer

import java.util.concurrent.Future

class TaskHolder {

    Peer peer //self peer for sender, remotePeer for receiver
    Peer remotePeer //for sender only
    String fileName
    File directory //for receiver only
    File file // for receiver only
    long bytesTransferred
    long totalBytes
    int progress
    boolean sender
    boolean connected
    IOException error
    Future future

    boolean isFinished() {
        return totalBytes == bytesTransferred
    }

    String getTaskName() {
        return (sender ? 'Sending' : 'Receiving') + ' task'
    }

    String getHeader() {
        return sender ? "Sending $fileName:" : "Receiving in $directory.path:"
    }
    void cancel() {
        future.cancel(true)
    }

    boolean isCanceled() {
        return future.isCancelled()
    }
}
