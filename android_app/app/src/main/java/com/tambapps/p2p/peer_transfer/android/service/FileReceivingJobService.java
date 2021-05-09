package com.tambapps.p2p.peer_transfer.android.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.PersistableBundle;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.tambapps.p2p.fandem.FileReceiver;
import com.tambapps.p2p.speer.Peer;

import com.tambapps.p2p.fandem.util.FileUtils;
import com.tambapps.p2p.peer_transfer.android.R;
import com.tambapps.p2p.peer_transfer.android.analytics.AnalyticsValues;
import com.tambapps.p2p.peer_transfer.android.analytics.CrashlyticsValues;
import com.tambapps.p2p.peer_transfer.android.service.event.TaskEventHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.channels.AsynchronousCloseException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by fonkoua on 13/05/18.
 */

public class FileReceivingJobService extends FileJobService {

    interface FileIntentProvider {
        PendingIntent ofFile(File file);
    }
    @Override
    FileTask startTask(NotificationCompat.Builder notifBuilder,
                       NotificationManager notificationManager,
                       final int notifId,
                       PersistableBundle bundle,
                       PendingIntent cancelIntent, FirebaseAnalytics analytics) {
        return new ReceivingTask(this, notifBuilder, notificationManager, notifId, cancelIntent,
                new FileIntentProvider() {
                    @Override
                    public PendingIntent ofFile(File file) {
                        Intent fileIntent = new Intent(Intent.ACTION_VIEW);
                        fileIntent.setData(FileProvider.getUriForFile(FileReceivingJobService.this,
                                getApplicationContext().getPackageName() + ".io", file));
                        fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        return PendingIntent.getActivity(FileReceivingJobService.this, notifId, fileIntent, PendingIntent.FLAG_UPDATE_CURRENT);                    }
                }, analytics)
                .execute(bundle.getString("downloadPath"), bundle.getString("peer"));
    }

    @Override
    int largeIcon() {
        return R.drawable.download2;
    }

    @Override
    int smallIcon() {
        return R.drawable.download;
    }

    static class ReceivingTask extends FileTask {

        private FileReceiver fileReceiver;
        private String fileName;
        private FileIntentProvider fileIntentProvider;

        ReceivingTask(TaskEventHandler taskEventHandler, NotificationCompat.Builder notifBuilder,
                      NotificationManager notificationManager,
                      int notifId,
                      PendingIntent cancelIntent,
                      FileIntentProvider fileIntentProvider, FirebaseAnalytics analytics) {
            super(taskEventHandler, notifBuilder, notificationManager, notifId, cancelIntent, analytics);
            this.fileIntentProvider = fileIntentProvider;
        }

        @Override
        protected void run(String... params) { //downloadPath, peer
            FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
            crashlytics.setCustomKey(CrashlyticsValues.SHARING_ROLE, "RECEIVER");
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, AnalyticsValues.SERVICE);
            bundle.putString(FirebaseAnalytics.Param.METHOD, "RECEIVE");

            final String dirPath = params[0];
            fileReceiver = new FileReceiver(true, this);

            getNotifBuilder().setContentTitle(getString(R.string.connecting))
                    .setContentText(getString(R.string.connecting_to, params[1]));
            updateNotification();

            // will be useful when file is partially written and an error occured
            AtomicReference<File> outputFileReference = new AtomicReference<>();
            try {
                File file = fileReceiver.receiveFrom(Peer.parse(params[1]), (name) -> {
                    File f = FileUtils.newAvailableFile(dirPath, name);
                    outputFileReference.set(f);
                    return f;
                });
                completeNotification(file);
            } catch (SocketException e) {
                // TODO verify if it works. It is supposed to happen when transfer is canceled (socket closed)
                NotificationCompat.Builder builder = finishNotification()
                        .setContentTitle(getString(R.string.transfer_canceled));
                File file = outputFileReference.get();
                if (file != null && file.exists() && !file.delete()) {
                    builder.setStyle(notifStyle.bigText(getString(R.string.incomplete_transfer)));
                }
            } catch (SocketTimeoutException e) {
                finishNotification()
                        .setContentTitle(getString(R.string.transfer_canceled))
                        .setContentText(getString(R.string.connection_timeout));
            } catch (AsynchronousCloseException e) {
                finishNotification()
                        .setContentTitle(getString(R.string.transfer_canceled));
            } catch (IOException e) {
                Log.e("FileReceivingJobService", "error while receiving", e);
                crashlytics.recordException(e);
                finishNotification()
                        .setContentTitle(getString(R.string.transfer_aborted))
                        .setStyle(notifStyle.bigText(getString(R.string.error_occured, e.getMessage())));
                File file = outputFileReference.get();
                if (file != null && file.exists() && !file.delete()) {
                    getNotifBuilder().setStyle(notifStyle.bigText(getString(R.string.error_incomplete)));
                }
            }
            getAnalytics().logEvent(FirebaseAnalytics.Event.SHARE, bundle);
        }

        private void completeNotification(File file) {
            finishNotification()
                    .setContentTitle(getString(R.string.transfer_complete))
                    .setContentIntent(fileIntentProvider.ofFile(file));

            Bitmap image = null;
            if (isImage(file)) {
                try (InputStream inputStream = new FileInputStream(file)) {
                    image = BitmapFactory.decodeStream(inputStream);
                } catch (IOException e) {
                }
            }
            if (image != null) {
                getNotifBuilder().setStyle(new NotificationCompat.BigPictureStyle()
                        .bigPicture(image).setSummaryText(fileName));
            } else {
                getNotifBuilder().setStyle(notifStyle.bigText(getString(R.string.success_received, fileName)));
            }
        }

        private boolean isImage(File file) {
            String fileName  = file.getName();
            int extensionIndex = fileName.lastIndexOf('.');
            if (extensionIndex > 0) {
                String extension = fileName.substring(extensionIndex + 1);
                for (String imageExtension : new String[]{"jpg", "png", "gif", "bmp"}) {
                    if (imageExtension.equalsIgnoreCase(extension)) {
                        return true;
                    }
                }
            }
            return false;
        }
        @Override
        public void cancel() {
            // TODO it is actually impossible to cancel a receive with this implementation
            //   make FileReceiver accept PeerConnection instead of peer, so that I can close it
            //   myself when I want to cancel
//            fileReceiver.cancel();
        }

        @Override
        void dispose() {
            super.dispose();
            fileIntentProvider = null;
        }

        @Override
        public String onConnected(String remoteAddress, String fileName, long fileSize) {
            this.fileName = fileName;
            FirebaseCrashlytics.getInstance().setCustomKey(CrashlyticsValues.FILE_LENGTH, fileSize);
            return getString(R.string.receveiving_connected, fileName);
        }
    }
}
