package com.tambapps.p2p.peer_transfer.android.work

import android.app.Notification
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.tambapps.p2p.fandem.util.FileUtils
import com.tambapps.p2p.fandem.util.TransferListener
import com.tambapps.p2p.peer_transfer.android.ui.theme.NotifSmallIconColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicReference

abstract class FandemWorker(appContext: Context, params: WorkerParameters, smallIcon: Int, largeIcon: Int) : CoroutineWorker(appContext, params),
  TransferListener {

  companion object {
    const val PEER_KEY = "pk"
    const val SOCKET_TIMEOUT = 1000 * 60 * 2 //in ms
    private const val NOTIFICATION_PROGRESS_UPDATE_INTERVAL = 1_000L
    val CHANNEL_ID: String = FandemWorker::class.java.name
  }

  private val notificationId = id.hashCode()
  val contentResolver: ContentResolver = appContext.contentResolver

  private val notificationManager = appContext.getSystemService(NotificationManager::class.java)
  private val notifBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
    .setSmallIcon(smallIcon)
    .setLargeIcon(BitmapFactory.decodeResource(applicationContext.resources, largeIcon))
    .setColor(NotifSmallIconColor.toArgb())

  private val lastNotificationReference = AtomicReference<Notification>(null)

  private var notificationLastUpdated = System.currentTimeMillis()

  final override suspend fun doWork(): Result {
    return doTransfer()
  }

  abstract suspend fun doTransfer(): Result

  override fun onProgressUpdate(
    fileName: String,
    progress: Int,
    bytesProcessed: Long,
    totalBytes: Long
  ) {
    val now = System.currentTimeMillis()
    if (progress < 100 && now - notificationLastUpdated >= NOTIFICATION_PROGRESS_UPDATE_INTERVAL) {
      // notifying every 0.5s because if it happened more frequently some notifs could be ignored
      notificationLastUpdated = now
      notify(progress = progress,
        bigText = FileUtils.toFileSize(bytesProcessed) + "/ " + FileUtils.toFileSize(totalBytes))

    }
  }

  fun notify(title: String? = null, text: String? = null, bigText: String? = null, progress: Int? = null, endNotif: Boolean = false) {
    if (endNotif) {
      notifBuilder.clearActions()
      notifBuilder.setStyle(null)
        .setAutoCancel(true)
        .setOngoing(false)
        .setContentText("")
        .setContentTitle("")
        .setProgress(0, 0, false)
    } else notifBuilder.setOngoing(true)
    if (text != null && bigText == null) notifBuilder.setStyle(null)
    if (bigText != null && text == null) notifBuilder.setContentText(null)
    if (title != null) notifBuilder.setContentTitle(title)
    if (text != null) notifBuilder.setContentText(text)
    if (bigText != null) notifBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
    if (progress != null) notifBuilder.setProgress(100, progress, false)
    else notifBuilder.setProgress(0, 0, false).setOngoing(false)

    val notification = notifBuilder.build()
    lastNotificationReference.set(notification)
    if (endNotif) {
      // NEEDED FOR OLD DEVICES THAT USE FOREGROUND NOTIFICATION
      notificationManager.cancel(notificationId)
      notificationManager.notify(notificationId + 1, notification)
    } else {
      notificationManager.notify(notificationId, notification)
    }
  }

  protected fun getString(resId: Int) = applicationContext.getString(resId)
  protected fun getString(resId: Int, vararg objects: Any) = applicationContext.getString(resId, *objects)

  override suspend fun getForegroundInfo(): ForegroundInfo {
    return ForegroundInfo(notificationId, lastNotificationReference.get() ?: notifBuilder.build())
  }

}