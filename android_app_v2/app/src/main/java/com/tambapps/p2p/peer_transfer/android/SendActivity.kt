package com.tambapps.p2p.peer_transfer.android

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tambapps.p2p.peer_transfer.android.ui.theme.FandemAndroidTheme
import com.tambapps.p2p.peer_transfer.android.ui.theme.gradientBrush

class SendActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val pickFileLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) {
      Toast.makeText(this, "TODO", Toast.LENGTH_SHORT).show()
    }

    setContent {
      FandemAndroidTheme {
        SendView(pickFileLauncher)
      }
    }
  }
}

@Composable
fun SendView(pickFileLauncher: ActivityResultLauncher<String>) {
  Surface(modifier = Modifier
    .fillMaxSize()
    .background(brush = gradientBrush), color = Color.Transparent) {

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
      Spacer(modifier = Modifier.weight(1f))
      Text(modifier = Modifier.weight(1f), text = stringResource(id = R.string.pick_a_file_to_send), fontSize = 24.sp)
      Spacer(modifier = Modifier.weight(1f))
      FloatingActionButton(modifier = Modifier.size(128.dp),
        shape = RoundedCornerShape(128.dp),
        onClick = { pickFileLauncher.launch("*/*") }) {
        Image(painter = painterResource(id = R.drawable.folder), contentDescription = "folder",
          modifier = Modifier.size(80.dp))
      }
      Spacer(modifier = Modifier.weight(1f))
    }
  }
}

