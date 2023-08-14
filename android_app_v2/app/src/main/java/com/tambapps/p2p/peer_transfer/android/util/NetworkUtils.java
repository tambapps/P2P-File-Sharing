package com.tambapps.p2p.peer_transfer.android.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import java.lang.reflect.Method;
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

    public static int AP_STATE_ENABLING = 12;
    public static int AP_STATE_ENABLED = 13;

    // https://stackoverflow.com/questions/12401108/how-to-check-programmatically-if-hotspot-is-enabled-or-disabled
    public static boolean isHotspotEnabled(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        try {
            Method method = wifiManager.getClass().getDeclaredMethod("getWifiApState");
            method.setAccessible(true);
            int state = (Integer) method.invoke(wifiManager, (Object[]) null);
            return state == AP_STATE_ENABLING || state == AP_STATE_ENABLED;
        } catch (ReflectiveOperationException e) {
            // if there is some reflection issue, let's just assume it's enabled
            // let's not take the risk to tell the user it's not enabled when it could be
            return true;
        }
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi != null && mWifi.isConnected();
    }
}