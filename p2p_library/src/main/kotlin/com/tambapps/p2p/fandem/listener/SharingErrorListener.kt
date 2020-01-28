package com.tambapps.p2p.fandem.listener

import java.io.IOException

interface SharingErrorListener {
    fun onError(e: IOException)
}