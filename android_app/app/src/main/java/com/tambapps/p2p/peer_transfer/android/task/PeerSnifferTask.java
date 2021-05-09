package com.tambapps.p2p.peer_transfer.android.task;

import android.os.AsyncTask;


import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.tambapps.p2p.fandem.Fandem;
import com.tambapps.p2p.fandem.SenderPeer;
import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.speer.seek.PeerSeeker;
import com.tambapps.p2p.speer.seek.strategy.SeekingStrategy;

import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class PeerSnifferTask extends AsyncTask<Void, Void, Void> {

    private final WeakReference<PeerSeeker.SeekListener<SenderPeer>> listenerReference;
    private final WeakReference<ExecutorService> executorServiceReference;
    private final InetAddress address;
    private Runnable onEnd;

    public PeerSnifferTask(PeerSeeker.SeekListener<SenderPeer> listener, ExecutorService executorService, InetAddress address, Runnable onEnd) {
        this.listenerReference = new WeakReference<>(listener);
        this.executorServiceReference = new WeakReference<>(executorService);
        this.address = address;
        this.onEnd = onEnd;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        SeekingStrategy seekingStrategy = Fandem.seekingStrategy(address);
        PeerSeeker<SenderPeer> seeker = Fandem.seeker(listenerReference.get());
        // prevent seeking itself
        seeker.addFilteredPeer(address);
        try {
            List<Future<List<SenderPeer>>> futures = seeker.seek(seekingStrategy, executorServiceReference.get());
            for (Future<?> future :futures) {
                future.get();
            }
            onEnd.run();
        } catch (InterruptedException e) {
            // ignore, task was just probably canceled
        } catch (ExecutionException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            onEnd.run();
        }
        onEnd = null;
        return null;
    }

}
