package com.tambapps.p2p.peer_transfer.android.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.PersistableBundle;
import android.support.v4.app.NotificationCompat;

import com.crashlytics.android.Crashlytics;
import com.tambapps.p2p.peer_transfer.android.R;
import com.tambapps.p2p.file_sharing.TransferListener;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by fonkoua on 13/05/18.
 */

public abstract class FileJobService extends JobService {

    private final static ExecutorService executor = Executors.newFixedThreadPool(2);
    final static int SOCKET_TIMEOUT = 1000 * 90; //in ms

    private NotificationBroadcastReceiver notificationBroadcastReceiver;
    private String ACTION_CANCEL;
    private FileTask fileTask;

    @Override
    public void onCreate() {
        super.onCreate();
        ACTION_CANCEL = getClass().getName() + ".cancel";
        notificationBroadcastReceiver = new NotificationBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        intentFilter.addAction(ACTION_CANCEL);
        registerReceiver(notificationBroadcastReceiver, intentFilter);
    }

    @Override
    public boolean onStartJob(final JobParameters params) {
        PersistableBundle bundle = params.getExtras();
        NotificationManager notificationManager  = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        int notifId = bundle.getInt("id");
        Runnable endRunnable = new Runnable() {
            @Override
            public void run() {
                unregisterReceiver(notificationBroadcastReceiver);
                jobFinished(params, false);
            }
        };

        PendingIntent cancelIntent = PendingIntent.getBroadcast(this, 0,
                new Intent(ACTION_CANCEL), 0);

        fileTask = startTask(buildNotification(notificationManager, notifId),
                notificationManager, notifId, bundle, endRunnable, cancelIntent);
        return true;
    }

    @TargetApi(26)
    private void createChannel(NotificationManager notificationManager) {
        if (channelExists(notificationManager))
            return;
        NotificationChannel channel = new NotificationChannel(getClass().getName(), getClass().getName(), NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("notifications for " + getClass().getName());
        channel.enableLights(false);
        notificationManager.createNotificationChannel(channel);
    }

    @TargetApi(26)
    private boolean channelExists(NotificationManager notificationManager) {
        NotificationChannel channel = notificationManager.getNotificationChannel(getClass().getName());
        return channel != null && channel.getImportance() != NotificationManager.IMPORTANCE_NONE;
    }

    private NotificationCompat.Builder buildNotification(NotificationManager notificationManager, int notifId) {
        if (Build.VERSION.SDK_INT >= 26) {
            createChannel(notificationManager);
        }
        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(this, getClass().getName());
        notifBuilder
                .setOngoing(true)
                .setSmallIcon(smallIcon())
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), largeIcon()))
                .setColor(getResources().getColor(R.color.colorSmallIcon));
        Notification notification = notifBuilder.build();
        notificationManager.notify(notifId, notification);
        return notifBuilder;
    }

    abstract FileTask startTask(NotificationCompat.Builder notifBuilder,
                                NotificationManager notificationManager,
                                int notifId,
                                PersistableBundle bundle,
                                Runnable endRunnable,
                                PendingIntent cancelIntent);
    abstract int smallIcon();
    abstract int largeIcon();

    @Override
    public boolean onStopJob(JobParameters params) {
        if (fileTask != null) {
            fileTask.cancel();

        }
        return false;
    }

    static abstract class FileTask implements TransferListener {
        private NotificationCompat.Builder notifBuilder;
        NotificationCompat.BigTextStyle notifStyle;
        private NotificationManager notificationManager;
        private final int notifId;
        private Runnable endRunnable;
        private String remotePeer;

        FileTask(NotificationCompat.Builder notifBuilder,
                 NotificationManager notificationManager,
                 int notifId, Runnable endRunnable,
                 PendingIntent cancelIntent) {
            this.notifBuilder = notifBuilder;
            this.notificationManager = notificationManager;
            this.notifId = notifId;
            this.endRunnable = endRunnable;
            notifStyle = new NotificationCompat.BigTextStyle();

            notifBuilder.addAction(android.R.drawable.ic_delete, "cancel", cancelIntent);
        }

        @Override
        public void onProgressUpdate(int progress, long bytesProcessed, long totalBytes) {
            if (progress < 100) {
                notifBuilder.setProgress(100, progress, false);
                notifStyle.bigText(FileJobService.bytesToString(bytesProcessed) + "/ " + FileJobService.bytesToString(totalBytes));
            }
            updateNotification();
        }

        FileTask execute(final String... params) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    FileTask.this.run(params);
                    try {
                        Thread.sleep(500); //wait to ensure that the notification is well updated
                    } catch (InterruptedException e) {
                        Crashlytics.logException(e);
                    }
                    updateNotification();
                    endRunnable.run();
                    dispose();
                }
            });
            return this;
        }

        @Override
        public final void onConnected(String remoteAddress, int port, String fileName, long fileSize) {
            remotePeer = remoteAddress + ":" + port;
            getNotifBuilder()
                    .setProgress(100, 0, false)
                    .setContentText("")
                    .setContentTitle(onConnected(remotePeer, fileName))
                    .setStyle(notifStyle.bigText(""));
            updateNotification();
        }

        abstract void run(String... params);
        abstract void cancel();
        abstract String onConnected(String remotePeer, String fileName); //return the title of the notification

        NotificationCompat.Builder getNotifBuilder() {
            return notifBuilder;
        }

        void updateNotification() {
            notificationManager.notify(notifId, notifBuilder.build());
        }

        NotificationCompat.Builder finishNotification() {
            notifBuilder.mActions.clear();
            return notifBuilder.setStyle(null)
                    .setAutoCancel(true)
                    .setOngoing(false)
                    .setContentText("")
                    .setContentTitle("")
                    .setProgress(0, 0, false);
        }

        void dispose() {
            notifBuilder = null;
            notifStyle = null;
            notificationManager = null;
            endRunnable = null;
        }
    }

    public class NotificationBroadcastReceiver extends BroadcastReceiver {

        @Override
        public final void onReceive(Context context, Intent intent) {
            if (ACTION_CANCEL.equals(intent.getAction()) && fileTask != null) {
                fileTask.cancel();
            }
        }
    }

    static String bytesToString(long bytes) {
        String units = "kMG";
        long denominator = 1;
        int i = 0;

        while (bytes / (denominator * 1024) > 0 && i < units.length()) {
            denominator *= 1024;
            i++;
        }
        return String.format(Locale.US, "%.1f %sB", ((float)bytes)/((float)denominator),
                i == 0 ? "" : units.charAt(i - 1));
    }
}
