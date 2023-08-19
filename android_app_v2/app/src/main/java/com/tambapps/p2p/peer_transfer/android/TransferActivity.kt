package com.tambapps.p2p.peer_transfer.android

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.net.wifi.WifiManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import com.tambapps.p2p.peer_transfer.android.util.NetworkUtils

abstract class TransferActivity: ComponentActivity() {
  private var dialog: AlertDialog? = null

  override fun onResume() {
    super.onResume()
    checkCanShare()
  }

  private fun checkCanShare() {
    if (!isNetworkConfigured()) {
      dialog = AlertDialog.Builder(this)
        .setTitle(getString(R.string.nar))
        .setMessage(getString(R.string.nar_description))
        .setNeutralButton(R.string.cancel) { dialog, which -> finish() }
        .setPositiveButton(
          getString(R.string.connect_to_wifi)
        ) { dialog, which -> startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS)) }
        .setNegativeButton(getString(R.string.start_hotspot)) { dialog, which -> turnOnHotspot() }
        .setNeutralButton("ok", null)
        .setCancelable(false)
        .create()
      dialog!!.show()
    } else if (dialog != null) {
      dialog!!.dismiss()
      dialog = null
    }
  }

  private fun turnOnHotspot() {
    val intent = Intent(Intent.ACTION_MAIN, null)
    intent.addCategory(Intent.CATEGORY_LAUNCHER)
    val cn = ComponentName("com.android.settings", "com.android.settings.TetherSettings")
    intent.component = cn
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    startActivity(intent)
  }

  private fun isNetworkConfigured(): Boolean {
    val wifiManager = getSystemService(WIFI_SERVICE) as WifiManager
    return NetworkUtils.isHotspotEnabled(this) || wifiManager.isWifiEnabled && NetworkUtils.isNetworkConnected(
      this
    )
  }
}