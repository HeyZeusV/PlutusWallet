package com.heyzeusv.plutuswallet.util

import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.heyzeusv.plutuswallet.ui.theme.LocalPWColors
import com.heyzeusv.plutuswallet.ui.theme.alertDialogButton

/**
 *  Composable for a standard AlertDialog with [title] that displays [message].
 *  [onConfirm] runs when confirm button with [onConfirmText] is pressed.
 *  [onDismiss] runs when dismiss button with [onDismissText] is pressed.
 */
@Composable
fun PWAlertDialog(
    onConfirmText: String,
    onConfirm: () -> Unit,
    onDismissText: String,
    onDismiss: () -> Unit,
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = onConfirmText.uppercase(),
                    color = LocalPWColors.current.alertDialogButtonText,
                    style = alertDialogButton
                )
            }
        },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = onDismissText.uppercase(),
                    color = LocalPWColors.current.alertDialogButtonText,
                    style = alertDialogButton
                )
            }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.subtitle1
            )
        },
        text = {
            Text(
                text = message,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
            )
        }
    )
}