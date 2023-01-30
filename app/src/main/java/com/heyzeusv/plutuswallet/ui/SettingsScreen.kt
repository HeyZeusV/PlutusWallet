package com.heyzeusv.plutuswallet.ui

import android.content.SharedPreferences
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.heyzeusv.plutuswallet.ui.transaction.SettingOptions.CURRENCY_SYMBOL
import com.heyzeusv.plutuswallet.ui.transaction.SettingOptions.CURRENCY_SYMBOL_SIDE
import com.heyzeusv.plutuswallet.ui.transaction.SettingOptions.DATE_FORMAT
import com.heyzeusv.plutuswallet.ui.transaction.SettingOptions.DECIMAL_NUMBER
import com.heyzeusv.plutuswallet.ui.transaction.SettingOptions.DECIMAL_SYMBOL
import com.heyzeusv.plutuswallet.ui.transaction.SettingOptions.LANGUAGE
import com.heyzeusv.plutuswallet.ui.transaction.SettingOptions.THEME
import com.heyzeusv.plutuswallet.ui.transaction.SettingOptions.THOUSANDS_SYMBOL
import com.heyzeusv.plutuswallet.util.Key
import com.heyzeusv.plutuswallet.util.PWAlertDialog
import com.heyzeusv.plutuswallet.util.PreferenceHelper.get
import com.heyzeusv.plutuswallet.util.PreferenceHelper.set
import com.heyzeusv.plutuswallet.util.SettingsUtils
import java.lang.NumberFormatException

@Composable
fun SettingsScreen(
    setVM: SettingsViewModel,
    sharedPref: SharedPreferences,
    recreateActivity: () -> Unit
) {
    var decimalSymbolSelectedValue by remember { mutableStateOf("") }
    var thousandsSymbolSelectedValue by remember { mutableStateOf("") }
    var decimalNumberSelectedValue by remember { mutableStateOf("") }
    var openSwitchDialog by remember { mutableStateOf(false) }
    var openDecimalDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            if (openSwitchDialog) {
                PWAlertDialog(
                    onConfirmText = stringResource(R.string.alert_dialog_switch),
                    onConfirm = {
                        val oldDecimal = sharedPref[Key.KEY_DECIMAL_SYMBOL, "period"]
                        val oldThousands = sharedPref[Key.KEY_THOUSANDS_SYMBOL, "comma"]
                        sharedPref[Key.KEY_THOUSANDS_SYMBOL] = oldDecimal
                        sharedPref[Key.KEY_DECIMAL_SYMBOL] = oldThousands
                        setVM.updateDecimalSymbol(SettingsUtils.getSeparatorSymbol(oldDecimal))
                        setVM.updateThousandsSymbol(SettingsUtils.getSeparatorSymbol(oldThousands))
                        openSwitchDialog = false
                    },
                    onDismissText = stringResource(R.string.alert_dialog_cancel),
                    onDismiss = { openSwitchDialog = false },
                    title = stringResource(R.string.alert_dialog_duplicate_symbols),
                    message = stringResource(R.string.alert_dialog_duplicate_symbols_warning)
                )
            }
            if (openDecimalDialog) {
                val current = sharedPref[Key.KEY_DECIMAL_NUMBER, "yes"]
                PWAlertDialog(
                    onConfirmText = stringResource(R.string.alert_dialog_switch),
                    onConfirm = {
                        when (current) {
                            "yes" -> {
                                sharedPref[Key.KEY_DECIMAL_NUMBER] = "no"
                                setVM.updateDecimalNumber("no")
                            }
                            "no" -> {
                                sharedPref[Key.KEY_DECIMAL_NUMBER] = "yes"
                                setVM.updateDecimalNumber("yes")
                            }
                        }
                        openDecimalDialog = false
                    },
                    onDismissText = stringResource(R.string.alert_dialog_cancel),
                    onDismiss = { openDecimalDialog = false },
                    title = stringResource(R.string.alert_dialog_are_you_sure),
                    message = if (current == "yes") {
                        stringResource(R.string.alert_dialog_decimal_place_warning)
                    } else {
                        ""
                    }
                )
            }
            SettingSetup(THEME, sharedPref) {
                sharedPref[THEME.key] = it
                recreateActivity()
            }
            SettingSetup(CURRENCY_SYMBOL, sharedPref) {
                sharedPref[CURRENCY_SYMBOL.key] = it
                setVM.updateCurrencySymbol(SettingsUtils.getCurrencySymbol(it))
            }
            SettingSetup(CURRENCY_SYMBOL_SIDE, sharedPref) {
                sharedPref[CURRENCY_SYMBOL_SIDE.key] = it
                setVM.updateCurrencySymbolSide(it)
             }
            SettingSetup(
                THOUSANDS_SYMBOL,
                optionSelectedDisplay = thousandsSymbolSelectedValue,
                updateOptionSelectedDisplay = { thousandsSymbolSelectedValue = it },
                sharedPref
            ) {
                val decimalSymbol = sharedPref[Key.KEY_DECIMAL_SYMBOL, "period"]
                if (decimalSymbol == it) {
                    openSwitchDialog = true
                } else {
                    sharedPref[THOUSANDS_SYMBOL.key] = it
                    val newThousandsSymbol = SettingsUtils.getSeparatorSymbol(it)
                    setVM.updateThousandsSymbol(newThousandsSymbol)
                    thousandsSymbolSelectedValue = "\"$newThousandsSymbol\""
                }
            }
            SettingSetup(
                DECIMAL_SYMBOL,
                optionSelectedDisplay = decimalSymbolSelectedValue,
                updateOptionSelectedDisplay = { decimalSymbolSelectedValue = it },
                sharedPref
            ) {
                val thousandsSymbol = sharedPref[Key.KEY_THOUSANDS_SYMBOL, "comma"]
                if (thousandsSymbol == it) {
                    openSwitchDialog = true
                } else {
                    sharedPref[DECIMAL_SYMBOL.key] = it
                    val newDecimalSymbol = SettingsUtils.getSeparatorSymbol(it)
                    setVM.updateDecimalSymbol(newDecimalSymbol)
                    decimalSymbolSelectedValue = "\"$newDecimalSymbol\""
                }
            }
            SettingSetup(
                DECIMAL_NUMBER,
                optionSelectedDisplay = decimalNumberSelectedValue,
                updateOptionSelectedDisplay = { decimalNumberSelectedValue = it },
                sharedPref,
            ) { openDecimalDialog = true }
            SettingSetup(DATE_FORMAT, sharedPref) {
                sharedPref[DATE_FORMAT.key] = it
                setVM.updateDateFormatter(try { it.toInt() } catch (e: NumberFormatException) { 0 })
            }
            SettingSetup(LANGUAGE, sharedPref) {
                sharedPref[LANGUAGE.key] = it
                sharedPref[Key.KEY_MANUAL_LANGUAGE] = true
                recreateActivity()
            }
        }
    }
}

@Composable
fun SettingSetup(
    setting: SettingOptions,
    sharedPref: SharedPreferences,
    onConfirm: (String) -> Unit
) {
    val valueArray = stringArrayResource(setting.valueArrayId)
    val displayArray = stringArrayResource(setting.displayArrayId)
    val optionsMap = valueArray.zip(displayArray).toMap()

    val optionSelectedValue = sharedPref[setting.key, valueArray[0]]
    var optionSelectedDisplay by remember {
        mutableStateOf(optionsMap[optionSelectedValue] ?: "")
    }

    Setting(
        setting,
        optionsMap,
        optionSelectedValue,
        optionSelectedDisplay,
        onConfirm = {
            optionSelectedDisplay = optionsMap.getValue(it)
            onConfirm(it)
        }
    )
}

@Composable
fun SettingSetup(
    setting: SettingOptions,
    optionSelectedDisplay: String,
    updateOptionSelectedDisplay: (String) -> Unit,
    sharedPref: SharedPreferences,
    onConfirm: (String) -> Unit
) {
    val valueArray = stringArrayResource(setting.valueArrayId)
    val displayArray = stringArrayResource(setting.displayArrayId)
    val optionsMap = valueArray.zip(displayArray).toMap()

    val optionSelectedValue = sharedPref[setting.key, valueArray[0]]
    updateOptionSelectedDisplay(optionsMap[optionSelectedValue] ?: "")

    Setting(
        setting,
        optionsMap,
        optionSelectedValue,
        optionSelectedDisplay,
        onConfirm = { onConfirm(it) }
    )
}

@Composable
fun Setting(
    setting: SettingOptions,
    optionsMap: Map<String, String>,
    optionSelectedValue:String,
    optionSelectedDisplay: String,
    onConfirm: (String) -> Unit
) {
    var openDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { openDialog = true }
    ) {
        if (openDialog) {
            ListAlertDialog(
                title = stringResource(setting.titleId),
                optionSelectedValue,
                optionsMap,
                onConfirm = {
                    openDialog = false
                    onConfirm(it)
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
                    Column(modifier = Modifier.selectableGroup()) {
                        options.forEach { entry ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .selectable(
                                        selected = (entry.key == selectedOption),
                                        role = Role.RadioButton,
                                        onClick = { onOptionSelected(entry.key) }
                                    ),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (entry.key == selectedOption),
                                    onClick = null // recommended for accessibility by Google
                                )
                                Text(text = entry.value)
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
                    TextButton(onClick = { onConfirm(selectedOption) }) {
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