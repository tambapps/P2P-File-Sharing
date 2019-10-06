package com.tambapps.p2p.peer_transfer.android;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.OpenableColumns;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;

import com.tambapps.p2p.fandem.util.IPUtils;
import com.tambapps.p2p.peer_transfer.android.analytics.AnalyticsValues;
import com.tambapps.p2p.peer_transfer.android.service.FileSendingJobService;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Locale;

public class SendActivity extends AppCompatActivity {

    private static final int SENDING_JOB_ID = 0;
    private final static int PICK_FILE = 1;
    private FirebaseAnalytics analytics;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        analytics = FirebaseAnalytics.getInstance(this);

        setContentView(R.layout.activity_send);
        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, PICK_FILE);
            }
        });

        Intent intent = getIntent();
        String receivedAction = intent.getAction();

        if (Intent.ACTION_SEND.equals(receivedAction)) {
            Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (uri == null) {
                Toast.makeText(this, "Couldn't get file, sending canceled", Toast.LENGTH_SHORT).show();
                return;
            }

            Pair<InetAddress, Integer> peer = getPeer();
            if (peer == null) {
                return;
            }
            InetAddress address = peer.first;
            int port = peer.second;

            if (sendFile(uri, address, port)) {
                TextView textView = findViewById(R.id.text_view);
                textView.setText(("Started send service on\n" + address.getHostAddress() + ":"
                        + port +
                        "\n" +
                        "You can see the progress on the notification"));
                textView.setGravity(Gravity.CENTER_HORIZONTAL);
            }

        }

    }

    private boolean sendFile(Uri uri, InetAddress address, int port) {
        PersistableBundle bundle = new PersistableBundle();
        bundle.putString("address", address.getHostAddress());
        bundle.putInt("port", port);

        Pair<String, Long> fileInfos = getFileInfos(uri);
        bundle.putString("fileUri", uri.toString());
        bundle.putInt("id", SENDING_JOB_ID);

        if (fileInfos.first != null) {
            bundle.putString("fileName", fileInfos.first);
        } else {
            Toast.makeText(this, "Error: couldn't get name of file", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (fileInfos.second != null) {
            bundle.putString("fileSize", String.valueOf(fileInfos.second));
        } else {
            Toast.makeText(this, "Error: couldn't get size of file", Toast.LENGTH_SHORT).show();
            return false;
        }

        JobInfo.Builder jobInfoBuilder = new JobInfo.Builder(0,
                new ComponentName(this, FileSendingJobService.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setExtras(bundle);

        JobScheduler jobScheduler =
                (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        logSend(fileInfos.second);
        jobScheduler.schedule(jobInfoBuilder.build());
        return true;
    }

    private void logSend(Long fileSize) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, AnalyticsValues.SERVICE_START);
        bundle.putLong(FirebaseAnalytics.Param.QUANTITY, fileSize);
        bundle.putString(FirebaseAnalytics.Param.METHOD, "SEND");
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_FILE) {
            if (resultCode == RESULT_OK) {

                Pair<InetAddress, Integer> peer = getPeer();
                if (peer == null) {
                    Toast.makeText(this, "Network error, Couldn't start sending", Toast.LENGTH_SHORT).show();
                    return;
                }
                InetAddress address = peer.first;
                int port = peer.second;

                if (sendFile(data.getData(), address, port)) {
                    Intent returnIntent = new Intent();

                    String message = "Service started. " + String.format(Locale.US, "Waiting connection on %s:%d",
                            address.getHostAddress(), port);
                    returnIntent.putExtra(MainActivity.RETURN_TEXT_KEY, message);
                    setResult(RESULT_OK, returnIntent);
                    finish();
                }

            } else {
                Toast.makeText(this, "Couldn't get a file", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private Pair<InetAddress, Integer> getPeer() {
        InetAddress address;
        int port;

        try {
            address = IPUtils.getIPAddress();
            if (address == null) {
                Toast.makeText(this, "Couldn't get ip address. Please verify your internet connection", Toast.LENGTH_SHORT).show();
                return null;
            }
            port = IPUtils.getAvalaiblePort(address);
        } catch (IOException e) {
            Crashlytics.logException(e);
            Toast.makeText(this, "Error: couldn't get ip address", Toast.LENGTH_SHORT).show();
            return null;
        }
        return Pair.create(address, port);
    }

    private Pair<String, Long> getFileInfos(Uri uri) {
        Cursor cursor = getContentResolver()
                .query(uri, null, null, null, null, null);
        String name = null;
        Long size = null;

        try {
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    name = cursor.getString(
                            cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                    // If the size is unknown, the value stored is null.  But since a
                    // long can't be null we check that this column isn't null in the
                    // cursor
                    size = null;
                    if (!cursor.isNull(sizeIndex)) {
                        size = cursor.getLong(sizeIndex);
                    }
                }
            } else {
                File file = new File(uri.getPath());
                if (!file.exists()) {
                    return Pair.create(null, null);
                }
                return Pair.create(file.getName(), file.length());
            }

            return Pair.create(name, size);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
