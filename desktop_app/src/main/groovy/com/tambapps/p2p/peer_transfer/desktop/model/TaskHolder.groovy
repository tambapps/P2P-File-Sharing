package com.tambapps.p2p.peer_transfer.desktop.model

import com.tambapps.p2p.file_sharing.FileSharer

abstract class TaskHolder<T extends FileSharer> {

    final T task
    String remotePeer
    String fileName
    long bytesTransferred
    long totalBytes
    int progress

    TaskHolder(T task) {
        this.task = task
    }

    TaskHolder(T task, File file) {
        this.task = task
        this.fileName = file.name
        totalBytes = file.length()
    }

    abstract boolean isSender()
}
