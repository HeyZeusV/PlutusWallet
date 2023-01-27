package com.heyzeusv.plutuswallet.ui

import android.content.SharedPreferences
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.ui.theme.LocalPWColors
import com.heyzeusv.plutuswallet.ui.theme.alertDialogButton
import com.heyzeusv.plutuswallet.ui.transaction.SettingOptions
import com.heyzeusv.plutuswallet.util.Key
import com.heyzeusv.plutuswallet.util.PreferenceHelper.get
import com.heyzeusv.plutuswallet.util.PreferenceHelper.set

@Composable
fun SettingsScreen(
    sharedPref: SharedPreferences,
    recreateActivity: () -> Unit
) {
    val scrollState = rememberScrollState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            SettingOptions.values().forEach { setting ->
                when (setting) {
                    SettingOptions.LANGUAGE -> {
                        Setting(
                            setting,
                            sharedPref,
                            onConfirm = {
                                sharedPref[Key.KEY_MANUAL_LANGUAGE] = true
                                recreateActivity()
                            }
                        )
                    }
                    SettingOptions.THEME -> {
                        Setting(setting, sharedPref, onConfirm = { recreateActivity() })
                    }
                    else -> Setting(setting, sharedPref)
                }
            }
        }
    }
}

@Composable
fun Setting(
    setting: SettingOptions,
    sharedPref: SharedPreferences,
    onConfirm: () -> Unit = { }
) {
    val valueArray = stringArrayResource(setting.valueArrayId)
    val displayArray = stringArrayResource(setting.displayArrayId)
    val options = valueArray.zip(displayArray).toMap()

    val optionSelectedValue = sharedPref[setting.key, valueArray[0]]
    var optionSelectedDisplay by remember { mutableStateOf(options[optionSelectedValue] ?: "") }

    var openDialog by remember { mutableStateOf(false) }

    /**
     *  TODO?: Settings ViewModel, updateSetting(key, value) { when(key) -> updateSpecificSetting(value) }
     */
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { openDialog = true }
    ) {
        if (openDialog) {
            ListAlertDialog(
                setting,
                optionSelectedValue,
                options,
                onConfirm = {
                    sharedPref[setting.key] = it
                    optionSelectedDisplay = options.getValue(it)
                    openDialog = false
                    onConfirm()
                },
                onDismiss = { openDialog = false }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(setting.titleId),
                style = MaterialTheme.typography.subtitle1.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = optionSelectedDisplay,
                style = MaterialTheme.typography.subtitle2
            )
        }
    }
}

@Composable
fun ListAlertDialog(
    setting: SettingOptions,
    initialValue: String,
    options: Map<String, String>,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(initialValue) }

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
                        text = stringResource(setting.titleId),
                        style = MaterialTheme.typography.subtitle1
                    )
                    Column(modifier = Modifier.selectableGroup()) {
                        options.forEach { entry ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = (entry.key == selectedOption),
                                        role = Role.RadioButton,
                                        onClick = { onOptionSelected(entry.key) }
                                    )
                                    .padding(horizontal = 16.dp)
                            ) {
                                RadioButton(
                                    selected = (entry.key == selectedOption),
                                    onClick = null // recommended for accessibility by Google
                                )
                                Text(
                                    text = entry.value
                                )
                            }
                        }
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
                        onClick = { onConfirm(selectedOption) }
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