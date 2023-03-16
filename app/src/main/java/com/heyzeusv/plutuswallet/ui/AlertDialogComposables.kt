package com.heyzeusv.plutuswallet.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.RadioButton
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
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.ListItemInterface
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
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .defaultMinSize(minHeight = Dp(integerResource(R.integer.ad_minSize).toFloat()))
                .testTag(stringResource(R.string.tt_ad)),
            shape = MaterialTheme.shapes.medium,
            elevation = dimensionResource(R.dimen.cardElevation)
        ) {
            Column(
                modifier = modifier
                    .padding(
                        top = dimensionResource(R.dimen.ad_topPad),
                        bottom = dimensionResource(R.dimen.ad_botPad)
                    ),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = dimensionResource(R.dimen.ad_content_horiPad)
                    )
                ) {
                    Text(
                        text = title,
                        color = MaterialTheme.colors.onSurface,
                        style = MaterialTheme.typography.subtitle1
                    )
                    Text(
                        text = message,
                        modifier = Modifier.padding(top = dimensionResource(R.dimen.ad_tf_topPad)),
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.body2
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensionResource(R.dimen.ad_button_horiPad))
                        .padding(top = dimensionResource(R.dimen.ad_button_topPad)),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = dimensionResource(R.dimen.ad_button_spacedBy),
                        alignment = Alignment.End
                    )
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = onDismissText.uppercase(),
                            modifier = Modifier.testTag(stringResource(R.string.tt_ad_dismiss)),
                            color = LocalPWColors.current.alertDialogButtonText,
                            style = alertDialogButton
                        )
                    }
                    TextButton(onClick = onConfirm) {
                        Text(
                            text = onConfirmText.uppercase(),
                            modifier = Modifier.testTag(stringResource(R.string.tt_ad_confirm)),
                            color = LocalPWColors.current.alertDialogButtonText,
                            style = alertDialogButton
                        )
                    }
                }
            }
        }
    }
}

/**
 *  Composable that displays an AlertDialog with [title] that allows for user input.
 *  [onDismiss] runs when dismiss button is pressed.
 *  [data] is item that will be edited by [onConfirmData] when confirm button is pressed.
 *  [onConfirm] runs when confirm button is pressed.
 */
@Composable
fun PWInputAlertDialog(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    data: ListItemInterface = Account(0, ""),
    onConfirm: (String) -> Unit = { },
    onConfirmData: (ListItemInterface, String) -> Unit = { _, _ -> }
) {
    var text by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    /**
     *  There is an AlertDialog() composable in the compose library, but I could not
     *  edit the padding. Using Dialog() allows for much more customization.
     */
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .defaultMinSize(minHeight = Dp(integerResource(R.integer.ad_minSize).toFloat()))
                .testTag(stringResource(R.string.tt_ad)),
            shape = MaterialTheme.shapes.medium,
            elevation = dimensionResource(R.dimen.cardElevation)
        ) {
            Column(
                modifier = modifier.padding(
                    top = dimensionResource(R.dimen.ad_topPad),
                    bottom = dimensionResource(R.dimen.ad_botPad)
                ),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = dimensionResource(R.dimen.ad_content_horiPad)
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
                            .padding(top = dimensionResource(R.dimen.ad_tf_topPad))
                            .testTag(stringResource(R.string.tt_ad_input)),
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
                        .padding(horizontal = dimensionResource(R.dimen.ad_button_horiPad))
                        .padding(top = dimensionResource(R.dimen.ad_button_topPad)),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = dimensionResource(R.dimen.ad_button_spacedBy),
                        alignment = Alignment.End
                    )
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = stringResource(R.string.alert_dialog_cancel).uppercase(),
                            modifier = Modifier.testTag(stringResource(R.string.tt_ad_dismiss)),
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
                            modifier = Modifier.testTag(stringResource(R.string.tt_ad_confirm)),
                            color = LocalPWColors.current.alertDialogButtonText,
                            style = alertDialogButton
                        )
                    }
                }
            }
        }
    }
}

/**
 *  Composable that displays Alert Dialog with [title] that gives user a chance to select one
 *  option from [options]. [initialValue] is the value that is pre-selected when Alert Dialog
 *  appears. [onConfirm] runs when confirm button is pressed. [onDismiss] runs when dismiss button
 *  is pressed.
 */
@Composable
fun PWListAlertDialog(
    title: String,
    initialValue: String,
    options: Map<String, String>,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(initialValue) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .defaultMinSize(minHeight = Dp(integerResource(R.integer.ad_minSize).toFloat()))
                .testTag(stringResource(R.string.tt_ad)),
            shape = MaterialTheme.shapes.medium,
            elevation = dimensionResource(R.dimen.cardElevation)
        ) {
            Column(
                modifier = modifier.padding(
                    top = dimensionResource(R.dimen.ad_topPad),
                    bottom = dimensionResource(R.dimen.ad_botPad)
                ),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = dimensionResource(R.dimen.ad_content_horiPad)
                    )
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.subtitle1
                    )
                    Column(
                        modifier = Modifier
                            .padding(top = dimensionResource(R.dimen.ad_tf_topPad))
                            .selectableGroup()
                    ) {
                        options.forEach { entry ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .selectable(
                                        selected = (entry.key == selectedOption),
                                        role = Role.RadioButton,
                                        onClick = { onOptionSelected(entry.key) }
                                    )
                                    .testTag(entry.value),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (entry.key == selectedOption),
                                    onClick = null // recommended for accessibility by Google
                                )
                                Text(
                                    text = entry.value,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f),
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensionResource(R.dimen.ad_button_horiPad))
                        .padding(top = dimensionResource(R.dimen.ad_button_topPad)),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = dimensionResource(R.dimen.ad_button_spacedBy),
                        alignment = Alignment.End
                    )
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = stringResource(R.string.alert_dialog_cancel).uppercase(),
                            modifier = Modifier.testTag(stringResource(R.string.tt_ad_dismiss)),
                            color = LocalPWColors.current.alertDialogButtonText,
                            style = alertDialogButton
                        )
                    }
                    TextButton(onClick = { onConfirm(selectedOption) }) {
                        Text(
                            text = stringResource(R.string.alert_dialog_save).uppercase(),
                            modifier = Modifier.testTag(stringResource(R.string.tt_ad_confirm)),
                            color = LocalPWColors.current.alertDialogButtonText,
                            style = alertDialogButton
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PWAlertDialogPreview() {
    PreviewHelper {
        PWAlertDialog(
            title = "Alert Dialog Preview",
            message = "Preview for PWAlertDialog",
            onConfirmText = "Yes",
            onConfirm = { },
            onDismissText = "No",
            onDismiss = { }
        )
    }
}

@Preview
@Composable
fun PWInputAlertDialogPreview() {
    PreviewHelper {
        PWInputAlertDialog(
            title = "Input Alert Dialog Preview",
            onDismiss = { }
        )
    }
}

@Preview
@Composable
fun PWListAlertDialogPreview() {
    PreviewHelper {
        PWListAlertDialog(
            title = "List Alert Dialog Preview",
            initialValue = "comma",
            options = mapOf("period" to ".", "comma" to ","),
            onConfirm = { },
            onDismiss = { }
        )
    }
}