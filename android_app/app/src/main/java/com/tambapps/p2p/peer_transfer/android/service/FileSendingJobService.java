package com.tambapps.p2p.peer_transfer.android.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.core.app.NotificationCompat;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.tambapps.p2p.fandem.Peer;
import com.tambapps.p2p.fandem.listener.SendingListener;

import com.tambapps.p2p.peer_transfer.android.R;
import com.tambapps.p2p.peer_transfer.android.analytics.AnalyticsValues;
import com.tambapps.p2p.peer_transfer.android.service.sniff.SniffHandlerService;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * Created by fonkoua on 05/05/18.
 */

public class FileSendingJobService extends FileJobService {

    private final SniffHandlerService sniffHandlerService = new SniffHandlerService();

    @Override
    FileTask startTask(NotificationCompat.Builder notifBuilder,
                       NotificationManager notificationManager,
                       int notifId,
                       PersistableBundle bundle,
                       Runnable endRunnable,
                       PendingIntent cancelIntent, FirebaseAnalytics analytics) {

        String peerHexCode = bundle.getString("peer");
        String fileName = bundle.getString("fileName");
        sniffHandlerService.startInBackground(this, peerHexCode, fileName);
        return new SendingTask(notifBuilder, notificationManager, notifId, getContentResolver(),
                endRunnable, cancelIntent, analytics, sniffHandlerService,
                new Runnable() {
                    @Override
                    public void run() {
                        sendBroadcast(new Intent(SendingStartedBroadcastReceiver.SENDING_STARTED));
                    }
                })
                .execute(peerHexCode,
                        bundle.getString("fileUri"),
                        fileName,
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

    static class SendingTask extends FileTask implements SendingListener {

        private final SniffHandlerService sniffHandlerService;
        private final Runnable sendBroadcastRunnable;
        private com.tambapps.p2p.fandem.task.SendingTask fileSender;
        private ContentResolver contentResolver;
        private String fileName;

        SendingTask(NotificationCompat.Builder notifBuilder,
                    NotificationManager notificationManager,
                    int notifId,
                    ContentResolver contentResolver,
                    Runnable endRunnable,
                    PendingIntent cancelIntent, FirebaseAnalytics analytics, SniffHandlerService sniffHandlerService,
                    Runnable sendBroadcastRunnable) {
            super(notifBuilder, notificationManager, notifId, endRunnable, cancelIntent, analytics);
            this.contentResolver = contentResolver;
            this.sniffHandlerService = sniffHandlerService;
            this.sendBroadcastRunnable = sendBroadcastRunnable;
        }

        void run(String... params) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, AnalyticsValues.SERVICE);
            bundle.putString(FirebaseAnalytics.Param.METHOD, "SEND");

            fileSender = new com.tambapps.p2p.fandem.task.SendingTask(this, Peer.fromHexString(params[0]),
                    SOCKET_TIMEOUT);
            Uri fileUri = Uri.parse(params[1]);
            fileName = params[2];
            long fileSize = Long.parseLong(params[3]);
            try {
                fileSender.send(contentResolver.openInputStream(fileUri), fileName, fileSize);
                if (fileSender.isCanceled()) {
                    finishNotification()
                            .setContentTitle(getString(R.string.transfer_canceled));
                } else {
                    finishNotification().setContentTitle(getString(R.string.transfer_complete))
                            .setStyle(notifStyle.bigText(getString(R.string.success_send, fileName)));
                }
            } catch (SocketTimeoutException e) {
                Crashlytics.logException(e);
                finishNotification()
                        .setContentTitle(getString(R.string.transfer_canceled))
                        .setContentText(getString(R.string.connection_timeout));
            } catch (FileNotFoundException e) {
                Crashlytics.logException(e);
                finishNotification()
                        .setContentTitle(getString(R.string.transfer_aborted))
                        .setContentText(getString(R.string.couldnt_find_file));
            } catch (IOException e) {
                Crashlytics.logException(e);
                finishNotification()
                        .setContentTitle(getString(R.string.transfer_aborted))
                        .setStyle(notifStyle.bigText(getString(R.string.error_occured, e.getMessage())));
            } catch (IllegalArgumentException e) {
                Crashlytics.logException(e);
                finishNotification()
                        .setContentTitle(getString(R.string.transfer_aborted))
                        .setContentText(getString(R.string.coudlnt_send));
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
            sendBroadcastRunnable.run();
            sniffHandlerService.stop();
            return getString(R.string.sending_connected, fileName);
        }

        @Override
        void dispose() {
            super.dispose();
            contentResolver = null;
            sniffHandlerService.stop();
        }

        @Override
        public void onStart(Peer peer, String s) {
            getNotifBuilder().setContentTitle(getString(R.string.waiting_connection))
                    .setContentText(getString(R.string.waiting_connection_message, peer, peer.toHexString()));
            updateNotification();
        }
    }

    @Override
    void cancel() {
        super.cancel();
        sniffHandlerService.stop();
    }
}
