package com.tambapps.p2p.peer_transfer.android;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;

import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.tambapps.p2p.fandem.Peer;
import com.tambapps.p2p.peer_transfer.android.analytics.AnalyticsValues;
import com.tambapps.p2p.peer_transfer.android.service.FileReceivingJobService;

public class ReceiveActivity extends AppCompatActivity {

    private TextView errorText;
    private EditText peerKeyInput;
    private FirebaseAnalytics analytics;

    private String downloadPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
        analytics = FirebaseAnalytics.getInstance(this);

        errorText = findViewById(R.id.peer_key_error);
        peerKeyInput = findViewById(R.id.peer_key);
        downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();

        peerKeyInput.requestFocus();
        peerKeyInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (errorText.getAlpha() >= 1f && Peer.isCorrectPeerKey(s.toString())) {
                    errorText.animate().alpha(0).setDuration(500)
                            .start();
                }
            }
        });
    }

    public void startReceiving(View view) {

        String peerKey = peerKeyInput.getText().toString();
        if (Peer.isCorrectPeerKey(peerKey)) {
            JobScheduler jobScheduler =
                    (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);

            PersistableBundle bundle = new PersistableBundle();

            bundle.putString("downloadPath", downloadPath);
            bundle.putString("peer", peerKey);

            JobInfo.Builder jobInfoBuilder = new JobInfo.Builder(1,
                    new ComponentName(this, FileReceivingJobService.class))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setExtras(bundle);

            logReceive();
            jobScheduler.schedule(jobInfoBuilder.build());
            Intent returnIntent = new Intent();
            returnIntent.putExtra(MainActivity.RETURN_TEXT_KEY, getString(R.string.service_started));
            setResult(RESULT_OK, returnIntent);
            finish();
        } else {
            errorText.animate().alpha(1).setDuration(500)
            .start();
        }
    }

    private void logReceive() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, AnalyticsValues.SERVICE_START);
        bundle.putString(FirebaseAnalytics.Param.METHOD, "RECEIVE");
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

}
