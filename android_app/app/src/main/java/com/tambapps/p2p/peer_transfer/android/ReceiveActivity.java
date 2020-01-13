package com.tambapps.p2p.peer_transfer.android;

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
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.tambapps.p2p.fandem.Peer;
import com.tambapps.p2p.fandem.sniff.PeerSniffer;
import com.tambapps.p2p.fandem.sniff.SniffPeer;
import com.tambapps.p2p.peer_transfer.android.analytics.AnalyticsValues;
import com.tambapps.p2p.peer_transfer.android.service.FileReceivingJobService;
import com.tambapps.p2p.peer_transfer.android.task.PeerSnifferTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReceiveActivity extends AppCompatActivity implements PeerSniffer.SniffListener {

    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private ProgressBar progressBar;

    private FirebaseAnalytics analytics;
    private String downloadPath;

    private List<SniffPeer> peers = new ArrayList<>();
    private RecyclerView.Adapter recyclerAdapter;
    private AsyncTask currentTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
        analytics = FirebaseAnalytics.getInstance(this);

        downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        progressBar = findViewById(R.id.progress_bar);

        initializeRecyclerView();
        initializeRefreshLayout();
        sniffPeersAsync();
        /*
        errorText = findViewById(R.id.peer_key_error);
        peerKeyInput = findViewById(R.id.peer_key);
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

         */
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
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (progressBar.getVisibility() == View.VISIBLE) { // if async task running
                    return;
                }
                refreshLayout.setRefreshing(false);
                progressBar.setVisibility(View.VISIBLE);
                peers.clear();
                recyclerAdapter.notifyDataSetChanged();
                sniffPeersAsync();
            }
        });
    }

    private void sniffPeersAsync() {
        currentTask = new PeerSnifferTask(this, executorService).execute();
    }

    public void startReceiving(Peer peer) {
        JobScheduler jobScheduler =
                (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);

        PersistableBundle bundle = new PersistableBundle();

        bundle.putString("downloadPath", downloadPath);
        bundle.putString("peer", peer.toHexString());

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
    public void onPeerFound(SniffPeer peer) {
        Log.d("WIFI", "peer found!!!");
        peers.add(peer);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                recyclerAdapter.notifyDataSetChanged();
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
    public void onError(Exception e) {
        Log.d("WIFI", e.getClass().getSimpleName() + " : " + e.getMessage());
        //TODO
    }

    @Override
    public void onEnd() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
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
            final SniffPeer discoveredPeer = peers.get(position);
            final Peer peer = discoveredPeer.getPeer();
            holder.position = position;
            holder.filenameText.setText(discoveredPeer.getFileName());
            holder.deviceNameText.setText(discoveredPeer.getDeviceName());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(ReceiveActivity.this)
                            .setTitle(getString(R.string.alert_receive_file, discoveredPeer.getFileName()))
                            .setMessage(getString(R.string.alert_receive_file_message, discoveredPeer.getFileName(), peer.toHexString()))
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startReceiving(peer);
                                }
                            })
                            .setNeutralButton(R.string.no, null)
                            .show();
                }
            });
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
