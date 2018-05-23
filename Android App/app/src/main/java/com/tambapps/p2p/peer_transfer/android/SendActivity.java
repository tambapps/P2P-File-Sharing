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
import android.view.View;
import android.widget.Toast;

import com.tambapps.p2p.file_sharing.IPUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Locale;

import com.tambapps.p2p.peer_transfer.android.service.FileSendingJobService;

public class SendActivity extends AppCompatActivity {

    private static final int SENDING_JOB_ID = 0;
    private final static int PICK_FILE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_FILE) {
            if (resultCode == RESULT_OK) {

                JobScheduler jobScheduler =
                        (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);

                PersistableBundle bundle = new PersistableBundle();
                Pair<String, Long> fileInfos = getFileInfos(data.getData());
                bundle.putString("fileUri", data.getData().toString());
                bundle.putString("fileName", fileInfos.first);
                bundle.putInt("id", SENDING_JOB_ID);

                InetAddress address;
                int port;

                try {
                    address = IPUtils.getIPAddress();
                    if (address == null) {
                        Toast.makeText(this, "Couldn't get ip address. Please verify your internet connection", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    port = IPUtils.getAvalaiblePort(address);
                    bundle.putString("address", address.getHostAddress());
                    bundle.putInt("port", port);
                } catch (IOException e) {
                    Toast.makeText(this, "Error: couldn't get ip address", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (fileInfos.second != null) {
                    bundle.putString("fileSize", String.valueOf(fileInfos.second));
                } else {
                    Toast.makeText(this, "Error: couldn't get size of file", Toast.LENGTH_SHORT).show();
                    return;
                }

                JobInfo.Builder jobInfoBuilder = new JobInfo.Builder(0,
                        new ComponentName(this, FileSendingJobService.class))
                                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                        .setExtras(bundle);

                jobScheduler.schedule(jobInfoBuilder.build());
                Intent returnIntent = new Intent();

                String message = "Service started. " + String.format(Locale.US, "Waiting connection on %s:%d",
                        address.getHostAddress(), port);
                returnIntent.putExtra(MainActivity.RETURN_TEXT_KEY, message);
                setResult(RESULT_OK, returnIntent);
                finish();

            } else {
                Toast.makeText(this, "Couldn't get a file", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private Pair<String, Long> getFileInfos(Uri uri) {
        Cursor cursor = getContentResolver()
                .query(uri, null, null, null, null, null);
        String name = null;
        Long size = null;
        try {
            if (cursor != null && cursor.moveToFirst()) {
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
            return new Pair<>(name, size);
        } finally {
            cursor.close();
        }
    }
}
