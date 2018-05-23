package com.tambapps.p2p.peer_transfer.android.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.PersistableBundle;
import android.support.v4.app.NotificationCompat;

import com.tambapps.p2p.file_sharing.FileSender;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.tambapps.p2p.peer_transfer.android.R;

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
                       PendingIntent cancelIntent) {
        return new SendingTask(notifBuilder, notificationManager, notifId, getContentResolver(), endRunnable, cancelIntent)
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
                    PendingIntent cancelIntent) {
            super(notifBuilder, notificationManager, notifId, endRunnable, cancelIntent);
            this.contentResolver = contentResolver;
        }

        void run(String... params) {
            try {
                fileSender = new FileSender(params[0], Integer.parseInt(params[1]),
                        SOCKET_TIMEOUT);
            } catch (IOException e) {
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
                    finishNotification().setContentTitle("Transfer completed");//.setStyle(notifStyle.bigText("Transfer completed"));
                }

                updateNotification();

            } catch (FileNotFoundException e) {
                finishNotification()
                        .setContentTitle("Transfer aborted")
                        .setContentText("The file couldn't be found");
                updateNotification();
            } catch (IOException e) {
                finishNotification()
                        .setContentTitle("Transfer aborted")
                        .setContentText("An error occurred during the transfer");
                updateNotification();
            } catch (IllegalArgumentException e) {
                finishNotification()
                        .setContentTitle("Transfer aborted")
                        .setContentText("The file selected couldn't be sent");
                updateNotification();
            }
        }

        @Override
        public void cancel() {
            if (!fileSender.isCanceled()) {
                fileSender.cancel();
            }
        }

        @Override
        long bytesProcessed() {
            return fileSender.getBytesSent();
        }

        @Override
        long totalBytes() {
            return fileSender.getTotalBytes();
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
