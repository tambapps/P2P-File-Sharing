package com.tambapps.p2p.peer_transfer.android

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tambapps.p2p.fandem.Fandem
import com.tambapps.p2p.peer_transfer.android.service.FandemService
import com.tambapps.p2p.peer_transfer.android.ui.theme.FandemAndroidTheme
import com.tambapps.p2p.peer_transfer.android.ui.theme.gradientBrush
import com.tambapps.p2p.peer_transfer.android.util.hasPermission
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject


const val ANY_CONTENT_TYPE = "*/*"

@AndroidEntryPoint
class SendActivity : ComponentActivity() {

  @Inject lateinit var fandemService: FandemService

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      FandemAndroidTheme {
        SendView(fandemService)
      }
    }
  }
}

@Composable
fun SendView(fandemService: FandemService) {
  val context = LocalContext.current as Activity
  val pickFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
    if (uris.isEmpty()) {
      Toast.makeText(context, "No file was selected", Toast.LENGTH_SHORT).show()
      return@rememberLauncherForActivityResult
    }
    CoroutineScope(Dispatchers.IO).launch {
      val peer = try {
        Fandem.findAvailableSendingPeer()
      } catch (e: IOException) {
        withContext(Dispatchers.Main) { Toast.makeText(context, context.getString(R.string.couldn_get_ip_address), Toast.LENGTH_SHORT).show() }
        null
      } ?: return@launch
      fandemService.sendFiles(context.contentResolver, peer, uris)
    }
  }

  Surface(modifier = Modifier
    .fillMaxSize()
    .background(brush = gradientBrush), color = Color.Transparent) {

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
      Spacer(modifier = Modifier.weight(1f))
      Text(modifier = Modifier.weight(1f), text = stringResource(id = R.string.pick_a_file_to_send), fontSize = 24.sp)
      Spacer(modifier = Modifier.weight(1f))
      FloatingActionButton(modifier = Modifier.size(128.dp),
        shape = RoundedCornerShape(128.dp),
        onClick = {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            && !context.hasPermission(permission = POST_NOTIFICATIONS)) {
            AlertDialog.Builder(context)
              .setTitle("Notifications are not enabled")
              .setMessage("Without notifications you won't be able to follow the progress of the transfer")
              .setPositiveButton("enable") { dialogInterface: DialogInterface, i: Int ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", context.packageName, null)
                intent.data = uri
                context.startActivity(intent)
              }
              .setNeutralButton("cancel", null)
              .setNegativeButton("continue") { dialogInterface: DialogInterface, i: Int ->
                pickFileLauncher.launch(ANY_CONTENT_TYPE)
              }
              .show()
          } else {
            pickFileLauncher.launch(ANY_CONTENT_TYPE)
          }
        }) {
        Image(painter = painterResource(id = R.drawable.folder), contentDescription = "folder",
          modifier = Modifier.size(80.dp))
      }
      Spacer(modifier = Modifier.weight(1f))
    }
  }
}

