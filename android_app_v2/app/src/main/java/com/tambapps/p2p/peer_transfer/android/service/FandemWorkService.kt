package com.tambapps.p2p.peer_transfer.android.service

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.tambapps.p2p.fandem.model.FileData
import com.tambapps.p2p.peer_transfer.android.work.FandemWorker
import com.tambapps.p2p.peer_transfer.android.work.ReceiveFileWorker
import com.tambapps.p2p.peer_transfer.android.work.SendFileWorker
import com.tambapps.p2p.speer.Peer
import javax.inject.Inject

class FandemWorkService @Inject constructor(private val workManager: WorkManager,
                                            private val notificationManager: NotificationManager) {

  fun startSendFileWork(contentResolver: ContentResolver, peer: Peer, uris: List<Uri>) {
    // needs to be done BEFORE creating work
    createWorkNotificationChannelIfNeeded()

    val fileData = uris.map {
      contentResolver.query(it, null, null, null, null)!!.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        cursor.moveToFirst()
        FileData(cursor.getString(nameIndex), cursor.getLong(sizeIndex), "")
      }
    }

    val workRequest = OneTimeWorkRequestBuilder<SendFileWorker>()
      .setInputData(Data.Builder()
        .putString(FandemWorker.PEER_KEY, peer.toString())
        .putStringArray(SendFileWorker.FILE_NAMES_KEY, fileData.map { it.fileName }.toTypedArray())
        .putStringArray(SendFileWorker.FILE_URIS_KEY, uris.map { it.toString() }.toTypedArray())
        .putLongArray(SendFileWorker.FILE_SIZES_KEY, fileData.map { it.fileSize }.toLongArray())
        .build())
      .addTag(SendFileWorker::class.java.name)
      .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
      .build()
    workManager.enqueue(workRequest)
  }

  fun startReceiveFileWork(peer: Peer) {
    // need to be done BEFORE creating work
    createWorkNotificationChannelIfNeeded()

    val workRequest = OneTimeWorkRequestBuilder<ReceiveFileWorker>()
      .setInputData(Data.Builder()
        .putString(FandemWorker.PEER_KEY, peer.toString())
        .build())
      .addTag(SendFileWorker::class.java.name)
      .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
      .build()
    workManager.enqueue(workRequest)
  }

  private fun createWorkNotificationChannelIfNeeded() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || workNotificationChannelExists()) return
    val channel =
      NotificationChannel(FandemWorker.CHANNEL_ID, "Transfer notifications", NotificationManager.IMPORTANCE_DEFAULT)
    channel.description = "File sending notifications"
    channel.enableLights(false)
    notificationManager.createNotificationChannel(channel)
  }

  @TargetApi(Build.VERSION_CODES.O)
  private fun workNotificationChannelExists(): Boolean {
    val channel = notificationManager.getNotificationChannel(FandemWorker.CHANNEL_ID)
    return channel != null && channel.importance != NotificationManager.IMPORTANCE_NONE
  }
}