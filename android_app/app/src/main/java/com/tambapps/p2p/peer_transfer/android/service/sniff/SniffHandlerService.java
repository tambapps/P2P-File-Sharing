package com.tambapps.p2p.peer_transfer.android.service.sniff;

import android.os.Build;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.tambapps.p2p.fandem.Fandem;
import com.tambapps.p2p.fandem.SenderPeer;
import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.peer_transfer.android.service.FileSendingJobService;
import com.tambapps.p2p.speer.ServerPeer;
import com.tambapps.p2p.speer.greet.PeerGreeter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.concurrent.Future;

public class SniffHandlerService {

    private Future<?> future;

    public void startInBackground(FileSendingJobService jobService,
                                  final String peerString, final String fileName) {
        PeerGreeter<SenderPeer> greeter = new PeerGreeter<>(Fandem.greetings());
        future = jobService.startSideTask(() -> {
            Peer peer = Peer.parse(peerString);
            String deviceName = Build.MANUFACTURER + " " + Build.MODEL;
            greeter.addAvailablePeer(new SenderPeer(peer.getIp(), peer.getPort(), deviceName, fileName));


            try (ServerPeer server = new ServerPeer(peer)) {
                greeter.greet(server);
            } catch (SocketException e) {
                // probably just a cancel
            } catch (IOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        });
    }

    public void stop() {
        future.cancel(true);
    }
}
