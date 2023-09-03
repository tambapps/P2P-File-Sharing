package com.tambapps.p2p.peer_transfer.android.util

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.text.style.TextAlign

@Composable
fun DialogButton(
  openDialogState: MutableState<Boolean>,
  text: String,
  onClick: () -> Unit = {},
) {
  DialogButton(dissmisser = { openDialogState.value = false }, text = text, onClick = onClick)
}

@Composable
fun DialogButton(
  dissmisser: () -> Unit,
  text: String,
  onClick: () -> Unit = {},
) {
  TextButton(onClick = {
    onClick.invoke()
    dissmisser.invoke()
  }){
    Text(text.uppercase(), textAlign = TextAlign.End)
  }
}