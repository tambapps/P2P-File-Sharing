package com.tambapps.p2p.peer_transfer.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
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
import com.tambapps.p2p.fandem.Fandem
import com.tambapps.p2p.peer_transfer.android.ui.theme.FandemAndroidTheme
import com.tambapps.p2p.peer_transfer.android.ui.theme.gradientBrush
import com.tambapps.p2p.peer_transfer.android.ui.theme.mainTextStyle
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      FandemAndroidTheme {
        MainView()
      }
    }
  }
}

@Composable
@Preview(showBackground = true)
fun MainView() {
  val context = LocalContext.current
  // A surface container using the 'background' color from the theme
  Surface(modifier = Modifier
    .fillMaxSize()
    .background(brush = gradientBrush), color = Color.Transparent) {
    Column {
      Header()
    }
    Box(modifier = Modifier.fillMaxSize()) {
      Column(modifier = Modifier.align(Alignment.Center)) {
        MainButton(textResId = R.string.send_file) {
          context.startActivity(Intent(context, SendActivity::class.java))
        }
        MainButton(textResId = R.string.receive_file) {
        }
      }
    }
  }
}

@Composable
fun Header() {
  Row(horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .padding(start = 4.dp, end = 16.dp)) {
    IconButton(onClick = { /*TODO*/ }) {
      Image(painter = painterResource(id = R.drawable.help), contentDescription = "help")
    }
    Text(
      text = "Fandem " + Fandem.VERSION
    )
  }
}


@Composable
fun MainButton(textResId: Int, onClick: () -> Unit) {
  Box(
    Modifier
      .fillMaxWidth()
      .height(150.dp)
      .clickable(onClick = onClick), contentAlignment = Alignment.Center) {
    Text(text = stringResource(id = textResId).uppercase(Locale.getDefault()), style = mainTextStyle)
  }
}