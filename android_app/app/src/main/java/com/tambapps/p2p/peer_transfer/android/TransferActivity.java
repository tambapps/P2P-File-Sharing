package com.tambapps.p2p.peer_transfer.android;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.tambapps.p2p.peer_transfer.android.util.NetworkUtils;

public class TransferActivity extends AppCompatActivity {

    private AlertDialog dialog;

    @Override
    protected void onResume() {
        super.onResume();
        checkCanShare();
    }

    public void checkCanShare() {
        if (!isNetworkConfigured()) {
            dialog = new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.nar))
                    .setMessage(getString(R.string.nar_description))
                    .setNeutralButton(R.string.cancel, (dialog, which) -> finish())
                    .setPositiveButton(getString(R.string.connect_to_wifi),
                            (dialog, which) -> startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS)))
                    .setNegativeButton(getString(R.string.start_hotspot), (dialog, which) -> turnOnHotspot())
                    .setNeutralButton(R.string.cancel, (dialog, which) -> finish())
                    .setCancelable(false)
                    .create();
            dialog.show();
        } else if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    public void turnOnHotspot() {
        final Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        final ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.TetherSettings");
        intent.setComponent(cn);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity( intent);
    }

    public boolean isNetworkConfigured() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        return NetworkUtils.isHotspotEnabled(this) || (wifiManager.isWifiEnabled() && NetworkUtils.isNetworkConnected(this));
    }
}
