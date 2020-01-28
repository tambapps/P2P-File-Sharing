package com.tambapps.p2p.fandem.listener

import java.io.File

interface ReceivingListener : TransferListener {
    fun onEnd(file: File)
}
