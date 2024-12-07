package com.tambapps.p2p.peer_transfer.android

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.tambapps.p2p.peer_transfer.android.ui.theme.FandemAndroidTheme
import com.tambapps.p2p.peer_transfer.android.ui.theme.FandemSurface
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

data class PageData(val titleRes: Int, val messageRes: Int, val imageRes: Int)
private val pages = listOf(
  PageData(R.string.welcome_to_fandem, R.string.welcome_des, R.drawable.appicon),
  PageData(R.string.p2p, R.string.p2p_des, R.drawable.transfer),
  PageData(R.string.same_wifi, R.string.same_wifi_des, R.drawable.wifi),
  PageData(R.string.hotspot, R.string.hotspot_des, R.drawable.hotspot),
  PageData(R.string.transfer_followup, R.string.transfer_followup_des, R.drawable.notification),
  PageData(R.string.lets_get_started, R.string.lets_get_started_des, R.drawable.appicon),
)
private val notificationPageIndex = pages.indexOfFirst { it.imageRes == R.drawable.notification }

@AndroidEntryPoint
class OnBoardingActivity : ComponentActivity() {

  companion object {
    const val FIRST_PREFERENCE_TIME_KEY = "first_time"
  }

  @Inject
  lateinit var sharedPreferences: SharedPreferences

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (!sharedPreferences.contains(FIRST_PREFERENCE_TIME_KEY)) {
      startActivity(Intent(this, OnBoardingActivity::class.java))
      sharedPreferences.edit(commit = true) {
        putBoolean(FIRST_PREFERENCE_TIME_KEY, true)
      }
    }

    val pushNotificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
      Toast.makeText(this, "Notification permission " + (if (granted) "granted" else "not granted"), Toast.LENGTH_SHORT).show()
    }
    setContent {
      FandemAndroidTheme {
        OnBoardingScreen(this@OnBoardingActivity, pushNotificationPermissionLauncher)
      }
    }
  }
}

@Composable
fun OnBoardingScreen(
  activity: Activity,
  pushNotificationPermissionLauncher: ActivityResultLauncher<String>
) {
  val currentPage = remember { mutableIntStateOf(0) }


  FandemSurface {
    Column(modifier = Modifier.fillMaxSize()) {
      Page(pages[currentPage.intValue])
      HorizontalDivider(color = Color.White, thickness = 2.dp)
      TabSelector(activity, pages, currentPage.intValue) {
        currentPage.intValue = it
        // if we just want to the next page after notifications
        if (it - 1 == notificationPageIndex && needsNotificationPermission(activity)) {
          pushNotificationPermissionLauncher.launch(POST_NOTIFICATIONS)
        }
      }
    }
  }
}

private fun needsNotificationPermission(context: Context): Boolean =
  Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
          && ContextCompat.checkSelfPermission(context, POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
@Composable
fun ColumnScope.Page(page: PageData) {
  Column(modifier = Modifier
    .weight(85f)
    .fillMaxWidth()
    .padding(16.dp), verticalArrangement = Arrangement.Bottom
  ) {
    Image(modifier = Modifier
      .size(100.dp)
      .align(Alignment.CenterHorizontally)
      .padding(bottom = 32.dp),

      painter = painterResource(page.imageRes), contentDescription = "image")
    Text(modifier = Modifier.padding(bottom = 32.dp),
      text = stringResource(page.titleRes), fontSize = 26.sp, fontWeight = FontWeight.Bold)

    Text(modifier = Modifier.padding(bottom = 32.dp),
      text = stringResource(page.messageRes), fontSize = 18.sp)
  }
}
@Composable
fun ColumnScope.TabSelector(activity: Activity, onboardPages: List<PageData>, currentPage: Int, onTabSelected: (Int) -> Unit) {
  Row(modifier = Modifier.weight(15f), verticalAlignment = Alignment.CenterVertically) {
    Box(Modifier.weight(1f))
    Row(verticalAlignment = Alignment.CenterVertically) {
      onboardPages.forEachIndexed { index, _ ->

        Box(
          modifier = Modifier
            .padding(8.dp)
            .size(if (currentPage == index) 14.dp else 6.dp)
            .clip(CircleShape) // Clip to circle
            .background(color = MaterialTheme.colorScheme.onPrimary)
            .clickable(
              onClick = { onTabSelected(index) },
              indication = ripple(bounded = false, radius = 30.dp),
              interactionSource = androidx.compose.foundation.interaction.MutableInteractionSource()
            )
        )
      }
    }
    Box(Modifier.weight(1f)) {
      val isLastPage = currentPage >= pages.lastIndex
      TextButton(onClick = {
        if (isLastPage) activity.finish() else onTabSelected(currentPage + 1)
      }, modifier = Modifier.align(Alignment.CenterEnd)) {
        R.string.onboarding_end
        Text(stringResource(
          if (!isLastPage) R.string.next else R.string.onboarding_end
        ).uppercase(), fontWeight = FontWeight.Bold, fontSize = 16.sp)
      }

    }
  }
}


