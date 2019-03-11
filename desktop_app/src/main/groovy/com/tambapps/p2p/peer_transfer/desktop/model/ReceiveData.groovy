package com.tambapps.p2p.peer_transfer.desktop.model

class ReceiveData extends ShareData {
    final String[] ipFields = ["", "", "", ""]
    int port
    File folder

    String getIp() {
        return String.join(".", ipFields)
    }

    @Override
    String checkErrors() {
        if (!ipFields.every {it != null && !it.empty }) {
            return "ip is not valid"
        }
        if (port == 0) {
            return "port is missing"
        }
        if (folder == null) {
            return "folder is missing"
        }
        return null
    }

    @Override
    void clear() {
        port = null
        folder = null
        clearIp()
    }
}
