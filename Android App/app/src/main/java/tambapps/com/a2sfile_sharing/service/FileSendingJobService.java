package tambapps.com.a2sfile_sharing.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.PersistableBundle;
import android.support.v4.app.NotificationCompat;

import com.tambapps.file_sharing.FileSender;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketTimeoutException;

import tambapps.com.a2sfile_sharing.R;

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
                        PendingIntent notifIntent) {
        return (FileTask) new SendingTask(notifBuilder, notificationManager, notifId, getContentResolver(), endRunnable, notifIntent)
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

    static class SendingTask extends FileTask<String> {

        private FileSender fileSender;
        private ContentResolver contentResolver;
        private String fileName;

        SendingTask(NotificationCompat.Builder notifBuilder,
                           NotificationManager notificationManager,
                           int notifId,
                           ContentResolver contentResolver,
                           Runnable endRunnable,
                           PendingIntent notifIntent) {
            super(notifBuilder, notificationManager, notifId, endRunnable, notifIntent);
            this.contentResolver = contentResolver;

        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                fileSender = new FileSender(params[0], Integer.parseInt(params[1]),
                        SOCKET_TIMEOUT);
                fileSender.setTransferListener(this);
                getNotifBuilder().setContentTitle("Waiting for a connection")
                .setContentText(fileSender.getIp() + ":" + fileSender.getPort());
                updateNotification();

                Uri fileUri = Uri.parse(params[2]);
                fileName = params[3];
                long fileSize = Long.parseLong(params[4]);
                fileSender.send(contentResolver.openInputStream(fileUri), fileName, fileSize);

                finishNotification()
                        .setContentTitle("Transfer completed")
                        .setContentText(fileName + " was successfully sent");
                updateNotification();

            } catch (SocketTimeoutException e) {
                finishNotification()
                        .setContentTitle("Transfer canceled")
                        .setContentText("No connection was made to this device");
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
            return null;
        }

        public void cancel() {

        }
        @Override
        public void onCancelled() {
            try {
                fileSender.interrupt();
            } catch (IOException ignored) {

            }
            getNotifBuilder().setContentText("Transfer canceled")
                    .setAutoCancel(true)
                    .setOngoing(false)
                    .setProgress(0, 0, false);
            updateNotification();
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
