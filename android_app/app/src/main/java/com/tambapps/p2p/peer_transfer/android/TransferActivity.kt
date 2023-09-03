package com.tambapps.p2p.peer_transfer.android

import android.content.ComponentName
import android.content.Intent
import android.net.wifi.WifiManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import com.tambapps.p2p.peer_transfer.android.ui.theme.FandemAndroidTheme
import com.tambapps.p2p.peer_transfer.android.util.DialogButton
import com.tambapps.p2p.peer_transfer.android.util.NetworkUtils

abstract class TransferActivity: ComponentActivity() {

  private val showNetworkDialogState = mutableStateOf(false)

  override fun onResume() {
    super.onResume()
    showNetworkDialogState.value = !isNetworkConfigured()
  }

  private fun turnOnHotspot() {
    val intent = Intent(Intent.ACTION_MAIN, null)
    intent.addCategory(Intent.CATEGORY_LAUNCHER)
    val cn = ComponentName("com.android.settings", "com.android.settings.TetherSettings")
    intent.component = cn
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    startActivity(intent)
  }

  protected fun isNetworkConfigured(): Boolean {
    val wifiManager = getSystemService(WIFI_SERVICE) as WifiManager
    return NetworkUtils.isHotspotEnabled(this)
        || wifiManager.isWifiEnabled && NetworkUtils.isNetworkConnected(this)
  }

  @Composable
  protected fun TransferActivityTheme(content: @Composable () -> Unit) {
    FandemAndroidTheme {
      if (showNetworkDialogState.value) {
        AlertDialog(onDismissRequest = { showNetworkDialogState.value = false },
          title = {
            Text(text = stringResource(id = R.string.nar))
          },
          text = {
            Text(text = stringResource(id = R.string.nar_description))
          },
          confirmButton = {
            Column(horizontalAlignment = Alignment.End) {
              DialogButton(openDialogState = showNetworkDialogState, text = getString(R.string.connect_to_wifi), onClick = { startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS)) })
              DialogButton(
                openDialogState = showNetworkDialogState,
                text = getString(R.string.start_hotspot),
                onClick = { turnOnHotspot() }
              )
              DialogButton(openDialogState = showNetworkDialogState, text = "ok")
            }
          })
      }
      content.invoke()
    }
  }

}