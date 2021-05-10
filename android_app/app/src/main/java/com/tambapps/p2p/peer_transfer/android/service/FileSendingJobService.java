package com.tambapps.p2p.peer_transfer.android.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.core.app.NotificationCompat;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.tambapps.p2p.fandem.Fandem;
import com.tambapps.p2p.fandem.FileSender;
import com.tambapps.p2p.speer.Peer;

import com.tambapps.p2p.peer_transfer.android.R;
import com.tambapps.p2p.peer_transfer.android.analytics.CrashlyticsValues;
import com.tambapps.p2p.peer_transfer.android.service.event.SendingEventHandler;
import com.tambapps.p2p.peer_transfer.android.service.sniff.SniffHandlerService;
import com.tambapps.p2p.speer.exception.HandshakeFailException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * Created by fonkoua on 05/05/18.
 */

public class FileSendingJobService extends FileJobService implements SendingEventHandler {

    private final SniffHandlerService sniffHandlerService = new SniffHandlerService();

    @Override
    FileTask startTask(NotificationCompat.Builder notifBuilder,
                       NotificationManager notificationManager,
                       int notifId,
                       PersistableBundle bundle,
                       PendingIntent cancelIntent, FirebaseAnalytics analytics) {

        String peerString = bundle.getString("peer");
        String fileName = bundle.getString("fileName");
        sniffHandlerService.startInBackground(this, peerString, fileName);
        return new SendingTask(this, notifBuilder, notificationManager, notifId, getContentResolver(), cancelIntent, analytics, sniffHandlerService)
                .execute(peerString,
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

    static class SendingTask extends FileTask {

        private final SniffHandlerService sniffHandlerService;
        private FileSender fileSender;
        private ContentResolver contentResolver;
        private String fileName;
        private long startTime;

        SendingTask(SendingEventHandler eventHandler, NotificationCompat.Builder notifBuilder,
                    NotificationManager notificationManager,
                    int notifId,
                    ContentResolver contentResolver,
                    PendingIntent cancelIntent, FirebaseAnalytics analytics, SniffHandlerService sniffHandlerService) {
            super(eventHandler, notifBuilder, notificationManager, notifId, cancelIntent, analytics);
            this.contentResolver = contentResolver;
            this.sniffHandlerService = sniffHandlerService;
        }

        void run(String... params) {
            FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
            crashlytics.setCustomKey(CrashlyticsValues.SHARING_ROLE, "SENDER");

            fileSender = new FileSender(Peer.parse(params[0]), this,
                    SOCKET_TIMEOUT);
            Uri fileUri = Uri.parse(params[1]);
            fileName = params[2];
            long fileSize = Long.parseLong(params[3]);
            crashlytics.setCustomKey(CrashlyticsValues.FILE_LENGTH, fileSize);

            getNotifBuilder().setContentTitle(getString(R.string.waiting_connection))
                    .setContentText(getString(R.string.waiting_connection_message, Fandem.toHexString(fileSender.getPeer())));
            updateNotification();
            try {
                fileSender.send(contentResolver.openInputStream(fileUri), fileName, fileSize,
                        () -> fileSender.computeChecksum(contentResolver.openInputStream(fileUri)));

                finishNotification().setContentTitle(getString(R.string.transfer_complete))
                        .setStyle(notifStyle.bigText(getString(R.string.success_send, fileName)));

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.METHOD, "SEND");
                bundle.putLong("size", fileSize);
                bundle.putLong("duration", System.currentTimeMillis() - startTime);
                getAnalytics().logEvent(FirebaseAnalytics.Event.SHARE, bundle);
            } catch (HandshakeFailException e) {
                finishNotification()
                        .setContentTitle(getString(R.string.couldnt_start))
                        .setContentText(e.getMessage());
            } catch (SocketException e) {
                // probably a cancel
                finishNotification()
                        .setContentTitle(getString(R.string.transfer_canceled));
                ((SendingEventHandler)eventHandler).onServiceTimeout();
            } catch (SocketTimeoutException e) {
                finishNotification()
                        .setContentTitle(getString(R.string.transfer_canceled))
                        .setContentText(getString(R.string.connection_timeout));
                ((SendingEventHandler)eventHandler).onServiceTimeout();
            } catch (FileNotFoundException e) {
                finishNotification()
                        .setContentTitle(getString(R.string.transfer_aborted))
                        .setContentText(getString(R.string.couldnt_find_file));
            } catch (IOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                finishNotification()
                        .setContentTitle(getString(R.string.transfer_aborted))
                        .setStyle(notifStyle.bigText(getString(R.string.error_occured, e.getMessage())));
            } catch (IllegalArgumentException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                finishNotification()
                        .setContentTitle(getString(R.string.transfer_aborted))
                        .setContentText(getString(R.string.coudlnt_send));
            }
        }

        @Override
        public void cancel() {
            fileSender.cancel();
        }

        @Override
        public String onConnected(String remoteAddress, String fileName, long fileSize) {
            this.startTime = System.currentTimeMillis();
            ((SendingEventHandler)eventHandler).onServiceStarted();
            sniffHandlerService.stop();
            return getString(R.string.sending_connected, fileName);
        }

        @Override
        void dispose() {
            super.dispose();
            contentResolver = null;
            sniffHandlerService.stop();
        }

    }

    @Override
    void cancel() {
        super.cancel();
        sniffHandlerService.stop();
    }

    @Override
    public void onServiceStarted() {
        sendBroadcast(new Intent(SendingEventBroadcastReceiver.SENDING_STARTED));
    }

    @Override
    public void onServiceTimeout() {
        sendBroadcast(new Intent(SendingEventBroadcastReceiver.SERVICE_TIMEOUT));
    }
}
