package tambapps.com.a2sfile_sharing.service;

import android.content.Intent;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import tambapps.com.a2sfile_sharing.R;

/**
 * Created by fonkoua on 24/03/18.
 */

public class ReceiveFileService extends FileForegroundService {

    private final static String TAG = "ReceiveFileService";

    public ReceiveFileService() {
        super("File Receiver");
    }

    @Override
    void runService(Intent intent) {
        getNotifBuilder().setContentTitle("Starting connection")
        .setProgress(0,0 , false);
        updateNotification();

        String uploadPath = intent.getStringExtra("uploadPath");
        String ip = intent.getStringExtra("ip");
        int port = intent.getIntExtra("port", 0);
        Log.i(TAG, "starting connection");
        try (Socket socket = new Socket(InetAddress.getByName(ip), port);
             DataInputStream dis = new DataInputStream(socket.getInputStream())) {
            int progressPeriod = dis.readInt();
            int bufferSize = dis.readInt();
            String fileName = readName(dis);
            File outputFile = newFile(uploadPath, fileName);
            Log.i(TAG, "starting downloading " + fileName);
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[bufferSize]; // or 4096, or more
                int count;
                int step = 0;
                while ((count = dis.read(buffer)) > 0) {
                    step++;
                    if ((step % progressPeriod) == 0) {
                        updateProgress(dis.readInt());
                    }
                    fos.write(buffer, 0, count);
                }
                Intent fileIntent = new Intent(Intent.ACTION_VIEW);
                fileIntent.setData(FileProvider.getUriForFile(this,
                        getApplicationContext().getPackageName() + ".io", outputFile));
                fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                getNotifBuilder().setOngoing(false)
                        .setAutoCancel(true)
                        .setContentIntent(createPI(fileIntent))
                        .setContentTitle("Download success")
                        .setContentText("View " + fileName)
                        .setProgress(0, 0, false);
                updateNotification();
                Log.i(TAG, "successfully downloaded " + fileName);
            } catch (SocketTimeoutException e) {
                Log.e(TAG, "Couldn't connect to another device", e);
                getNotifBuilder().setProgress(0,0, false)
                        .setOngoing(false)
                        .setAutoCancel(true)
                        .setContentText("Couldn't connect to another device");
            } catch (IOException e) {
                Log.e(TAG, "error while downloading file", e);
                getNotifBuilder().setProgress(0,0, false)
                        .setOngoing(false)
                        .setAutoCancel(true)
                        .setContentText("an error occurred during the transfer of file")
                        .setContentIntent(dialogActivityIntent(fileName +" couldn't be fully downloaded",
                                null));
                updateNotification();
                outputFile.delete();
            }
        } catch (IOException e) {
            Log.e(TAG, "failed to connect", e);
            getNotifBuilder().setContentText("an error occurred during the connexion to the server")
                    .setOngoing(false)
                    .setAutoCancel(true)
                    .setContentIntent(dialogActivityIntent("The transfer was aborted", null));
            updateNotification();
        }
    }

    private File newFile(String directory, String name) throws IOException {
        File file = new File(directory, name);
        if (file.exists()) { //searching available file name
            for (int i = 1; file.exists() && i < 999; i++) {
                StringBuilder number = new StringBuilder(String.valueOf(i));
                while (number.length() < 3) {
                    number.insert(0, '0');
                }
                String fileName;
                if (name.contains(".")) {
                    int dotIndex = name.indexOf('.');
                    fileName = name.substring(0, dotIndex) + '_' + number + name.substring(dotIndex);
                } else {
                    fileName = name + '_' + number;
                }
                file = new File(directory, fileName);
            }
        }
        if (!file.createNewFile()) {
            throw new IOException("Couldn't create file");
        }
        return file;
    }

    private String readName(DataInputStream dis) throws IOException {
        char[] name = new char[dis.readInt()];
        for (int i = 0; i < name.length; i++) {
            name[i] = dis.readChar();
        }
        return new String(name);
    }

    @Override
    int largeIcon() {
        return R.drawable.download2;
    }

    @Override
    int smallIcon() {
        return R.drawable.download;
    }
}
