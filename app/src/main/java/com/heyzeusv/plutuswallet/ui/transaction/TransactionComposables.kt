package com.heyzeusv.plutuswallet.ui.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.MaterialTheme

@Composable
fun TransactionTextInput(
    initialText: String,
    label: String,
    helperText: String,
    modifier: Modifier = Modifier,
    maxLength: Int = 0
) {
    var text by remember { mutableStateOf(initialText) }

    Column(modifier = modifier.padding(horizontal = 12.dp)) {
        OutlinedTextField(
            value = text,
            onValueChange = { if (it.length <= maxLength) text = it },
            modifier = modifier.fillMaxWidth(),
            label = { Text(label) },
            singleLine = true
        )
        Row(
            modifier = modifier.fillMaxWidth().padding(start = 16.dp, top = 4.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = helperText,
                style = MaterialTheme.typography.caption
            )
            if (maxLength > 0) {
                Text(
                    text = "${text.length}/$maxLength",
                    style = MaterialTheme.typography.caption
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewTransTextBox() {
    TransactionTextInput("Testing", "Title", "Helper Text", maxLength = 10)
}