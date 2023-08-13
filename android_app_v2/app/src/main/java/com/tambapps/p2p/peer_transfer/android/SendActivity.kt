package com.tambapps.p2p.peer_transfer.android

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.tambapps.p2p.peer_transfer.android.ui.theme.FandemAndroidTheme
import com.tambapps.p2p.peer_transfer.android.ui.theme.gradientBrush
import com.tambapps.p2p.peer_transfer.android.util.hasPermission

const val ANY_CONTENT_TYPE = "*/*"

class SendActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      FandemAndroidTheme {
        SendView()
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun SendView() {
  val context = LocalContext.current as Activity
  val pickFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) {
    Toast.makeText(context, "TODO", Toast.LENGTH_SHORT).show()
  }

  // request permission and then pick file
  val requestPermissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { isGranted: Boolean ->
    if (isGranted) {
      pickFileLauncher.launch(ANY_CONTENT_TYPE)
    } else {
      Toast.makeText(context, R.string.permissions_not_granted, Toast.LENGTH_SHORT).show()
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
            // TODO there is a problem with that when permission is denied
            requestPermissionLauncher.launch(POST_NOTIFICATIONS)
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

