package com.tambapps.p2p.peer_transfer.android.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.PersistableBundle;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

import android.provider.MediaStore;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.tambapps.p2p.fandem.FileReceiver;
import com.tambapps.p2p.fandem.exception.CorruptedFileException;
import com.tambapps.p2p.fandem.util.OutputStreamProvider;
import com.tambapps.p2p.peer_transfer.android.ManageFilesActivity;
import com.tambapps.p2p.speer.Peer;

import com.tambapps.p2p.fandem.util.FileUtils;
import com.tambapps.p2p.peer_transfer.android.R;
import com.tambapps.p2p.peer_transfer.android.analytics.CrashlyticsValues;
import com.tambapps.p2p.peer_transfer.android.service.event.TaskEventHandler;
import com.tambapps.p2p.speer.exception.HandshakeFailException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.channels.AsynchronousCloseException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by fonkoua on 13/05/18.
 */

public class FileReceivingJobService extends FileJobService {

    static public interface FileIntentProvider {
        // only for Android before 11
        PendingIntent ofFile(File file);
    }
    @Override
    FileTask startTask(NotificationCompat.Builder notifBuilder,
                       NotificationManager notificationManager,
                       final int notifId,
                       PersistableBundle bundle,
                       PendingIntent cancelIntent, FirebaseAnalytics analytics) {
        File file = null;
        if (bundle.getString("file") != null) {
            file = new File(bundle.getString("file"));
        }
        return new ReceivingTask(this, notifBuilder, notificationManager, notifId, cancelIntent, file,
                new FileIntentProvider() {
                    @Override
                    public PendingIntent ofFile(File file) {
                        Intent fileIntent = new Intent(Intent.ACTION_VIEW);
                        fileIntent.setData(FileProvider.getUriForFile(FileReceivingJobService.this,
                                getApplicationContext().getPackageName() + ".io", file));
                        fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        return PendingIntent.getActivity(FileReceivingJobService.this, notifId, fileIntent, PendingIntent.FLAG_UPDATE_CURRENT);                    }

                }, analytics)
                .execute(bundle.getString("uri"), bundle.getString("peer"));
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
        // only for Android before 11
        private final File file;
        private FileIntentProvider fileIntentProvider;
        // will be useful when file is partially written and an error occurred
        private final AtomicReference<File> outputFileReference = new AtomicReference<>();
        private long startTime;
        private final ContentResolver contentResolver;

        ReceivingTask(TaskEventHandler taskEventHandler, NotificationCompat.Builder notifBuilder,
                      NotificationManager notificationManager,
                      int notifId,
                      PendingIntent cancelIntent,
                      File file, FileIntentProvider fileIntentProvider, FirebaseAnalytics analytics) {
            super(taskEventHandler, notifBuilder, notificationManager, notifId, cancelIntent, analytics);
            this.file = file;
            this.fileIntentProvider = fileIntentProvider;
            this.contentResolver = notifBuilder.mContext.getContentResolver();
        }

        @Override
        protected void run(String... params) { //downloadPath, peer
            FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
            crashlytics.setCustomKey(CrashlyticsValues.SHARING_ROLE, "RECEIVER");

            final Uri uri = Uri.parse(params[0]);
            fileReceiver = new FileReceiver(true, this);
            // only for android 11+
            boolean deleteUri = Build.VERSION.SDK_INT < Build.VERSION_CODES.R;

            getNotifBuilder().setContentTitle(getString(R.string.connecting))
                    .setContentText(getString(R.string.connecting_to, params[1]));
            updateNotification();

            try (ParcelFileDescriptor pfd = contentResolver.openFileDescriptor(uri, "w")) {
                long fileLength = fileReceiver.receiveFrom(Peer.parse(params[1]), (OutputStreamProvider) (name) ->
                                new FileOutputStream(
                                        pfd.getFileDescriptor())
                        );

                deleteUri = false;
                completeNotification(uri, fileName, fileLength);
                updateNotification();
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.METHOD, "RECEIVE");
                bundle.putLong("size", fileLength);
                bundle.putLong("duration", System.currentTimeMillis() - startTime);
                getAnalytics().logEvent(FirebaseAnalytics.Event.SHARE, bundle);
            } catch (HandshakeFailException e) {
                finishNotification()
                        .setContentTitle(getString(R.string.couldnt_start))
                        .setContentText(e.getMessage());
            } catch (CorruptedFileException e) {
                finishNotification()
                        .setContentTitle(getString(R.string.corrupted_file))
                        .setContentText(e.getMessage());
            } catch (SocketException e) {
                NotificationCompat.Builder builder = finishNotification()
                        .setContentTitle(getString(R.string.transfer_canceled));
                File file = outputFileReference.get();
                // TODO handle errors. Only Android 11+ devices should get text 'delete it yourself'
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
            if (deleteUri) {
                contentResolver.delete(uri, null);
            }
        }

        private void completeNotification(Uri uri, String fileName, long fileLength) {
            NotificationCompat.Builder builder = finishNotification()
                    .setContentTitle(getString(R.string.transfer_complete));

            if (file != null && file.exists()) {
                // the file conversion is REQUIRED, ESPECIALLY FOR THIS
                builder.setContentIntent(fileIntentProvider.ofFile(file));
            }

            Bitmap image = null;
            if (file != null && isImage(file)) {
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
            builder.setStyle(notifStyle.bigText(getString(R.string.success_received, fileName)));
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
            fileReceiver.cancel();
            File file = outputFileReference.get();
            if (file != null && file.exists() && !file.delete()) {
                // do nothing, let's assume the file deletion will always succeed
            }
        }

        @Override
        void dispose() {
            super.dispose();
            fileIntentProvider = null;
        }

        @Override
        public String onConnected(String remoteAddress, String fileName, long fileSize) {
            this.fileName = fileName;
            this.startTime = System.currentTimeMillis();
            FirebaseCrashlytics.getInstance().setCustomKey(CrashlyticsValues.FILE_LENGTH, fileSize);
            return getString(R.string.receveiving_connected, fileName);
        }
    }
}
