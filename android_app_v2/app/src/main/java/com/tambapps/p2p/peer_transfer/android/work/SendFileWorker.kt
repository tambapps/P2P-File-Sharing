package com.tambapps.p2p.peer_transfer.android.work

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.tambapps.p2p.fandem.Fandem
import com.tambapps.p2p.fandem.FileSender
import com.tambapps.p2p.fandem.SenderPeer
import com.tambapps.p2p.fandem.model.SendingFileData
import com.tambapps.p2p.fandem.util.FileUtils
import com.tambapps.p2p.peer_transfer.android.R
import com.tambapps.p2p.peer_transfer.android.util.NetworkUtils
import com.tambapps.p2p.speer.Peer
import com.tambapps.p2p.speer.datagram.MulticastDatagramPeer
import com.tambapps.p2p.speer.exception.HandshakeFailException
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.FileNotFoundException
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.concurrent.Executors

@HiltWorker
class SendFileWorker @AssistedInject constructor(@Assisted appContext: Context,
                                                 @Assisted workerParams: WorkerParameters
): FandemWorker(appContext, workerParams, R.drawable.upload_little, R.drawable.upload2) {

  companion object {
    const val TAG = "SendFileWorker"
    const val FILE_NAMES_KEY  = "fn"
    const val FILE_URIS_KEY  = "fu"
    const val FILE_SIZES_KEY  = "fs"
  }

  private val scheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
  private val senderPeersMulticastService = Fandem.multicastService(scheduledExecutorService)
  private lateinit var fileNames: Array<String>

  override suspend fun doTransfer(): Result {
    try {
      val peerString = inputData.getString(PEER_KEY) ?: return Result.failure()
      val peer = Peer.parse(peerString)

      val files = parseFileData() ?: return Result.failure()
      val deviceName = Build.MANUFACTURER + " " + Build.MODEL
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
        Log.e(TAG, "Error while starting multicasting")
      }
      notify(title = getString(R.string.waiting_connection), text = getString(R.string.waiting_connection_message, Fandem.toHexString(peer)))
      val fileSender = FileSender(peer,  this, SOCKET_TIMEOUT)
      try {
        fileSender.send(files)
        if (files.size > 1) {
          notify(endNotif = true, title = getString(R.string.transfer_complete), bigText = getString(R.string.success_send_many, fileNames.joinToString(separator = "\n- ", prefix = "- ")))
        } else {
          notify(endNotif = true, title = getString(R.string.transfer_complete), text = getString(R.string.success_send, fileNames.first()))
        }
      } catch (e: HandshakeFailException) {
        notify(endNotif = true, title = getString(R.string.couldnt_start), text = e.message)
      } catch (e: SocketException) {
        // probably a cancel
        notify(endNotif = true, title = getString(R.string.transfer_canceled))
      } catch (e: SocketTimeoutException) {
        notify(endNotif = true, title = getString(R.string.transfer_canceled), text = getString(R.string.connection_timeout))
      } catch (e: FileNotFoundException) {
        notify(endNotif = true, title = getString(R.string.transfer_aborted), text = getString(R.string.couldnt_find_file))
      } catch (e: Exception) {
        Log.e(TAG, "An unexpected error occurred while sending file", e)
        notify(endNotif = true, title = getString(R.string.transfer_aborted), bigText = getString(R.string.error_occured, e.message ?: "<no message>"))
      }
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

  override fun onTransferStarted(fileName: String, fileSize: Long) {
    Log.i(TAG, "Sending $fileName (${FileUtils.toFileSize(fileSize)})")
    notify(title = getString(R.string.sending_file, fileName))
  }

  override fun onConnected(selfPeer: Peer?, remotePeer: Peer?) {
    Log.i(TAG, "Connected to peer $remotePeer (using $selfPeer)")
    notify(bigText = getString(
      R.string.sending_connected,
      fileNames.joinToString(separator = "\n- ", prefix = "- ")
    ))
  }

  private fun parseFileData(): List<SendingFileData>? {
    fileNames = inputData.getStringArray(FILE_NAMES_KEY) ?: return null
    val fileUris = inputData.getStringArray(FILE_URIS_KEY) ?: return null
    val fileSizes = inputData.getLongArray(FILE_SIZES_KEY) ?: return null

    if (fileNames.size != fileUris.size || fileUris.size != fileSizes.size) return null
    val fileCount = fileNames.size
    if (fileCount == 0) return null

    val files = mutableListOf<SendingFileData>()
    Log.i(TAG, "Computing checksums")
    for (i in 0 until fileCount) {
      val fileName = fileNames[i]
      val fileUri = Uri.parse(fileUris[i])
      val fileSize = fileSizes[i]
      val checksum = FileUtils.computeChecksum(contentResolver.openInputStream(fileUri))
      files.add(SendingFileData(fileName, fileSize, checksum) {
        contentResolver.openInputStream(fileUri)
      })
    }
    return files
  }
}