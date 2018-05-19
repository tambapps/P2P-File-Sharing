package tambapps.com.a2sfile_sharing.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.os.PersistableBundle;
import android.support.v4.app.NotificationCompat;

import com.tambapps.file_sharing.FileReceiver;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;

import tambapps.com.a2sfile_sharing.R;

/**
 * Created by fonkoua on 13/05/18.
 */

public class FileReceivingJobService extends FileJobService {

    @Override
    FileTask startTask(NotificationCompat.Builder notifBuilder,
                        NotificationManager notificationManager,
                        int notifId,
                        PersistableBundle bundle,
                        Runnable endRunnable,
                        PendingIntent notifIntent) {
        return (FileTask) new ReceivingTask(notifBuilder, notificationManager, notifId, endRunnable, notifIntent)
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

    static class ReceivingTask extends FileTask<String> {

        private FileReceiver fileReceiver;
        private String fileName;

        ReceivingTask(NotificationCompat.Builder notifBuilder,
                      NotificationManager notificationManager,
                      int notifId,
                      Runnable endRunnable,
                      PendingIntent notifIntent) {
            super(notifBuilder, notificationManager, notifId, endRunnable, notifIntent);
        }

        @Override
        protected Void doInBackground(String... params) {
            fileReceiver = new FileReceiver(params[0]);
            fileReceiver.setTransferListener(this);
            getNotifBuilder().setContentTitle("Connecting...")
                    .setContentText("connecting to " + params[1]);
            updateNotification();
            try {
                fileReceiver.receiveFrom(params[1]);

                finishNotification()
                        .setContentTitle("Transfer completed")
                        .setContentText(fileName + " was successfully received");

                //TODO file open notificationClick USE FUTURE?????? OU CALLBACK???
                updateNotification();
            } catch (SocketTimeoutException e) {
                finishNotification()
                        .setContentTitle("Transfer canceled")
                        .setContentText("Connection timeout");
            } catch (IOException e) {
                finishNotification()
                        .setContentTitle("Transfer aborted")
                        .setContentText("An error occurred during the transfer");
            }

            return null;
        }

        @Override
        public void onCancelled() {
            try {
                File file = fileReceiver.interrupt();
                file.delete();
            } catch (IOException ignored) {

            }

            finishNotification().setContentText("Transfer canceled");
            updateNotification();
        }

        @Override
        long bytesProcessed() {
            return fileReceiver.getBytesReceived();
        }

        @Override
        long totalBytes() {
            return fileReceiver.getTotalBytes();
        }

        @Override
        public String onConnected(String remoteAddress, String fileName) {
            this.fileName = fileName;
            return "Receiving " + fileName;
        }
    }
}
