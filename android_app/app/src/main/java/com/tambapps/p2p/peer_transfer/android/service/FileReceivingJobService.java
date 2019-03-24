package com.tambapps.p2p.peer_transfer.android.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;

import com.tambapps.p2p.fandem.Peer;

import com.tambapps.p2p.fandem.util.FileUtils;
import com.tambapps.p2p.peer_transfer.android.R;
import com.tambapps.p2p.peer_transfer.android.analytics.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.nio.channels.AsynchronousCloseException;

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
                       Runnable endRunnable,
                       PendingIntent cancelIntent, FirebaseAnalytics analytics) {
        return new ReceivingTask(notifBuilder, notificationManager, notifId, endRunnable, cancelIntent,
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

        private com.tambapps.p2p.fandem.task.ReceivingTask fileReceiver;
        private String fileName;
        private FileIntentProvider fileIntentProvider;

        ReceivingTask(NotificationCompat.Builder notifBuilder,
                      NotificationManager notificationManager,
                      int notifId,
                      Runnable endRunnable,
                      PendingIntent cancelIntent,
                      FileIntentProvider fileIntentProvider, FirebaseAnalytics analytics) {
            super(notifBuilder, notificationManager, notifId, endRunnable, cancelIntent, analytics);
            this.fileIntentProvider = fileIntentProvider;
        }

        @Override
        protected void run(String... params) { //downloadPath, peer
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, Constants.Value.SERVICE);
            bundle.putString(FirebaseAnalytics.Param.METHOD, "RECEIVE");

            final String dirPath = params[0];
            fileReceiver = new com.tambapps.p2p.fandem.task.ReceivingTask(this, new com.tambapps.p2p.fandem.task.FileProvider() {
                @Override
                public File newFile(String name) throws IOException {
                    return FileUtils.newAvailableFile(dirPath, name);
                }
            });

            getNotifBuilder().setContentTitle("Connecting...")
                    .setContentText("connecting to " + params[1]);
            updateNotification();

            try {
                fileReceiver.receiveFrom(Peer.parse(params[1]));
                File file = fileReceiver.getOutputFile();
                if (fileReceiver.isCanceled()) {
                    NotificationCompat.Builder builder = finishNotification()
                            .setContentTitle("Transfer canceled");
                    if (file.exists() && !file.delete()) {
                        builder.setStyle(notifStyle.bigText("The file couldn't be downloaded entirely. Please, delete it."));
                    }
                } else {
                    finishNotification()
                            .setContentTitle("Transfer completed")
                            .setContentIntent(fileIntentProvider.ofFile(file));

                    Bitmap image = null;
                    if (isImage(file)) {
                        try (InputStream inputStream = new FileInputStream(file)) {
                            image = BitmapFactory.decodeStream(inputStream);
                        } catch (IOException e) {
                            Crashlytics.log("Couldn't decode img");
                            Crashlytics.logException(e);
                        }
                    }
                    if (image != null) {
                        getNotifBuilder().setStyle(new NotificationCompat.BigPictureStyle()
                                .bigPicture(image).setSummaryText(fileName));
                    } else {
                        getNotifBuilder().setStyle(notifStyle.bigText(fileName + " was successfully received"));
                    }
                }
            } catch (SocketTimeoutException e) {
                Crashlytics.logException(e);
                finishNotification()
                        .setContentTitle("Transfer canceled")
                        .setContentText("Connection timeout");
            } catch (AsynchronousCloseException e) {
                Crashlytics.logException(e);
                finishNotification()
                        .setContentTitle("Transfer canceled");
            } catch (IOException e) {
                Log.e("FileReceivingJobService", "error while receiving", e);
                Crashlytics.logException(e);
                finishNotification()
                        .setContentTitle("Transfer aborted")
                        .setStyle(notifStyle.bigText("An error occurred during the transfer:\n" +
                                e.getMessage()));
                File file = fileReceiver.getOutputFile();
                if (file != null && file.exists() && !file.delete()) {
                    getNotifBuilder().setStyle(notifStyle.bigText("An error occurred during the transfer.\n" +
                            "The file couldn't be downloaded entirely. Please, delete it."));
                }
            }
            getAnalytics().logEvent(FirebaseAnalytics.Event.SHARE, bundle);
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
            if (!fileReceiver.isCanceled()) {
                fileReceiver.cancel();
            }
        }

        @Override
        void dispose() {
            super.dispose();
            fileIntentProvider = null;
        }

        @Override
        public String onConnected(String remoteAddress, String fileName) {
            this.fileName = fileName;
            return "Receiving " + fileName;
        }
    }
}
