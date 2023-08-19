package com.tambapps.p2p.peer_transfer.android

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tambapps.p2p.fandem.Fandem
import com.tambapps.p2p.fandem.SenderPeer
import com.tambapps.p2p.fandem.util.FileUtils
import com.tambapps.p2p.peer_transfer.android.service.FandemWorkService
import com.tambapps.p2p.peer_transfer.android.ui.theme.FandemAndroidTheme
import com.tambapps.p2p.peer_transfer.android.ui.theme.gradientBrush
import com.tambapps.p2p.speer.Peer
import com.tambapps.p2p.speer.util.PeerUtils
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class ReceiveActivity : TransferActivity() {

  @Inject
  lateinit var fandemWorkService: FandemWorkService
  private val viewModel: ReceiveViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      FandemAndroidTheme {
        // A surface container using the 'background' color from the theme
        Surface(modifier = Modifier
          .fillMaxSize()
          .background(brush = gradientBrush), color = Color.Transparent) {
          Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = stringResource(id = R.string.select_peer), textAlign = TextAlign.Center, fontSize = 22.sp, modifier = Modifier.padding(top = 20.dp, bottom = 10.dp))
            LinearProgressIndicator(modifier = Modifier
              .fillMaxWidth()
              .height(2.dp))
            Box(modifier = Modifier.weight(1f)) {
              val sendingPeersState = viewModel.sendingPeers.observeAsState()
              val sendingPeers = sendingPeersState.value
              if (sendingPeers.isNullOrEmpty()) {
                Text(text = stringResource(id = R.string.loading_sending_peers), modifier = Modifier
                  .align(Alignment.Center)
                  .padding(bottom = 40.dp), fontSize = 22.sp)
              } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                  items(sendingPeers.size) { position ->
                    val peer = sendingPeers[position]
                    Column(modifier = Modifier.padding(start = 10.dp, top = 4.dp, bottom = 4.dp)) {
                      Text(text = peer.deviceName, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
                      Text(text = if (peer.files.size == 1) peer.files.first().fileName else peer.files.joinToString(separator = "\n- ", prefix = "- ", transform = { it.fileName }), fontSize = 16.sp, modifier = Modifier.padding(bottom = 4.dp))
                      Text(text = FileUtils.toFileSize(peer.files.sumOf { it.fileSize }), fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
                    }
                  }
                }
              }
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
              .fillMaxWidth()
              .height(80.dp)
              .padding(10.dp)) {
              Text(text = stringResource(id = R.string.or), fontSize = 20.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)

              val showDialog = remember { mutableStateOf(false) }
              if (showDialog.value) {
                TextInputDialog(
                  initialValue = getPeerKeyPrefix(),
                  onPositiveClick = { hexCode: String ->
                  if (Fandem.isCorrectPeerKey(hexCode)) {
                    startReceiving(Fandem.parsePeerFromHexString(hexCode))
                    return@TextInputDialog null
                  } else {
                    return@TextInputDialog getString(R.string.peer_key_malformed)
                  }

                }) { showDialog.value = false }
              }
              Button(onClick = { showDialog.value = true }, modifier = Modifier
                .weight(3f), shape = RoundedCornerShape(50)) {
                Text(text = stringResource(id = R.string.enter_peer_key_manually).uppercase(Locale.getDefault()), fontSize = 18.sp, textAlign = TextAlign.Center)
              }
            }
          }
        }
      }
    }
  }

  private fun startReceiving(peer: Peer) {
    fandemWorkService.startReceiveFileWork(peer)
    Toast.makeText(applicationContext, getString(R.string.service_started), Toast.LENGTH_LONG).show()
  }

  private fun getPeerKeyPrefix(): String? {
    val key: String = try {
      Fandem.toHexString(PeerUtils.getPrivateNetworkIpAddress())
    } catch (e: IOException) {
      return null
    }
    // subtract the last octet
    return key.substring(0, key.length - 2)
  }
}

@HiltViewModel
class ReceiveViewModel @Inject constructor(): ViewModel() {
  val sendingPeers = MutableLiveData<MutableList<SenderPeer>>(mutableListOf())
}