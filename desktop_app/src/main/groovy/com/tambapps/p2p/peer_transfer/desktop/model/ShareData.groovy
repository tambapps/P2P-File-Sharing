package com.tambapps.p2p.peer_transfer.desktop.model

abstract class ShareData {

    abstract String checkErrors()

    abstract void clear()

    protected void clearIp() {
        for (int i = 0; i < ipFields.length; i++) {
            ipFields[i] = ""
        }
    }
}
