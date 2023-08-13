package com.tambapps.p2p.peer_transfer.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tambapps.p2p.fandem.Fandem
import com.tambapps.p2p.peer_transfer.android.ui.theme.BlueOcean
import com.tambapps.p2p.peer_transfer.android.ui.theme.Cyan
import com.tambapps.p2p.peer_transfer.android.ui.theme.FandemAndroidTheme

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
  // A surface container using the 'background' color from the theme
  Surface(modifier = Modifier
    .fillMaxSize()
    .background(
      brush = Brush.verticalGradient(
        listOf(
          BlueOcean,
          Cyan
        )
      )
    ), color = Color.Transparent) {
    Column {
      Row(horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
          .padding(horizontal = 8.dp, vertical = 8.dp)) {
        IconButton(onClick = { /*TODO*/ }) {
          Image(painter = painterResource(id = R.drawable.help), contentDescription = "help")
        }
        Text(
          text = "Fandem " + Fandem.VERSION
        )
      }
    }
  }
}

