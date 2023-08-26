package com.tambapps.p2p.peer_transfer.android.work

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.tambapps.p2p.fandem.FileReceiver
import com.tambapps.p2p.fandem.exception.CorruptedFileException
import com.tambapps.p2p.fandem.util.FileUtils
import com.tambapps.p2p.fandem.util.OutputStreamProvider
import com.tambapps.p2p.peer_transfer.android.R
import com.tambapps.p2p.speer.Peer
import com.tambapps.p2p.speer.exception.HandshakeFailException
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.Exception
import java.net.SocketException
import java.net.SocketTimeoutException
import java.nio.channels.AsynchronousCloseException

@HiltWorker
class ReceiveFileWorker @AssistedInject constructor(@Assisted appContext: Context,
                                                    @Assisted params: WorkerParameters)
  : FandemWorker(appContext, params, R.drawable.download, R.drawable.download2), OutputStreamProvider {

  private data class ReceivedFile(val fileName: String, val uri: Uri)
  companion object {
    const val TAG = "ReceiveFileWorker"
  }

  private val receivedFiles = mutableListOf<ReceivedFile>()

  override suspend fun doTransfer(): Result {
    val peerString = inputData.getString(PEER_KEY) ?: return Result.failure()
    val peer = Peer.parse(peerString)
    Log.i(TAG, "Starting connection to $peer")
    notify(title = getString(R.string.connecting), text = getString(R.string.connecting_to, peer))
    try {
      val fileReceiver = FileReceiver(this)
      fileReceiver.receiveFrom(peer, this)
      notify(endNotif = true, title = getString(R.string.transfer_complete), bigText = getString(R.string.success_received,
        receivedFiles.joinToString(transform = { it.fileName}, separator = "\n- ", prefix = "- ")))

    } catch (e: HandshakeFailException) {
      notify(endNotif = true, title = getString(R.string.couldnt_start), text = e.message)
    } catch (e: CorruptedFileException) {
      notify(endNotif = true, title = getString(R.string.corrupted_file), text = e.message)
    } catch (e: SocketException) {
      notify(endNotif = true, title = getString(R.string.transfer_canceled), bigText = getString(R.string.incomplete_transfer))
    } catch (e: SocketTimeoutException) {
      notify(endNotif = true, title = getString(R.string.transfer_aborted), text = getString(R.string.connection_timeout))
    } catch (e: AsynchronousCloseException) {
      notify(endNotif = true, title = getString(R.string.transfer_canceled))
    } catch (e: Exception) {
      notify(endNotif = true, title = getString(R.string.transfer_aborted), bigText = e.message)
    }
    return Result.success()
  }

  override fun onConnected(selfPeer: Peer?, remotePeer: Peer?) {
    Log.i(TAG, "Connected to peer $remotePeer (using $selfPeer)")
  }

  override fun onTransferStarted(fileName: String, fileSize: Long) {
    Log.i(SendFileWorker.TAG, "Receiving $fileName (${FileUtils.toFileSize(fileSize)})")
    suspendNotify(title = getString(R.string.receiving_connected, fileName))
  }

  override fun newOutputStream(fileName: String): OutputStream {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      val values = ContentValues()
      values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
      // doesn't seem to be required values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
      values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
      val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)!!
      receivedFiles.add(ReceivedFile(fileName, uri))
      contentResolver.openOutputStream(uri)!!
    } else {
      val downloadDir =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
      val file = FileUtils.newAvailableFile(downloadDir, fileName)
      FileOutputStream(file)
    }
  }
}