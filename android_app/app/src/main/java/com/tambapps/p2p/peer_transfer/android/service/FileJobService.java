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
import androidx.core.app.NotificationCompat;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.tambapps.p2p.fandem.Peer;
import com.tambapps.p2p.fandem.listener.TransferListener;
import com.tambapps.p2p.peer_transfer.android.R;
import com.tambapps.p2p.peer_transfer.android.service.event.TaskEventHandler;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by fonkoua on 13/05/18.
 */

public abstract class FileJobService extends JobService implements TaskEventHandler {

    private final static ExecutorService executor = Executors.newCachedThreadPool();
    final static int SOCKET_TIMEOUT = 1000 * 60 * 2; //in ms
    private FirebaseAnalytics analytics;


    private NotificationBroadcastReceiver notificationBroadcastReceiver;
    private String ACTION_CANCEL;
    private FileTask fileTask;
    private volatile JobParameters params;

    @Override
    public void onCreate() {
        super.onCreate();
        analytics = FirebaseAnalytics.getInstance(this);
        ACTION_CANCEL = getClass().getName() + ".cancel";
        notificationBroadcastReceiver = new NotificationBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        intentFilter.addAction(ACTION_CANCEL);
        registerReceiver(notificationBroadcastReceiver, intentFilter);
    }

    @Override
    public boolean onStartJob(final JobParameters params) {
        this.params = params;
        PersistableBundle bundle = params.getExtras();
        NotificationManager notificationManager  = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        int notifId = ThreadLocalRandom.current().nextInt();

        PendingIntent cancelIntent = PendingIntent.getBroadcast(this, 0,
                new Intent(ACTION_CANCEL), 0);

        fileTask = startTask(buildNotification(notificationManager, notifId),
                notificationManager, notifId, bundle, cancelIntent, analytics);
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
                                PendingIntent cancelIntent, FirebaseAnalytics analytics);
    abstract int smallIcon();
    abstract int largeIcon();

    @Override
    public boolean onStopJob(JobParameters params) {
       cancel();
        return false;
    }

    void cancel() {
        if (fileTask != null) {
            fileTask.cancel();
        }
    }

    static abstract class FileTask implements TransferListener {
        private NotificationCompat.Builder notifBuilder;
        NotificationCompat.BigTextStyle notifStyle;
        private NotificationManager notificationManager;
        private final int notifId;
        private FirebaseAnalytics analytics;
        protected TaskEventHandler eventHandler;
        private long lastNotificationUpdate = 0L;

        FileTask(TaskEventHandler eventHandler, NotificationCompat.Builder notifBuilder,
                 NotificationManager notificationManager,
                 int notifId,
                 PendingIntent cancelIntent, FirebaseAnalytics analytics) {
            this.notifBuilder = notifBuilder;
            this.notificationManager = notificationManager;
            this.notifId = notifId;
            this.eventHandler = eventHandler;
            this.analytics = analytics;
            notifStyle = new NotificationCompat.BigTextStyle();

            notifBuilder.addAction(android.R.drawable.ic_delete, "cancel", cancelIntent);
        }

        @Override
        public void onProgressUpdate(int progress, long bytesProcessed, long totalBytes) {
            if (progress < 100) {
                notifBuilder.setProgress(100, progress, false);
                notifStyle.bigText(FileJobService.bytesToString(bytesProcessed) + "/ " + FileJobService.bytesToString(totalBytes));
            }
            long now = System.currentTimeMillis();
            if (now - lastNotificationUpdate >= 500L) {
                updateNotification();
                lastNotificationUpdate = now;
            }
        }

        FileTask execute(final String... params) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    FileTask.this.run(params);
                    updateNotification();
                    eventHandler.onEnd();
                    dispose();
                }
            });
            return this;
        }

        @Override
        public final void onConnected(Peer selfPeer, Peer remotePeer, String fileName, long fileSize) {
            String remotePeer1 = remotePeer.toString();
            getNotifBuilder()
                    .setOngoing(true)
                    .setProgress(100, 0, false)
                    .setContentText("")
                    .setContentTitle(onConnected(remotePeer1, fileName))
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
            eventHandler = null;
            analytics = null;
        }

        public FirebaseAnalytics getAnalytics() {
            return analytics;
        }

        private Context getContext() {
            return notifBuilder.mContext;
        }

        String getString(int rId, Object... args) {
            return getContext().getString(rId, args);
        }
    }

    public class NotificationBroadcastReceiver extends BroadcastReceiver {

        @Override
        public final void onReceive(Context context, Intent intent) {
            if (ACTION_CANCEL.equals(intent.getAction())) {
                cancel();
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

    public Future startSideTask(Runnable runnable) {
        return executor.submit(runnable);
    }

    @Override
    public void onEnd() {
        unregisterReceiver(notificationBroadcastReceiver);
        jobFinished(params, false);
    }
}
