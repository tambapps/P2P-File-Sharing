package com.tambapps.p2p.peer_transfer.desktop.model

class ReceiveData extends ShareData {
    final String[] ipFields = ["", "", "", ""]
    Integer port
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
        //we don't clear folder
        for (int i = 0; i < ipFields.length; i++) {
            ipFields[i] = ""
        }
    }
}
