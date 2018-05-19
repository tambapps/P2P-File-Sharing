package tambapps.com.a2sfile_sharing.service;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import tambapps.com.a2sfile_sharing.MainActivity;
import tambapps.com.a2sfile_sharing.R;
import tambapps.com.a2sfile_sharing.io.FileProgressInputStream;

/**
 * Bound Service
 */

public class SendFileService extends FileForegroundService {
    private final static String TAG = "SendFileService";

    //private final static int maxConnections = 1;
    //private String address;

    private final static int bufferSize =  4096;
    private final static int progressPeriod = 4;



    private IBinder binder = new LocalBinder();
    private boolean canLeave = false;
    private Info info = null;
    private transient boolean canceled = false;
    ServerSocket serverSocket;

    private ObservableEmitter<Info> messageEmitter = new ObservableEmitter<Info>() {
        @Override
        public void setDisposable(Disposable d) {
            
        }

        @Override
        public void setCancellable(Cancellable c) {

        }

        @Override
        public boolean isDisposed() {
            return false;
        }

        @Override
        public ObservableEmitter<Info> serialize() {
            return null;
        }

        @Override
        public boolean tryOnError(Throwable t) {
            return false;
        }

        @Override
        public void onNext(Info value) {

        }

        @Override
        public void onError(Throwable error) {

        }

        @Override
        public void onComplete() {

        }
    };
    private Observable<Info> messageObservable = Observable.create(new ObservableOnSubscribe<Info>() {
        @Override
        public void subscribe(ObservableEmitter<Info> emitter) throws Exception {
            //executed on another thread
            messageEmitter = emitter;

        }
    });

    private Intent intent;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        this.intent = intent;
        Log.e("bind", String.valueOf(serverSocket));
        return binder;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(getApplicationContext(), "FINIIII", Toast.LENGTH_SHORT).show();
    }

    public SendFileService() {
        super("File Sender");
    }

    @Override
    void runService(Intent intent) {
        getNotifBuilder().setContentTitle("Starting file service");
        Intent intent1 = new Intent(this, MainActivity.class);
        intent1.setAction(Intent.ACTION_MAIN);
        intent1.addCategory(Intent.CATEGORY_LAUNCHER);
        intent1.setAction(getPackageName() + "." + getClass().getName());
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intent1, 0);
        getNotifBuilder().setContentIntent(pendingIntent);
        updateNotification();
        Uri fileUri = intent.getData();
        Log.i(TAG, "Creating server socket");
        InetAddress address;
        try {
            address = InetAddress.getByAddress(intent.getByteArrayExtra("address"));
            serverSocket = new ServerSocket(0, 1, address);
            serverSocket.setSoTimeout(1000*60);
        } catch (IOException e) {
            Log.e(TAG, "Couldn't create socket", e);
            messageEmitter.onNext(new Info("Couldn't start sending service. Please, retry"));
            return;
        }
        getNotifBuilder().setContentTitle("Waiting for a connection")
                .setProgress(0, 0, false);
        updateNotification();
        Log.i(TAG, "Waiting for a client connection");
        String sAddress = address.toString().substring(1);
        info = new Info("Waiting for a device to connect", sAddress, serverSocket.getLocalPort());
        messageEmitter.onNext(info);

        try (Socket socket = serverSocket.accept()) {
            File file = new File(fileUri.getPath());
            Log.i(TAG, "Starting transfer to " + socket.getRemoteSocketAddress().toString() + ":"
                    + socket.getPort());
            messageEmitter.onNext(info.set("Starting transfer to " + socket.getRemoteSocketAddress().toString() + ". You can now leave this screen"));
            canLeave = true;
            try (DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                 FileProgressInputStream fis = new FileProgressInputStream(file)) {
                dos.writeInt(progressPeriod);
                dos.writeInt(bufferSize);
                dos.writeInt(file.getName().length());
                dos.writeChars(file.getName());
                int count;
                int step = 0;
                int progress;
                byte[] buffer = new byte[bufferSize]; // or 4096, or more
                while ((count = fis.read(buffer)) > 0) {
                    step++;
                    if (step % progressPeriod == 0) {
                        dos.writeInt(progress = fis.getProgress());
                        updateProgress(progress);
                    }
                    dos.write(buffer, 0, count);
                }
                getNotifBuilder().setOngoing(false)
                        .setAutoCancel(true)
                        .setContentTitle("Upload success")
                        .setContentText(file.getName() + " was successfully sent")
                        .setProgress(0, 0, false);
                updateNotification();
                Log.e(TAG, "Successfully sent " + file.getName() +
                        " to " + socket.getInetAddress().toString() + ":" + socket.getPort());
                messageEmitter.onNext(info.set("Transferred file successfully"));
            }

            serverSocket.close();
        } catch (SocketTimeoutException e) {
            Log.i(TAG, "Couldn't connect to another device", e);
            getNotifBuilder().setProgress(0,0, false)
                    .setOngoing(false)
                    .setAutoCancel(true)
                    .setContentText("Couldn't connect to another device");
            messageEmitter.onNext(info.set("Couldn't connect to another device\nPlease, retry"));

        } catch (IOException e) {
            if (canceled) {
                getNotifBuilder().setContentTitle("Transfer canceled")
                        .setContentText("")
                        .setProgress(0, 0, false)
                        .setOngoing(false)
                        .setAutoCancel(true);
                Log.i(TAG, "Transfer canceled");
            } else {
                getNotifBuilder()
                        .setContentTitle("Error during file transfer")
                        .setContentText("An error occurred during the connexion to the client")
                        .setOngoing(false)
                        .setAutoCancel(true)
                        .setContentIntent(dialogActivityIntent("The transfer was aborted", e.getMessage()));

                messageEmitter.onNext(info.set("An error occurred during the transfer. Transfer aborted"));
                Log.e(TAG, "Error during receive task", e);
            }
            updateNotification();
        }
    }


    public Disposable subscribeToObservable(Consumer<Info> consumer) {
        Disposable disposable =  messageObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer);
        if (info != null) {
            messageEmitter.onNext(info);
        }
        Log.i("observed", String.valueOf( info != null));
        return disposable;
    }

    public class LocalBinder extends Binder {
        public SendFileService getService() {
            return SendFileService.this;
        }
    }

    public static class Info {
        private String text;
        private String ip;
        private int port;

        Info(String text) {
            this.text = text;
            ip = null;
            port = -1;
        }

        Info(String text, String ip, int port) {
            this.text = text;
            this.ip = ip;
            this.port = port;
        }

        Info set(String text) {
            this.text = text;
            return this;
        }

        public String getText() {
            return text;
        }

        public String getIp() {
            return ip;
        }

        public int getPort() {
            return port;
        }
    }

    public boolean canLeave() {
        return canLeave;
    }

    public void cancel() {
        canceled = true;
        try {
            serverSocket.close();
        } catch (IOException e) {

        }
    }

    @Override
    int largeIcon() {
        return R.drawable.upload2;
    }

    @Override
    int smallIcon() {
        return R.drawable.upload_little;
    }
}
