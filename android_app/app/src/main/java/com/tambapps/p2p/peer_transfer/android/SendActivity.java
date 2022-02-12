package com.tambapps.p2p.peer_transfer.android;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.OpenableColumns;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import com.google.gson.Gson;
import com.tambapps.p2p.fandem.Fandem;
import com.tambapps.p2p.fandem.SenderPeer;
import com.tambapps.p2p.peer_transfer.android.model.AndroidFileData;
import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.peer_transfer.android.analytics.AnalyticsValues;
import com.tambapps.p2p.peer_transfer.android.service.FileSendingJobService;
import com.tambapps.p2p.peer_transfer.android.service.SendingEventBroadcastReceiver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SendActivity extends TransferActivity {

    private final static int PICK_FILE = 1;
    private final IntentFilter intentFilter = new IntentFilter();

    private FirebaseAnalytics analytics;
    private SendingEventBroadcastReceiver broadcastReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        analytics = FirebaseAnalytics.getInstance(this);
        intentFilter.addAction(SendingEventBroadcastReceiver.SENDING_STARTED);
        intentFilter.addAction(SendingEventBroadcastReceiver.SERVICE_TIMEOUT);

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
                Toast.makeText(this, this.getString(R.string.couldnt_get_file), Toast.LENGTH_SHORT).show();
                return;
            }

            Peer peer = getPeer();
            if (peer == null) {
                return;
            }
            if (sendFile(Collections.singletonList(uri), peer)) {
                sendContent(peer);
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        broadcastReceiver = new SendingEventBroadcastReceiver(this);
        registerReceiver(broadcastReceiver, intentFilter);
    }


    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    private void sendContent(Peer peer) {
        final String hexKey = Fandem.toHexString(peer);
        TextView textView = findViewById(R.id.text_view);
        textView.setText((getString(R.string.started_send_service_message, hexKey)));
        textView.setGravity(Gravity.CENTER);
        textView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                copyToClipboard(hexKey);
                return true;
            }
        });
        LinearLayout linearLayout = findViewById(R.id.parent);
        for (int i = linearLayout.getChildCount() - 1; i >= 0; i--) {
            if (linearLayout.getChildAt(i) != textView) {
                linearLayout.removeViewAt(i);
            }
        }
    }

    private void copyToClipboard(String key) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("Fandem peer key", key));
            Toast.makeText(getApplicationContext(), R.string.copied_clipboard, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), R.string.couldnt_copy_clipboard, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean sendFile(List<Uri> uris, Peer peer) {
        PersistableBundle bundle = new PersistableBundle();
        bundle.putString("peer", peer.toString());

        List<AndroidFileData> androidFileData = new ArrayList<>();

        for (Uri uri : uris) {
            Pair<String, Long> fileInfos = getFileInfos(uri);
            String fileName = fileInfos.first;
            Long fileSize = fileInfos.second;
            if (fileName == null) {
                Toast.makeText(this, this.getString(R.string.couldn_get_name_file), Toast.LENGTH_SHORT).show();
                return false;
            }
            if (fileSize == null) {
                Toast.makeText(this, this.getString(R.string.couldnt_get_size_of_file), Toast.LENGTH_SHORT).show();
                return false;
            }
            androidFileData.add(new AndroidFileData(fileName, fileSize, uri));
        }
        Gson gson = new Gson();
        bundle.putStringArray("files",
            androidFileData.stream().map(gson::toJson)
                .toArray(String[]::new)
        );

        JobInfo.Builder jobInfoBuilder = new JobInfo.Builder(0,
                new ComponentName(this, FileSendingJobService.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setExtras(bundle);

        JobScheduler jobScheduler =
                (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        logSend(androidFileData);
        jobScheduler.schedule(jobInfoBuilder.build());
        return true;
    }

    private void logSend(List<AndroidFileData> fileData) {
        for (AndroidFileData file : fileData) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, AnalyticsValues.SERVICE_START);
            bundle.putLong("size", file.getFileSize());
            bundle.putString(FirebaseAnalytics.Param.METHOD, "SEND");
            analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_FILE) {
            if (resultCode == RESULT_OK) {

                final Peer peer = getPeer();
                if (peer == null) {
                    Toast.makeText(this, this.getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (sendFile(Collections.singletonList(data.getData()), peer)) {
                    sendContent(peer);
                }
            } else {
                Toast.makeText(this, this.getString(R.string.couldnt_get_file_short), Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private Peer getPeer() {
        try {
            return Fandem.findAvailableSendingPeer();
        } catch (IOException e) {
            Toast.makeText(this, this.getString(R.string.couldn_get_ip_address), Toast.LENGTH_SHORT).show();
            return null;
        }
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

    private void onSendingEvent(final int stringId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent returnIntent = new Intent();
                returnIntent.putExtra(MainActivity.RETURN_TEXT_KEY, getString(stringId));
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });
    }
    public void onSendingStarted() {
        onSendingEvent(R.string.service_started);
    }

    public void onSendingCanceled() {
        onSendingEvent(R.string.send_service_canceled);
    }
}
