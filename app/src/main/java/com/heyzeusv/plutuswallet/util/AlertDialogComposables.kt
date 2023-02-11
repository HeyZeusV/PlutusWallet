package com.heyzeusv.plutuswallet.util

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.LocalElevationOverlay
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.DataInterface
import com.heyzeusv.plutuswallet.util.theme.LocalPWColors
import com.heyzeusv.plutuswallet.util.theme.alertDialogButton

/**
 *  Composable for a standard AlertDialog with [title] that displays [message].
 *  [onConfirm] runs when confirm button with [onConfirmText] is pressed.
 *  [onDismiss] runs when dismiss button with [onDismissText] is pressed.
 */
@Composable
fun PWAlertDialog(
    title: String,
    message: String,
    onConfirmText: String,
    onConfirm: () -> Unit,
    onDismissText: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = onConfirmText.uppercase(),
                    modifier = Modifier.testTag("AlertDialog confirm"),
                    color = LocalPWColors.current.alertDialogButtonText,
                    style = alertDialogButton
                )
            }
        },
        modifier = modifier.testTag("AlertDialog"),
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = onDismissText.uppercase(),
                    modifier = Modifier.testTag("AlertDialog dismiss"),
                    color = LocalPWColors.current.alertDialogButtonText,
                    style = alertDialogButton
                )
            }
        },
        title = {
            Text(
                text = title,
                color = MaterialTheme.colors.onSurface,
                style = MaterialTheme.typography.subtitle1
            )
        },
        text = {
            Text(
                text = message,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
            )
        },
        backgroundColor = LocalElevationOverlay.current?.apply(
            color = MaterialTheme.colors.surface,
            elevation = 8.dp
        ) ?: MaterialTheme.colors.surface
    )
}

/**
 *  Composable that displays an AlertDialog with [title] that allows for user input.
 *  [onDismiss] runs when dismiss button is pressed.
 *  [data] is item that will be edited by [onConfirmData] when confirm button is pressed.
 *  [onConfirm] run when confirm button is pressed.
 */
@Composable
fun PWInputAlertDialog(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    data: DataInterface = Account(0, ""),
    onConfirm: (String) -> Unit = { },
    onConfirmData: (DataInterface, String) -> Unit = { _, _ -> }
) {
    var text by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    /**
     *  There is an AlertDialog() composable in the compose library, but I could not
     *  edit the padding. Using Dialog() allows for much more customization.
     */
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.testTag("AlertDialog"),
            shape = MaterialTheme.shapes.medium,
            elevation = dimensionResource(R.dimen.cardElevation)
        ) {
            Column(
                modifier = modifier.padding(
                    top = dimensionResource(R.dimen.id_topPad),
                    bottom = dimensionResource(R.dimen.id_botPad)
                )
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = dimensionResource(R.dimen.id_content_horiPad)
                    )
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.subtitle1
                    )
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = dimensionResource(R.dimen.id_tf_topPad))
                            .testTag("AlertDialog input"),
                        label = { Text(text = stringResource(R.string.alert_dialog_input_hint)) },
                        trailingIcon = {
                            if (isError) {
                                Icon(
                                    imageVector = Icons.Filled.Error,
                                    contentDescription = stringResource(R.string.icon_cd_inputError)
                                )
                            }
                        },
                        isError = isError,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = MaterialTheme.colors.onSurface,
                            focusedBorderColor = MaterialTheme.colors.secondary,
                            focusedLabelColor = MaterialTheme.colors.secondary,
                            unfocusedLabelColor = MaterialTheme.colors.secondary,
                            errorLabelColor = MaterialTheme.colors.error
                        )
                    )
                    if (isError) {
                        Text(
                            text = stringResource(R.string.alert_dialog_input_error),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = dimensionResource(R.dimen.tfh_horiPad))
                                .padding(top = dimensionResource(R.dimen.tfh_topPad))
                                .height(dimensionResource(R.dimen.tfh_height)),
                            color = MaterialTheme.colors.error,
                            style = MaterialTheme.typography.caption
                        )
                    } else {
                        Spacer(
                            modifier = Modifier
                                .padding(top = dimensionResource(R.dimen.tfh_topPad))
                                .height(dimensionResource(R.dimen.tfh_height))
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensionResource(R.dimen.id_button_horiPad))
                        .padding(top = dimensionResource(R.dimen.id_button_topPad)),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = dimensionResource(R.dimen.id_button_spacedBy),
                        alignment = Alignment.End
                    )
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = stringResource(R.string.alert_dialog_cancel).uppercase(),
                            modifier = Modifier.testTag("AlertDialog dismiss"),
                            color = LocalPWColors.current.alertDialogButtonText,
                            style = alertDialogButton
                        )
                    }
                    TextButton(
                        onClick = {
                            if (text.isNotBlank()) {
                                isError = false
                                onConfirm(text)
                                onConfirmData(data, text)
                            } else {
                                isError = true
                            }
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.alert_dialog_save).uppercase(),
                            modifier = Modifier.testTag("AlertDialog confirm"),
                            color = LocalPWColors.current.alertDialogButtonText,
                            style = alertDialogButton
                        )
                    }
                }
            }
        }
    }
}