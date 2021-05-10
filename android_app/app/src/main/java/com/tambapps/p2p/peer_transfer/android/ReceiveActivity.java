package com.tambapps.p2p.peer_transfer.android;

import android.Manifest;
import android.animation.Animator;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.tambapps.p2p.fandem.Fandem;
import com.tambapps.p2p.fandem.SenderPeer;
import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.peer_transfer.android.analytics.AnalyticsValues;
import com.tambapps.p2p.peer_transfer.android.service.FileReceivingJobService;
import com.tambapps.p2p.peer_transfer.android.task.PeerSnifferTask;
import com.tambapps.p2p.speer.seek.PeerSeeker;
import com.tambapps.p2p.speer.util.PeerUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReceiveActivity extends PermissionActivity implements PeerSeeker.SeekListener<SenderPeer> {

    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 4);
    private ProgressBar progressBar;

    private FirebaseAnalytics analytics;
    private String downloadPath;

    private List<SenderPeer> peers = Collections.synchronizedList(new ArrayList<SenderPeer>());
    private RecyclerView.Adapter recyclerAdapter;
    private AsyncTask currentTask;
    private TextView loadingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
        analytics = FirebaseAnalytics.getInstance(this);

        downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        progressBar = findViewById(R.id.progress_bar);
        loadingText = findViewById(R.id.loading_text);

        initializeRecyclerView();
        initializeRefreshLayout();
        sniffPeersAsync();
        if (!hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requestPermissionDialog(R.string.ask_write_permission_title,
                R.string.ask_write_permission_message, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    private void initializeRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        recyclerAdapter = new MyAdapter();
        recyclerView.setAdapter(recyclerAdapter);
    }

    private void initializeRefreshLayout() {
        final SwipeRefreshLayout refreshLayout = findViewById(R.id.refresh_layout);
        refreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimaryDark));
        refreshLayout.setOnRefreshListener(() -> {
            if (progressBar.getVisibility() == View.VISIBLE) { // if async task running
                return;
            }
            refreshLayout.setRefreshing(false);
            progressBar.setVisibility(View.VISIBLE);
            peers.clear();
            recyclerAdapter.notifyDataSetChanged();
            sniffPeersAsync();
        });
    }

    private void sniffPeersAsync() {
        try {
            currentTask = new PeerSnifferTask(this, executorService,
                    PeerUtils.getIpAddress(), () -> runOnUiThread(() -> runOnUiThread(this::onSniffEnd))).execute();
            loadingText.setText(R.string.loading_sending_peers);
        } catch (IOException e) {
            progressBar.setVisibility(View.INVISIBLE);
            loadingText.setText(R.string.no_internet);
        }
    }

    private void onSniffEnd() {
        progressBar.setVisibility(View.INVISIBLE);
        if (this.peers.isEmpty()) {
            loadingText.setText(R.string.no_sender_found);
        }
    }

    public void startReceiving(Peer peer) {
        // analytics
        Bundle analyticsBundle = new Bundle();
        analyticsBundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "sought peer");
        if (peer instanceof SenderPeer) {
            // TODO add file size to it and
        }
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, analyticsBundle);

        JobScheduler jobScheduler =
                (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);

        PersistableBundle bundle = new PersistableBundle();

        bundle.putString("downloadPath", downloadPath);
        bundle.putString("peer", peer.toString());

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
    }

    private void logReceive() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, AnalyticsValues.SERVICE_START);
        bundle.putString(FirebaseAnalytics.Param.METHOD, "RECEIVE");
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    @Override
    public void onPeersFound(List<SenderPeer> peers) {
        this.peers.addAll(peers);
        runOnUiThread(() -> {
            recyclerAdapter.notifyDataSetChanged();
            if (loadingText.getAlpha() >= 1f) {
                loadingText.animate().alpha(0).setDuration(250)
                        .setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                loadingText.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        })
                        .start();
            }
        });
    }

    @Override
    protected void onStop() {
        if (currentTask != null) {
            currentTask.cancel(true);
        }
        executorService.shutdownNow();
        super.onStop();
    }

    @Override
    public void onException(IOException e) {
        FirebaseCrashlytics.getInstance().recordException(e);
    }

    private String getPeerKeyPrefix() {
        String key;
        try {
            key = Fandem.toHexString(PeerUtils.getIpAddress());
        } catch (IOException e) {
            return null;
        }
        // substract the last octet
        return key.substring(0, key.length() - 2);

    }
    public void receiveManually(View view) {
        final TextInputLayout layout = new TextInputLayout(this);
        final TextInputEditText editText = new TextInputEditText(this);
        layout.addView(editText);
        editText.setHint("Peer key");
        String keyPrefix = getPeerKeyPrefix();
        if (keyPrefix != null) {
            editText.setText(keyPrefix);
        }

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.peer_key_input)
                .setView(layout)
                .setPositiveButton(R.string.start_receiving, null)
                .setNeutralButton(R.string.cancel, null)
                .create();

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (layout.getError() != null && Fandem.isCorrectPeerKey(s.toString())) {
                    layout.setError(null);
                }
            }
        });
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        String hexCode = editText.getText() == null ? "" : editText.getText().toString();
                        layout.setError(Fandem.isCorrectPeerKey(hexCode) ? null : getString(R.string.peer_key_malformed));
                        if (layout.getError() == null) {
                            dialog.dismiss();
                            startReceiving(Fandem.parsePeerFromHexString(hexCode));
                        }
                    }
                });
            }
        });
        dialog.show();
    }

    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v =  LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.peer_element, parent, false);
            return new MyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final SenderPeer discoveredPeer = peers.get(position);
            holder.position = position;
            holder.filenameText.setText(discoveredPeer.getFileName());
            holder.deviceNameText.setText(discoveredPeer.getDeviceName());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(ReceiveActivity.this)
                            .setTitle(getString(R.string.alert_receive_file, discoveredPeer.getFileName()))
                            .setMessage(getString(R.string.alert_receive_file_message, discoveredPeer.getFileName(), discoveredPeer.getDeviceName()))
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startReceiving(discoveredPeer);
                                }
                            })
                            .setNeutralButton(R.string.no, null)
                            .show();
                }
            });
            holder.itemView.setAlpha(0f);
            holder.itemView.animate()
                    .alpha(1F)
                    .setDuration(250)
                    .start();
        }

        @Override
        public int getItemCount() {
            return peers.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {

            private final TextView filenameText;
            private final TextView deviceNameText;
            int position;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                filenameText = itemView.findViewById(R.id.element_peer_filename);
                deviceNameText = itemView.findViewById(R.id.element_peer_devicename);
            }
        }
    }
}
