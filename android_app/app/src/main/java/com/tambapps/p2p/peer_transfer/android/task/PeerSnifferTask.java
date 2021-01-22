package com.tambapps.p2p.peer_transfer.android.task;

import android.os.AsyncTask;

import com.tambapps.p2p.fandem.exception.SniffException;
import com.tambapps.p2p.fandem.sniff.PeerSniffer;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;

public class PeerSnifferTask extends AsyncTask<Void, Void, Void> {

    private final WeakReference<PeerSniffer.SniffListener> listenerReference;
    private final WeakReference<ExecutorService> executorServiceReference;

    public PeerSnifferTask(PeerSniffer.SniffListener listener, ExecutorService executorService) {
        this.listenerReference = new WeakReference<>(listener);
        this.executorServiceReference = new WeakReference<>(executorService);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        PeerSniffer.SniffListener listener = listenerReference.get();
        try {
            PeerSniffer sniffer = new PeerSniffer(listener);
            sniffer.sniffWhileNoneFound(executorServiceReference.get());
        } catch (IOException e) {
            listener.onError(new SniffException(e));
        }
        return null;
    }

}
