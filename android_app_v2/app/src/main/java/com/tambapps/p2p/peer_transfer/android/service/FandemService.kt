package com.tambapps.p2p.peer_transfer.android.service

import android.net.Uri
import androidx.work.WorkManager
import com.tambapps.p2p.speer.Peer
import javax.inject.Inject

class FandemService @Inject constructor(private val workManager: WorkManager) {

  fun sendFiles(peer: Peer, uris: List<Uri>) {
    TODO("Send workRequest")
  }
}