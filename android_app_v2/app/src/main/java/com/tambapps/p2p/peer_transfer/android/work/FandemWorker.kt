package com.tambapps.p2p.peer_transfer.android.work

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tambapps.p2p.fandem.util.FileUtils
import com.tambapps.p2p.fandem.util.TransferListener
import com.tambapps.p2p.peer_transfer.android.ui.theme.NotifSmallIconColor

abstract class FandemWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params),
  TransferListener {

  companion object {
    protected const val SOCKET_TIMEOUT = 1000 * 60 * 2 //in ms
  }

  protected abstract val smallIcon: Int
  protected abstract val largeIcon: Int
  private val notificationId = id.hashCode()

  private val notificationManager = appContext.getSystemService(NotificationManager::class.java)
  private lateinit var notifBuilder: NotificationCompat.Builder

  final override suspend fun doWork(): Result {
    val notificationManager = applicationContext.getSystemService(NotificationManager::class.java)
    createNotificationChannelIfNeeded(notificationManager)
    notifBuilder = createNotificationBuilder()
    return doTransfer()
  }

  abstract fun doTransfer(): Result

  override fun onTransferStarted(fileName: String?, fileSize: Long) {

  }
  override fun onProgressUpdate(
    fileName: String,
    progress: Int,
    bytesProcessed: Long,
    totalBytes: Long
  ) {
    if (progress < 100) {
      notify(progress= progress, bigText = FileUtils.toFileSize(bytesProcessed) + "/ " + FileUtils.toFileSize(
        totalBytes
      ))
    }
  }

  private fun createNotificationChannelIfNeeded(notificationManager: NotificationManager) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || channelExists(notificationManager)) return
    val channel =
      NotificationChannel(javaClass.name, javaClass.name, NotificationManager.IMPORTANCE_DEFAULT)
    channel.description = "notifications for " + javaClass.name
    channel.enableLights(false)
    notificationManager.createNotificationChannel(channel)
  }

  @TargetApi(Build.VERSION_CODES.O)
  private fun channelExists(notificationManager: NotificationManager): Boolean {
    val channel = notificationManager.getNotificationChannel(javaClass.name)
    return channel != null && channel.importance != NotificationManager.IMPORTANCE_NONE
  }

  private fun createNotificationBuilder(): NotificationCompat.Builder {
    val notifBuilder: NotificationCompat.Builder = NotificationCompat.Builder(applicationContext, javaClass.name)
    notifBuilder
      .setSmallIcon(smallIcon)
      .setLargeIcon(BitmapFactory.decodeResource(applicationContext.resources, largeIcon))
      .setColor(NotifSmallIconColor.toArgb())
    return notifBuilder
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
    }
    if (title != null) notifBuilder.setContentTitle(title)
    if (text != null) notifBuilder.setContentText(text)
    if (bigText != null) notifBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
    if (progress != null) notifBuilder.setProgress(100, progress, false).setOngoing(true)
    else notifBuilder.setProgress(0, 0, false).setOngoing(false)
    notificationManager.notify(notificationId, notifBuilder.build())
  }

  protected fun getString(resId: Int) = applicationContext.getString(resId)
  protected fun getString(resId: Int, vararg objects: Any) = applicationContext.getString(resId, *objects)
}