package com.tambapps.p2p.peer_transfer.android.task;

import android.os.AsyncTask;
import android.util.Log;

import com.tambapps.p2p.fandem.sniff.PeerSniffer;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class PeerSnifferTask extends AsyncTask<Void, Void, Void> {

    private final WeakReference<PeerSniffer.SniffListener> listenerReference;

    public PeerSnifferTask(PeerSniffer.SniffListener listener) {
        this.listenerReference = new WeakReference<>(listener);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        PeerSniffer.SniffListener listener = listenerReference.get();
        try {
            PeerSniffer sniffer = new PeerSniffer(listener);
            sniffer.sniffWhileNoneFound(); // TODO multithread that shit
        } catch (IOException e) {
            listener.onError(e);
        }
        return null;
    }

}
