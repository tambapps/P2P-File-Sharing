package com.tambapps.p2p.peer_transfer.android

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.tambapps.p2p.fandem.Fandem
import com.tambapps.p2p.peer_transfer.android.ui.theme.FandemAndroidTheme
import com.tambapps.p2p.peer_transfer.android.ui.theme.gradientBrush
import com.tambapps.p2p.speer.Peer
import com.tambapps.p2p.speer.util.PeerUtils
import java.io.IOException
import java.util.Locale

class ReceiveActivity : ComponentActivity() {
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
            Spacer(modifier = Modifier.weight(1f)) // TODO recyclerview
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
              .fillMaxWidth()
              .height(80.dp)
              .padding(10.dp)) {
              Text(text = stringResource(id = R.string.or), fontSize = 20.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)

              val showDialog = remember { mutableStateOf(false) }
              if (showDialog.value) {
                TextInputDialog(onPositiveClick = { hexCode: String ->
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
}

fun receiveManually(context: Context) {
  val layout = LayoutInflater.from(context).inflate(R.layout.manual_receive_dialog, null)
  val peerKeyLayout = layout.findViewById<TextInputLayout>(R.id.peerkey_layout)
  val peerKeyEditText = layout.findViewById<TextInputEditText>(R.id.peerkey_edittext)
  val keyPrefix = try {
    val k = Fandem.toHexString(PeerUtils.getPrivateNetworkIpAddress())
    // subtract the last octet
    k.substring(0, k.length - 2)
  } catch (e: IOException) { null }

  if (keyPrefix != null) {
    peerKeyEditText.setText(keyPrefix)
  }
  val dialog: AlertDialog = AlertDialog.Builder(context)
    .setTitle(R.string.peer_key_input)
    .setView(layout)
    .setPositiveButton(R.string.start_receiving, null)
    .setNeutralButton(R.string.cancel, null)
    .create()
  peerKeyEditText.addTextChangedListener(object : TextWatcher {
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    override fun afterTextChanged(s: Editable) {
      if (peerKeyLayout.error != null && Fandem.isCorrectPeerKey(s.toString())) {
        peerKeyLayout.error = null
      }
    }
  })
  dialog.setOnShowListener {
    val button: Button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
    button.setOnClickListener {
      val hexCode =
        if (peerKeyEditText.text == null) "" else peerKeyEditText.text.toString()
      if (Fandem.isCorrectPeerKey(hexCode)) {
        startReceiving(Fandem.parsePeerFromHexString(hexCode))
      } else {
        peerKeyLayout.error = context.getString(R.string.peer_key_malformed)
      }
    }
  }
  dialog.show()
}

fun startReceiving(peer: Peer) {
  // TODO
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(
    text = "Hello $name!",
    modifier = modifier
  )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  FandemAndroidTheme {
    Greeting("Android")
  }
}