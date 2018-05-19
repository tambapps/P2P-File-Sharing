package tambapps.com.a2sfile_sharing.service;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import tambapps.com.a2sfile_sharing.DialogActivity;
import tambapps.com.a2sfile_sharing.R;

/**
 * Created by fonkoua on 25/03/18.
 */

abstract class FileForegroundService extends IntentService {

    private int notifId;
    private NotificationCompat.Builder notifBuilder;
    private NotificationManager notificationManager;

    public FileForegroundService(String name) {
        super(name);
    }

    abstract void runService(Intent intent);
    abstract int largeIcon();
    abstract int smallIcon();

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        notificationManager  = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notifId = intent.getIntExtra("id", 0);
        startForeground(notifId, buildNotification());
        super.onStart(intent, startId);

    }

    @Override
    protected final void onHandleIntent(@Nullable Intent intent) {

        runService(intent);
    }

    PendingIntent createPI(Intent intent) {
        return PendingIntent.getActivity(this, notifId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    PendingIntent dialogActivityIntent(String title, String message) {
        Intent intent = new Intent(this, DialogActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("message", message);
        return createPI(intent);
    }

    private Notification buildNotification() {
        if (Build.VERSION.SDK_INT >= 26) {
            createChannel();
        }
        notifBuilder = new NotificationCompat.Builder(this, getClass().getName());
        notifBuilder//.setContentTitle("Downloading")
                .setAutoCancel(false)
                .setOngoing(true)
                .setSmallIcon(smallIcon())
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), largeIcon()))
                .setColor(getResources().getColor(R.color.colorSmallIcon))
                .setProgress(100, 0, false);
        Notification notification = notifBuilder.build();
        notificationManager.notify(notifId, notification);
        return notification;
    }

    @TargetApi(26)
    private void createChannel() {
        if (channelExists())
            return;
        NotificationChannel channel = new NotificationChannel(getClass().getName(), getClass().getName(), NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("notifications for " + getClass().getName());
        channel.enableLights(false);
        notificationManager.createNotificationChannel(channel);
    }

    @TargetApi(26)
    private boolean channelExists() {
        NotificationChannel channel = notificationManager.getNotificationChannel(getClass().getName());
        return channel != null && channel.getImportance() != NotificationManager.IMPORTANCE_NONE;
    }

    void updateProgress(int progress) {
        notifBuilder.setProgress(100, progress, false);
        notificationManager.notify(notifId, notifBuilder.build());
    }

    NotificationCompat.Builder getNotifBuilder() {
        return notifBuilder;
    }

    void updateNotification() {
        notificationManager.notify(notifId, notifBuilder.build());
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }
}
