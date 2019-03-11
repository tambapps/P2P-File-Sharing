package com.tambapps.p2p.peer_transfer.desktop.model

import com.tambapps.p2p.file_sharing.FileSender

class SendHolder extends TaskHolder<FileSender> {

    SendHolder(FileSender task, File file) {
        super(task, file)
    }

    @Override
    boolean isSender() {
        return true
    }
}
