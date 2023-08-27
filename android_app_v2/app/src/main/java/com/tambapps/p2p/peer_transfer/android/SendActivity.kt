package com.tambapps.p2p.peer_transfer.android

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tambapps.p2p.fandem.Fandem
import com.tambapps.p2p.peer_transfer.android.service.FandemWorkService
import com.tambapps.p2p.peer_transfer.android.ui.theme.FabColor
import com.tambapps.p2p.peer_transfer.android.ui.theme.FandemSurface
import com.tambapps.p2p.peer_transfer.android.ui.theme.TextColor
import com.tambapps.p2p.peer_transfer.android.util.DialogButton
import com.tambapps.p2p.peer_transfer.android.util.hasPermission
import com.tambapps.p2p.speer.Peer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

const val ANY_CONTENT_TYPE = "*/*"

@AndroidEntryPoint
class SendActivity : TransferActivity() {

  @Inject lateinit var fandemWorkService: FandemWorkService

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      TransferActivityTheme {
        SendView(fandemWorkService)
      }
    }
  }
}

class SendViewModel: ViewModel() {
  val peer = MutableLiveData<Peer?>()
}
@Composable
fun SendView(fandemWorkService: FandemWorkService, viewModel: SendViewModel = viewModel()) {
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
      viewModel.peer.postValue(peer)
      fandemWorkService.startSendFileWork(context.contentResolver, peer, uris)
    }
  }
  val peerState = viewModel.peer.observeAsState()
  val peer = peerState.value

  FandemSurface {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
      Spacer(modifier = Modifier.weight(1f))
      if (peer != null) {
        val clipboardManager = LocalClipboardManager.current
        val peerKey = Fandem.toHexString(peerState.value)
        Text(text = stringResource(R.string.transfer_started_consult_notif, peerKey), fontSize = 24.sp,
          textAlign = TextAlign.Center,
          color = TextColor,
          modifier = Modifier
            .weight(1f)
            .padding(horizontal = 8.dp)
            .pointerInput(Unit) {
              detectTapGestures(
                onLongPress = {
                  clipboardManager.setText(AnnotatedString(peerKey))
                }
              )
            })
      } else {
        Text(modifier = Modifier
          .weight(1f)
          .padding(horizontal = 8.dp),
          text = stringResource(id = R.string.pick_a_file_to_send),
          fontSize = 24.sp,
          color = TextColor
        )
      }
      Spacer(modifier = Modifier.weight(1f))
      if (peer == null) {
        val showDialogState = remember { mutableStateOf(false) }
        if (showDialogState.value) {
          NotificationDialog(showDialogState, context, pickFileLauncher)
        }
        FloatingActionButton(modifier = Modifier.size(128.dp),
          shape = RoundedCornerShape(128.dp),
          containerColor = FabColor,
          onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
              && !context.hasPermission(permission = POST_NOTIFICATIONS)) {
              showDialogState.value = true
            } else {
              pickFileLauncher.launch(ANY_CONTENT_TYPE)
            }
          }) {
          Image(painter = painterResource(id = R.drawable.folder), contentDescription = "folder",
            modifier = Modifier.size(80.dp))
        }
      }
      Spacer(modifier = Modifier.weight(1f))
    }
  }
}

@Composable
private fun NotificationDialog(
  showDialogState: MutableState<Boolean>,
  context: Activity,
  pickFileLauncher: ManagedActivityResultLauncher<String, List<@JvmSuppressWildcards Uri>>
) {
  AlertDialog(
    title = {
      Text(text = stringResource(id = R.string.notifications_not_enabled))
    },
    text = {
      Text(text = stringResource(id = R.string.no_progress_without_notifs))
    },
    onDismissRequest = { showDialogState.value = false },
    confirmButton = {
      Row(horizontalArrangement = Arrangement.SpaceAround) {
        DialogButton(openDialogState = showDialogState, text = stringResource(id = R.string.continuer),
          onClick = { pickFileLauncher.launch(ANY_CONTENT_TYPE) })
        DialogButton(openDialogState = showDialogState, text = stringResource(id = R.string.cancel))
        DialogButton(openDialogState = showDialogState, text = stringResource(id = R.string.enable),
          onClick = {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", context.packageName, null)
            intent.data = uri
            context.startActivity(intent)
          })
      }
    })
}

