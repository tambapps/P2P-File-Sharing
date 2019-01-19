package com.tambapps.p2p.peer_transfer.android.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.NotificationCompat;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.tambapps.p2p.file_sharing.FileSender;
import com.tambapps.p2p.peer_transfer.android.R;
import com.tambapps.p2p.peer_transfer.android.analytics.Constants;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by fonkoua on 05/05/18.
 */

public class FileSendingJobService extends FileJobService {

    @Override
    FileTask startTask(NotificationCompat.Builder notifBuilder,
                       NotificationManager notificationManager,
                       int notifId,
                       PersistableBundle bundle,
                       Runnable endRunnable,
                       PendingIntent cancelIntent, FirebaseAnalytics analytics) {
        return new SendingTask(notifBuilder, notificationManager, notifId, getContentResolver(), endRunnable, cancelIntent, analytics)
                .execute(bundle.getString("address"), String.valueOf(bundle.getInt("port")),
                        bundle.getString("fileUri"), bundle.getString("fileName"),
                        bundle.getString("fileSize"));
    }

    @Override
    int smallIcon() {
        return R.drawable.upload_little;
    }

    @Override
    int largeIcon() {
        return R.drawable.upload2;
    }

    static class SendingTask extends FileTask {

        private FileSender fileSender;
        private ContentResolver contentResolver;
        private String fileName;

        SendingTask(NotificationCompat.Builder notifBuilder,
                    NotificationManager notificationManager,
                    int notifId,
                    ContentResolver contentResolver,
                    Runnable endRunnable,
                    PendingIntent cancelIntent, FirebaseAnalytics analytics) {
            super(notifBuilder, notificationManager, notifId, endRunnable, cancelIntent, analytics);
            this.contentResolver = contentResolver;
        }

        void run(String... params) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, Constants.Value.SERVICE);
            bundle.putString(FirebaseAnalytics.Param.METHOD, "SEND");

            try {
                fileSender = new FileSender(params[0], Integer.parseInt(params[1]),
                        SOCKET_TIMEOUT);
            } catch (IOException e) {
                Crashlytics.logException(e);
                finishNotification()
                        .setContentTitle("Failed to start service")
                        .setContentText("Please, check your network connection");
                updateNotification();
                return;
            }

            fileSender.setTransferListener(this);
            getNotifBuilder().setContentTitle("Waiting for a connection")
                    .setContentText(fileSender.getIp() + ":" + fileSender.getPort());
            updateNotification();

            Uri fileUri = Uri.parse(params[2]);
            fileName = params[3];
            long fileSize = Long.parseLong(params[4]);
            try {
                fileSender.send(contentResolver.openInputStream(fileUri), fileName, fileSize);
                if (fileSender.isCanceled()) {
                    finishNotification()
                            .setContentTitle("Transfer canceled");
                } else {
                    finishNotification().setContentTitle("Transfer completed")
                            .setStyle(notifStyle.bigText(fileName + " was successfully sent"));
                }
            } catch (FileNotFoundException e) {
                Crashlytics.logException(e);
                finishNotification()
                        .setContentTitle("Transfer aborted")
                        .setContentText("The file couldn't be found");
            } catch (IOException e) {
                Crashlytics.logException(e);
                finishNotification()
                        .setContentTitle("Transfer aborted")
                        .setStyle(notifStyle.bigText("An error occurred during the transfer:\n" +
                        e.getMessage()));
            } catch (IllegalArgumentException e) {
                Crashlytics.logException(e);
                finishNotification()
                        .setContentTitle("Transfer aborted")
                        .setContentText("The file selected couldn't be sent");
            }

            getAnalytics().logEvent(FirebaseAnalytics.Event.SHARE, bundle);
        }

        @Override
        public void cancel() {
            if (!fileSender.isCanceled()) {
                fileSender.cancel();
            }
        }

        @Override
        public String onConnected(String remoteAddress, String fileName) {
            return "Sending " + fileName + "...";
        }

        @Override
        void dispose() {
            super.dispose();
            contentResolver = null;
        }
    }
}
