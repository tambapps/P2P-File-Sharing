package com.tambapps.p2p.peer_transfer.android

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.view.WindowCompat
import com.tambapps.p2p.fandem.Fandem
import com.tambapps.p2p.peer_transfer.android.ui.theme.FandemAndroidTheme
import com.tambapps.p2p.peer_transfer.android.ui.theme.FandemSurface
import com.tambapps.p2p.peer_transfer.android.ui.theme.IconTintColor
import com.tambapps.p2p.peer_transfer.android.ui.theme.TextColor
import com.tambapps.p2p.peer_transfer.android.ui.theme.mainTextStyle
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  companion object {
    const val FIRST_PREFERENCE_TIME_KEY = "first_time"
  }

  @Inject
  lateinit var sharedPreferences: SharedPreferences

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // Starting with recent Android versions, Compose expects developers to explicitly handle system insets to ensure proper spacing.
    WindowCompat.setDecorFitsSystemWindows(window, false) // Disable default insets handling
    if (!sharedPreferences.contains(FIRST_PREFERENCE_TIME_KEY)) {
      startActivity(Intent(this, OnBoardingActivity::class.java))
      sharedPreferences.edit(commit = true) {
        putBoolean(FIRST_PREFERENCE_TIME_KEY, true)
      }
    }
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
  FandemSurface {
    Column {
      Header()
    }
    Box(modifier = Modifier.fillMaxSize()) {
      Column(modifier = Modifier.align(Alignment.Center)) {
        MainButton(textResId = R.string.send_file) {
          context.startActivity(Intent(context, SendActivity::class.java))
        }
        MainButton(textResId = R.string.receive_file) {
          context.startActivity(Intent(context, ReceiveActivity::class.java))
        }
      }
    }
  }
}

@Composable
fun Header() {
  val context = LocalContext.current
  Row(horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .padding(start = 4.dp, end = 16.dp)) {
    IconButton(onClick = { context.startActivity(Intent(context, HelpActivity::class.java)) }, colors = IconButtonDefaults.iconButtonColors(contentColor = IconTintColor)) {
      Image(painter = painterResource(id = R.drawable.help), contentDescription = "help", colorFilter = ColorFilter.tint(IconTintColor))
    }
    Text(
      text = "Fandem " + Fandem.VERSION,
      color = TextColor
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
    Text(text = stringResource(id = textResId).uppercase(Locale.getDefault()), style = mainTextStyle, color = TextColor)
  }
}