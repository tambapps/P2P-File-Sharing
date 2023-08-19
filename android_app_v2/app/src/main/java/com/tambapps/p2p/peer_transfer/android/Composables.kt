@file:OptIn(ExperimentalMaterial3Api::class)

package com.tambapps.p2p.peer_transfer.android

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog


@Composable
fun TextInputDialog(
  // returns the error text if any
  onPositiveClick: (String) -> String?,
  dialogDismisser: () -> Unit,
) {
  val errorTextState = remember { mutableStateOf<String?>(null) }
  val textState = remember { mutableStateOf("") }
  Dialog({}) {
    Surface(shape = MaterialTheme.shapes.medium) {
      Column {
        Column(Modifier.padding(24.dp)) {
          Text(
            text = stringResource(id = R.string.peer_key_input),
            style = MaterialTheme.typography.titleMedium,
          )
          Spacer(Modifier.size(16.dp))
          OutlinedTextField(
            value = textState.value.uppercase(),
            onValueChange = { textState.value = it },
            label = { Text(stringResource(R.string.peer_key)) },
            isError = errorTextState.value != null,
            supportingText = if (errorTextState.value != null) {
              {
                Text(
                  modifier = Modifier.fillMaxWidth(),
                  text = errorTextState.value!!,
                  color = MaterialTheme.colorScheme.error
                )
              }
            } else null
          )
        }
        Row(
          Modifier
            .padding(8.dp)
            .fillMaxWidth(),
          Arrangement.spacedBy(8.dp, Alignment.End),
        ) {
          // neutral button
          TextButton(onClick = dialogDismisser) {
            Text(text = stringResource(id = R.string.cancel).uppercase())
          }

          // positive button
          TextButton(onClick = {
            val errorText = onPositiveClick.invoke(textState.value)
            errorTextState.value = errorText
            if (errorText == null) {
              dialogDismisser.invoke()
            }
          }) {
            Text(text = stringResource(id = R.string.start_receiving).uppercase())
          }
        }
      }
    }
  }
}