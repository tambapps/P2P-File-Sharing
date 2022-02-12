package com.tambapps.p2p.peer_transfer.android.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.tambapps.p2p.fandem.Fandem;
import com.tambapps.p2p.fandem.FileSender;
import com.tambapps.p2p.fandem.SenderPeer;
import com.tambapps.p2p.fandem.model.FileData;
import com.tambapps.p2p.fandem.model.SendingFileData;
import com.tambapps.p2p.fandem.util.FileUtils;
import com.tambapps.p2p.peer_transfer.android.util.NetworkUtils;
import com.tambapps.p2p.speer.Peer;

import com.tambapps.p2p.peer_transfer.android.R;
import com.tambapps.p2p.peer_transfer.android.analytics.CrashlyticsValues;
import com.tambapps.p2p.peer_transfer.android.service.event.SendingEventHandler;
import com.tambapps.p2p.speer.datagram.MulticastDatagramPeer;
import com.tambapps.p2p.speer.datagram.service.PeriodicMulticastService;
import com.tambapps.p2p.speer.exception.HandshakeFailException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by fonkoua on 05/05/18.
 */

public class FileSendingJobService extends FileJobService implements SendingEventHandler {

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private final PeriodicMulticastService<List<SenderPeer>> senderPeersMulticastService = Fandem.multicastService(scheduledExecutorService);

    @Override
    FileTask startTask(NotificationCompat.Builder notifBuilder,
                       NotificationManager notificationManager,
                       int notifId,
                       PersistableBundle bundle,
                       PendingIntent cancelIntent, FirebaseAnalytics analytics) {

        String peerString = bundle.getString("peer");
        Peer peer = Peer.parse(peerString);
        String fileName = bundle.getString("fileName");
        String deviceName = Build.MANUFACTURER + " " + Build.MODEL;
        long fileSize = bundle.getLong("fileSize");
        String checksum = null;
        try {
            Uri uri = Uri.parse(bundle.getString("fileUri"));
            checksum = FileUtils.computeChecksum(notifBuilder.mContext.getContentResolver().openInputStream(uri));
        } catch (IOException e) { }
        List<SenderPeer> senderPeers = Collections.singletonList(new SenderPeer(peer.getAddress(), peer.getPort(), deviceName, Collections.singletonList(new FileData(fileName, fileSize, checksum))));
        senderPeersMulticastService.setData(senderPeers);
        try {
            // the senderPeersMulticastService will close this peer when stopping
            MulticastDatagramPeer datagramPeer = new MulticastDatagramPeer(senderPeersMulticastService.getPort());
            NetworkInterface wifiNetworkInterface = NetworkUtils.findWifiNetworkInterface();
            if (wifiNetworkInterface != null) {
                // this is needed for multicast to work on hotspot (AKA mobile data sharing)
                datagramPeer.getSocket().setNetworkInterface(wifiNetworkInterface);
            }
            senderPeersMulticastService.start(datagramPeer, 1000L);
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Couldn't communicate sender key. The receiver will have to enter it manually", Toast.LENGTH_LONG).show();
        }
        return new SendingTask(this, notifBuilder, notificationManager, notifId, getContentResolver(), cancelIntent, analytics, senderPeersMulticastService,
            bundle.getString("filenames"))
                .execute(peerString,
                        bundle.getString("fileUri"),
                        fileName,
                        String.valueOf(bundle.getLong("fileSize")),
                    checksum);
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

        private final PeriodicMulticastService<List<SenderPeer>> senderPeersMulticastService;
        private FileSender fileSender;
        private ContentResolver contentResolver;
        private long startTime;

        SendingTask(SendingEventHandler eventHandler, NotificationCompat.Builder notifBuilder,
                    NotificationManager notificationManager,
                    int notifId,
                    ContentResolver contentResolver,
                    PendingIntent cancelIntent, FirebaseAnalytics analytics, PeriodicMulticastService<List<SenderPeer>> senderPeersMulticastService,
                    String fileNames) {
            super(eventHandler, notifBuilder, notificationManager, notifId, cancelIntent, analytics, fileNames);
            this.contentResolver = contentResolver;
            this.senderPeersMulticastService = senderPeersMulticastService;
        }

        void run(String... params) {
            FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
            crashlytics.setCustomKey(CrashlyticsValues.SHARING_ROLE, "SENDER");

            fileSender = new FileSender(Peer.parse(params[0]), this,
                    SOCKET_TIMEOUT);
            Uri fileUri = Uri.parse(params[1]);
            String fileName = params[2];
            long fileSize = Long.parseLong(params[3]);
            String checksum = params[4];
            crashlytics.setCustomKey(CrashlyticsValues.FILE_LENGTH, fileSize);

            getNotifBuilder().setContentTitle(getString(R.string.waiting_connection))
                    .setContentText(getString(R.string.waiting_connection_message, Fandem.toHexString(fileSender.getPeer())));
            updateNotification();
            try {
                List<SendingFileData> fileData = Collections.singletonList(new SendingFileData(fileName, fileSize, checksum, () -> contentResolver.openInputStream(fileUri)));
                fileSender.send(fileData);

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
                Log.e("FileSendingJobService", "error while sending file", e);
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
        public String onConnected(String remoteAddress, String fileNames) {
            this.startTime = System.currentTimeMillis();
            ((SendingEventHandler)eventHandler).onServiceStarted();
            senderPeersMulticastService.stop(true);
            return getString(R.string.sending_connected, fileNames);
        }

        @Override
        void dispose() {
            super.dispose();
            contentResolver = null;
            senderPeersMulticastService.stop(true);
        }

    }

    @Override
    void cancel() {
        super.cancel();
        senderPeersMulticastService.stop(true);
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
