package com.tambapps.p2p.peer_transfer.android.util;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public final class NetworkUtils {

    public static NetworkInterface findWifiNetworkInterface() {
        Enumeration<NetworkInterface> enumeration = null;

        try {
            enumeration = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            return null;
        }

        NetworkInterface wlan0 = null;

        while (enumeration.hasMoreElements()) {

            wlan0 = enumeration.nextElement();

            if (wlan0.getName().equals("wlan0")) {
                return wlan0;
            }
        }

        return null;
    }
}
