package com.tambapps.p2p.peer_transfer.desktop.model

import com.tambapps.p2p.file_sharing.FileReceiver
import com.tambapps.p2p.file_sharing.FileSender

class ReceiveHolder extends TaskHolder<FileReceiver> {

    ReceiveHolder(FileReceiver task) {
        super(task)
    }

    @Override
    boolean isSender() {
        return false
    }
}
