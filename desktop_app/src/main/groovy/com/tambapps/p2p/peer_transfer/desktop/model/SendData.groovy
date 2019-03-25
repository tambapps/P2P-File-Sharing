package com.tambapps.p2p.peer_transfer.desktop.model

class SendData extends ShareData {

    File file

    @Override
    String checkErrors() {
        if (file == null) {
            return "File is missing"
        }
        return null
    }

    @Override
    void clear() {
        file = null
    }
}
