package com.tambapps.p2p.peer_transfer.android.service.sniff;

import android.os.Build;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.tambapps.p2p.fandem.Peer;
import com.tambapps.p2p.fandem.sniff.PeerSniffHandler;
import com.tambapps.p2p.peer_transfer.android.service.FileSendingJobService;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Future;

public class SniffHandlerService {

    private ServerSocket serverSocket;
    private Future future;

    public void startInBackground(FileSendingJobService jobService,
                                  final String peerHexCode, final String fileName) {
        future = jobService.startSideTask(new Runnable() {
            @Override
            public void run() {
                Peer peer = Peer.fromHexString(peerHexCode);
                String deviceName = Build.MANUFACTURER + " " + Build.MODEL;
                PeerSniffHandler sniffHandler = new PeerSniffHandler(peer, deviceName, fileName);
                try {
                    serverSocket = sniffHandler.newServerSocket();
                    sniffHandler.handleSniff(serverSocket);
                } catch (IOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }
        });
    }

    public void stop() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) { }
            serverSocket = null;
        }
        if (future != null) {
            if (!future.isCancelled()) {
                future.cancel(true);
            }
            future = null;
        }
    }
}
