package com.tambapps.p2p.peer_transfer.android.service;

import android.net.wifi.WifiManager;
import android.util.Log;

import com.tambapps.p2p.fandem.Fandem;
import com.tambapps.p2p.fandem.SenderPeer;
import com.tambapps.p2p.peer_transfer.android.util.NetworkUtils;
import com.tambapps.p2p.speer.datagram.MulticastDatagramPeer;
import com.tambapps.p2p.speer.datagram.service.MulticastReceiverService;

import java.io.IOException;
import java.net.NetworkInterface;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static com.tambapps.p2p.fandem.Fandem.PEER_DISCOVERY_MULTICAST_ADDRESS;
import static com.tambapps.p2p.fandem.Fandem.senderPeersDeserializer;


/**
 * Class that uses a MulticastLock because it is recommended
 */
public class AndroidSenderPeersReceiverService extends MulticastReceiverService<List<SenderPeer>> {

    private static final String LOCK_TAG = "SenderPeersReceiverService";
    private final WifiManager wifiManager;
    public AndroidSenderPeersReceiverService(WifiManager wifiManager, ExecutorService executorService, DiscoveryListener<List<SenderPeer>> listener) {
        super(executorService, PEER_DISCOVERY_MULTICAST_ADDRESS, 50000, senderPeersDeserializer(), listener);
        this.wifiManager = wifiManager;
    }


    @Override
    public void start(MulticastDatagramPeer datagramPeer) throws IOException {
        // this is needed for multicast to work on hotspot (AKA mobile data sharing)
        NetworkInterface wifiNetworkInterface = NetworkUtils.findWifiNetworkInterface();
        if (wifiNetworkInterface != null) {
            datagramPeer.getSocket().setNetworkInterface(wifiNetworkInterface);
        }
        super.start(datagramPeer);
    }

    @Override
    protected void listen(MulticastDatagramPeer datagramPeer) {
        WifiManager.MulticastLock lock = wifiManager.createMulticastLock(LOCK_TAG);
        try {
            lock.acquire();
            lock.setReferenceCounted(true);

            super.listen(datagramPeer);
            lock.release();
        } catch (Exception e) {
            Log.e("AndroidSenderPeersReceiverService", "Multicast receive error", e);
        }
    }

}
