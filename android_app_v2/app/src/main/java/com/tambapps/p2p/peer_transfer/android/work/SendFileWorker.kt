package com.tambapps.p2p.peer_transfer.android.work

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.hilt.work.HiltWorker
import androidx.work.ForegroundInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.tambapps.p2p.fandem.Fandem
import com.tambapps.p2p.fandem.FileSender
import com.tambapps.p2p.fandem.SenderPeer
import com.tambapps.p2p.fandem.model.SendingFileData
import com.tambapps.p2p.fandem.util.FileUtils
import com.tambapps.p2p.peer_transfer.android.util.NetworkUtils
import com.tambapps.p2p.speer.Peer
import com.tambapps.p2p.speer.datagram.MulticastDatagramPeer
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.IOException
import java.util.concurrent.Executors

@HiltWorker
class SendFileWorker @AssistedInject constructor(@Assisted appContext: Context,
                                                 @Assisted workerParams: WorkerParameters
): Worker(appContext, workerParams) {

  companion object {
    const val PEER_KEY = "pk"
    const val TAG = "SendFileWorker"
    const val FILE_NAMES_KEY  = "fn"
    const val FILE_URIS_KEY  = "fu"
    const val FILE_SIZES_KEY  = "fs"
  }

  private val scheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
  private val senderPeersMulticastService = Fandem.multicastService(scheduledExecutorService)

  override fun doWork(): Result {
    try {
      val peerString = inputData.getString(PEER_KEY) ?: return Result.failure()
      val peer = Peer.parse(peerString)
      val deviceName = Build.MANUFACTURER + " " + Build.MODEL

      val fileNames = inputData.getStringArray(FILE_NAMES_KEY) ?: return Result.failure()
      val fileUris = inputData.getStringArray(FILE_URIS_KEY) ?: return Result.failure()
      val fileSizes = inputData.getLongArray(FILE_SIZES_KEY) ?: return Result.failure()

      if (fileNames.size != fileUris.size || fileUris.size != fileSizes.size) return Result.failure()
      val fileCount = fileNames.size
      if (fileCount == 0) return Result.failure()

      val files = mutableListOf<SendingFileData>()
      Log.i(TAG, "Computing checksums")
      for (i in 0 until fileCount) {
        val fileName = fileNames[i]
        val fileUri = Uri.parse(fileUris[i])
        val fileSize = fileSizes[i]
        val checksum = FileUtils.computeChecksum(applicationContext.contentResolver.openInputStream(fileUri))
        files.add(SendingFileData(fileName, fileSize, checksum) {
          applicationContext.contentResolver.openInputStream(fileUri)
        })
      }
      senderPeersMulticastService.data = listOf(
        SenderPeer(
          peer.address,
          peer.port,
          deviceName,
          files
        ))
      Log.i(TAG, "Starting multicasting at $peer")
      try {
        // the senderPeersMulticastService will close this peer when stopping
        val datagramPeer = MulticastDatagramPeer(senderPeersMulticastService.port)
        val wifiNetworkInterface = NetworkUtils.findWifiNetworkInterface()
        if (wifiNetworkInterface != null) {
          // this is needed for multicast to work on hotspot (AKA mobile data sharing)
          datagramPeer.socket.networkInterface = wifiNetworkInterface
        }
        senderPeersMulticastService.start(datagramPeer, 1000L)
      } catch (e: IOException) {
        Toast.makeText(
          applicationContext,
          "Couldn't communicate sender key. The receiver will have to enter it manually",
          Toast.LENGTH_LONG
        ).show()
      }

      val fileSender = FileSender(peer) // TODO listener arg
      fileSender.send(files)
      return Result.success()
    } finally {
      try {
        senderPeersMulticastService.stop()
        scheduledExecutorService.shutdown()
      } catch (e: Exception) {
        Log.e(TAG, "Error while closing worker resources", e)
      }
    }
  }

  override fun getForegroundInfo(): ForegroundInfo {
    return super.getForegroundInfo()
  }

}