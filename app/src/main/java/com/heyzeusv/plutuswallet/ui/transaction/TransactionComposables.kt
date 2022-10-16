package com.heyzeusv.plutuswallet.ui.transaction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.AlertDialog
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
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
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.toSize
import com.heyzeusv.plutuswallet.R

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
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 4.dp, end = 16.dp),
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

@Composable
fun TransactionDate(
    tranVM: TransactionViewModel,
    modifier: Modifier = Modifier
) {
    val date by tranVM.date.observeAsState()
    val source = remember { MutableInteractionSource() }

    DisableSelection {
        OutlinedTextField(
            value = date!!,
            onValueChange = { },
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            readOnly = true,
            label = { Text(text = stringResource(id = R.string.transaction_date)) },
            interactionSource = source
        )
    }

    if (source.collectIsPressedAsState().value) tranVM.selectDateOC(tranVM.tranLD.value!!.date)
}

@Composable
fun TransactionDropDownMenu(
    tranVM: TransactionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val accounts by tranVM.accountList.observeAsState()
    var expanded by remember { mutableStateOf(false) }
    // used to make sure DropdownMenuItems are the same size as OutlinedTextField
    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    val source = remember { MutableInteractionSource() }
    val showDialog by tranVM.showDialog.collectAsState()

    Column(modifier = modifier.padding(horizontal = 12.dp)) {
        DisableSelection {
            if (showDialog) {
                expanded = false
                AlertDialogInput(
                    tranVM = tranVM,
                    onDismiss = { tranVM.updateShowDialog(false) }
                )
            }
            OutlinedTextField(
                value = tranVM.account,
                onValueChange = { },
                modifier = modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        textFieldSize = coordinates.size.toSize()
                    },
                readOnly = true,
                label = { Text(stringResource(id = R.string.transaction_account))},
                trailingIcon = {
                    Icon(
                        imageVector = if (expanded) {
                            Icons.Filled.KeyboardArrowUp
                        } else {
                            Icons.Filled.KeyboardArrowDown
                        },
                        contentDescription = "content",
                        modifier = modifier.clickable { expanded = !expanded }
                    )
                },
                interactionSource = source
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = modifier
                    .width(with(LocalDensity.current) { textFieldSize.width.toDp() })
                    .padding(start = 12.dp)
            ) {
                accounts!!.forEach { label ->
                    DropdownMenuItem(
                        onClick = {
                            if (label == context.getString(R.string.account_create)) {
                                tranVM.updateShowDialog(true)
                            } else {
                                tranVM.account = label
                                expanded = false
                            }
                        }
                    ) {
                        Text(text = label)
                    }
                }
            }
        }
    }
    if (source.collectIsPressedAsState().value) expanded = true
}

@Composable
fun AlertDialogInput(
    tranVM: TransactionViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }
    val showDialog by tranVM.showDialog.collectAsState()
    var isError by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = {
                        if (text.isNotBlank()) {
                            isError = false
                            tranVM.insertAccount(text, context.getString(R.string.account_create))
                            tranVM.updateShowDialog(false)
                        } else {
                            isError = true
                        }
                    }
                ) {
                    Text(text = stringResource(id = R.string.alert_dialog_save))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(id = R.string.alert_dialog_cancel))
                }
            },
            modifier = modifier,
            title = { Text(text = stringResource(id = R.string.alert_dialog_create_account)) },
            text = {
                Column() {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.padding(top = 4.dp),
                        label = { Text(text = stringResource(id = R.string.alert_dialog_input_hint)) },
                        isError = isError
                    )
                    if (isError) {
                        Text(
                            text = stringResource(id = R.string.alert_dialog_input_error),
                            style = MaterialTheme.typography.caption
                        )
                    }
                }
            }
        )
    }
}

@Preview
@Composable
fun PreviewTransTextBox() {
    TransactionTextInput("Testing", "Title", "Helper Text", maxLength = 10)
}