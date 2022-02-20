package com.tambapps.p2p.peer_transfer.android;

import android.Manifest;
import android.animation.Animator;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.tambapps.p2p.fandem.Fandem;
import com.tambapps.p2p.fandem.SenderPeer;
import com.tambapps.p2p.fandem.model.FileData;
import com.tambapps.p2p.fandem.util.FileUtils;
import com.tambapps.p2p.peer_transfer.android.service.AndroidSenderPeersReceiverService;
import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.peer_transfer.android.analytics.AnalyticsValues;
import com.tambapps.p2p.peer_transfer.android.service.FileReceivingJobService;
import com.tambapps.p2p.speer.datagram.service.MulticastReceiverService;
import com.tambapps.p2p.speer.util.PeerUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ReceiveActivity extends TransferActivity implements MulticastReceiverService.DiscoveryListener<List<SenderPeer>> {

    private static final int PERMISSION_REQUEST_CODE = 8;
    private static final List<Character> ILLEGAL_CHARACTERS = Arrays.asList('/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':');

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private ProgressBar progressBar;

    private FirebaseAnalytics analytics;

    private List<SenderPeer> peers = Collections.synchronizedList(new ArrayList<>());
    private RecyclerView.Adapter recyclerAdapter;
    private MulticastReceiverService<List<SenderPeer>> senderPeersReceiverService;
    private TextView loadingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
        analytics = FirebaseAnalytics.getInstance(this);

        // if Android R or later, download files in app's private directory, since we can't get File
        // instance of external files
        // a screen manage received files will appear in
        progressBar = findViewById(R.id.progress_bar);
        loadingText = findViewById(R.id.loading_text);

        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        senderPeersReceiverService = new AndroidSenderPeersReceiverService(wifiManager, executorService, this);
        initializeRecyclerView();
        initializeRefreshLayout();
        if (isNetworkConfigured()) {
            sniffPeersAsync();
        } else {
            progressBar.setVisibility(View.GONE);
            loadingText.setText(R.string.no_internet);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R && !hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
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
            refreshLayout.setRefreshing(false);
            peers.clear();
            recyclerAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.VISIBLE);
            senderPeersReceiverService.stop();
            sniffPeersAsync();
        });
    }

    private void sniffPeersAsync() {
        try {
            senderPeersReceiverService.start();
            loadingText.setText(R.string.loading_sending_peers);
        } catch (IOException e) {
            progressBar.setVisibility(View.INVISIBLE);
            loadingText.setText(R.string.no_internet);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        senderPeersReceiverService.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isNetworkConfigured() && !senderPeersReceiverService.isRunning()) {
            sniffPeersAsync();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdownNow();
    }

    // optFile for Android before 11
    private void startReceiving(Peer peer) {
        // analytics
        Bundle analyticsBundle = new Bundle();
        analyticsBundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "sought peer");
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, analyticsBundle);

        JobScheduler jobScheduler =
                (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);

        PersistableBundle bundle = new PersistableBundle();

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
    public void onDiscovery(List<SenderPeer> peers) {
        boolean newPeers = false;
        InetAddress ownAddress = PeerUtils.getPrivateNetworkIpAddressOrNull();
        for (SenderPeer peer : peers) {
            if (!this.peers.contains(peer)) {
                newPeers = true;
                this.peers.add(peer);
            }
        }
        if (!newPeers) {
            return;
        }
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
    public void onError(IOException e) {
        Log.e("ReceiveActivity", "Error while sniffing", e);
        runOnUiThread(() -> {
            progressBar.setVisibility(View.INVISIBLE);
            loadingText.setText(R.string.no_internet);
        });
    }

    private String getPeerKeyPrefix() {
        String key;
        try {
            key = Fandem.toHexString(PeerUtils.getPrivateNetworkIpAddress());
        } catch (IOException e) {
            return null;
        }
        // substract the last octet
        return key.substring(0, key.length() - 2);

    }
    public void receiveManually(View view) {
        View layout = LayoutInflater.from(this).inflate(R.layout.manual_receive_dialog, null);
        final TextInputLayout peerKeyLayout = layout.findViewById(R.id.peerkey_layout);
        final TextInputEditText peerKeyEditText = layout.findViewById(R.id.peerkey_edittext);

        String keyPrefix = getPeerKeyPrefix();
        if (keyPrefix != null) {
            peerKeyEditText.setText(keyPrefix);
        }

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.peer_key_input)
                .setView(layout)
                .setPositiveButton(R.string.start_receiving, null)
                .setNeutralButton(R.string.cancel, null)
                .create();

        peerKeyEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (peerKeyLayout.getError() != null && Fandem.isCorrectPeerKey(s.toString())) {
                    peerKeyLayout.setError(null);
                }
            }
        });
        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view1 -> {
                String hexCode = peerKeyEditText.getText() == null ? "" : peerKeyEditText.getText().toString();
                if (Fandem.isCorrectPeerKey(hexCode)) {
                    startReceiving(Fandem.parsePeerFromHexString(hexCode));
                } else {
                    peerKeyLayout.setError(getString(R.string.peer_key_malformed));
                }
            });
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
            String fileNames = discoveredPeer.getFiles().stream().map(FileData::getFileName).collect(Collectors.joining("\n- ", "- ", ""));

            holder.fileNameText.setText(fileNames);
            holder.fileSizeText.setText(FileUtils.toFileSize(discoveredPeer.getFiles().stream().mapToLong(FileData::getFileSize).sum()));
            holder.deviceNameText.setText(discoveredPeer.getDeviceName());
            holder.itemView.setOnClickListener(v -> {
                String message = getString(R.string.alert_receive_file_message,
                    discoveredPeer.getDeviceName(),
                    discoveredPeer.getFiles().stream().map(FileData::getFileName).collect(Collectors.joining("- ", "- ", "")));
                message += "\n" + getString(R.string.in_download_folder);
                new AlertDialog.Builder(ReceiveActivity.this)
                        .setTitle(getString(R.string.alert_receive_file, fileNames))
                        .setMessage(message)
                        .setPositiveButton(R.string.yes, (dialog, which) -> {
                            // receive in Download folder
                            startReceiving(discoveredPeer);
                        })
                        .setNeutralButton(R.string.no, null)
                        .show();
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

            private final TextView fileNameText;
            private final TextView fileSizeText;
            private final TextView deviceNameText;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                fileNameText = itemView.findViewById(R.id.element_peer_filename);
                fileSizeText = itemView.findViewById(R.id.element_peer_filesize);
                deviceNameText = itemView.findViewById(R.id.element_peer_devicename);
            }
        }
    }

    public boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_GRANTED;
    }
    public void requestWritePermission(String permission) {
        ActivityCompat.requestPermissions(this,
                new String[]{permission},
                PERMISSION_REQUEST_CODE);
    }

    public void requestPermissionDialog(int title, int message, final String permission) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton(this.getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), getString(R.string.permissions_not_granted), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestWritePermission(permission);
                    }
                })
                .create()
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), this.getString(R.string.permissions_not_granted), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    private boolean isValidFileName(String s) {
        if (s.isEmpty()) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (ILLEGAL_CHARACTERS.contains(c)) {
                return false;
            }
        }
        return true;
    }
}
