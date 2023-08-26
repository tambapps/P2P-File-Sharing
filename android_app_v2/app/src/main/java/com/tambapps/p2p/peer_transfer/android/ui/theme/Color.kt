package com.tambapps.p2p.peer_transfer.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Cyan = Color(0xFF43cea2)
val FabColor = Color(0xFF00495F)
val BlueOcean = Color(0xFF185a9d)
val ColorPrimary = Color(0xFF3F51B5)
val NotifSmallIconColor = Color(0xFF185a9d)

val TextColor
  @Composable
  // let the color be loaded dynamically if dynamicColorEnabled
  get() = if (dynamicColorEnabled) Color.Unspecified else MaterialTheme.colorScheme.onPrimary
val IconTintColor
  @Composable
  // let the color be loaded dynamically if dynamicColorEnabled
  get() = MaterialTheme.colorScheme.onPrimary