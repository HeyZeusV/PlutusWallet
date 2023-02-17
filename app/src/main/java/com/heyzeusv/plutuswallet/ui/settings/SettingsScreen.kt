package com.heyzeusv.plutuswallet.ui.settings

import android.content.SharedPreferences
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceManager
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.ui.PreviewHelper
import com.heyzeusv.plutuswallet.util.Key
import com.heyzeusv.plutuswallet.util.PWListAlertDialog
import com.heyzeusv.plutuswallet.util.PWAlertDialog
import com.heyzeusv.plutuswallet.util.PreferenceHelper.get
import com.heyzeusv.plutuswallet.util.PreferenceHelper.set
import com.heyzeusv.plutuswallet.util.SettingOptions
import com.heyzeusv.plutuswallet.util.SettingOptions.CURRENCY_SYMBOL
import com.heyzeusv.plutuswallet.util.SettingOptions.CURRENCY_SYMBOL_SIDE
import com.heyzeusv.plutuswallet.util.SettingOptions.DATE_FORMAT
import com.heyzeusv.plutuswallet.util.SettingOptions.DECIMAL_NUMBER
import com.heyzeusv.plutuswallet.util.SettingOptions.DECIMAL_SYMBOL
import com.heyzeusv.plutuswallet.util.SettingOptions.LANGUAGE
import com.heyzeusv.plutuswallet.util.SettingOptions.THEME
import com.heyzeusv.plutuswallet.util.SettingOptions.THOUSANDS_SYMBOL
import com.heyzeusv.plutuswallet.util.SettingsUtils
import java.lang.NumberFormatException

/**
 *  Composable that displays Settings screen.
 *  Data that is displayed is retrieved from [setVM]. [recreateActivity] is by language and theme
 *  settings in order to immediately cause recomposition.
 */
@Composable
fun SettingsScreen(
    setVM: SettingsViewModel,
    recreateActivity: () -> Unit
) {
    SettingsScreen(
        recreateActivity,
        setVM::updateNumberSymbols,
        setVM::updateDecimalNumber,
        setVM::updateCurrencySymbol,
        setVM::updateCurrencySymbolSide,
        setVM::updateThousandsSymbol,
        setVM::updateDecimalSymbol,
        setVM::updateDateFormatter
    )
}

/**
 *  Composable that displays Settings screen.
 *  All the data has been hoisted into above [SettingsScreen] thus allowing for easier testing.
 */
@Composable
fun SettingsScreen(
    recreateActivity: () -> Unit,
    updateNumberSymbols: (Char, Char) -> Unit,
    updateDecimalNumber: (String) -> Unit,
    updateCurrencySymbol: (String) -> Unit,
    updateCurrencySymbolSide: (String) -> Unit,
    updateThousandsSymbol: (Char) -> Unit,
    updateDecimalSymbol: (Char) -> Unit,
    updateDateFormatter: (Int) -> Unit
) {
    val sharedPref = PreferenceManager.getDefaultSharedPreferences(LocalContext.current)

    var decimalSymbolSelectedValue by remember { mutableStateOf("") }
    var thousandsSymbolSelectedValue by remember { mutableStateOf("") }
    var decimalNumberSelectedValue by remember { mutableStateOf("") }
    var openSwitchDialog by remember { mutableStateOf(false) }
    var openDecimalDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        elevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            if (openSwitchDialog) {
                PWAlertDialog(
                    title = stringResource(R.string.alert_dialog_duplicate_symbols),
                    message = stringResource(R.string.alert_dialog_duplicate_symbols_warning),
                    onConfirmText = stringResource(R.string.alert_dialog_switch),
                    onConfirm = {
                        val oldDecimal = sharedPref[Key.KEY_DECIMAL_SYMBOL, "period"]
                        val oldThousands = sharedPref[Key.KEY_THOUSANDS_SYMBOL, "comma"]
                        sharedPref[Key.KEY_THOUSANDS_SYMBOL] = oldDecimal
                        sharedPref[Key.KEY_DECIMAL_SYMBOL] = oldThousands
                        updateNumberSymbols(
                            SettingsUtils.getSeparatorSymbol(oldDecimal),
                            SettingsUtils.getSeparatorSymbol(oldThousands)
                        )
                        openSwitchDialog = false
                    },
                    onDismissText = stringResource(R.string.alert_dialog_cancel),
                    onDismiss = { openSwitchDialog = false }
                )
            }
            if (openDecimalDialog) {
                val current = sharedPref[Key.KEY_DECIMAL_NUMBER, "yes"]
                PWAlertDialog(
                    title = stringResource(R.string.alert_dialog_are_you_sure),
                    message = if (current == "yes") {
                        stringResource(R.string.alert_dialog_decimal_place_warning)
                    } else {
                        ""
                    },
                    onConfirmText = stringResource(R.string.alert_dialog_switch),
                    onConfirm = {
                        when (current) {
                            "yes" -> {
                                sharedPref[Key.KEY_DECIMAL_NUMBER] = "no"
                                updateDecimalNumber("no")
                            }
                            "no" -> {
                                sharedPref[Key.KEY_DECIMAL_NUMBER] = "yes"
                                updateDecimalNumber("yes")
                            }
                        }
                        openDecimalDialog = false
                    },
                    onDismissText = stringResource(R.string.alert_dialog_cancel),
                    onDismiss = { openDecimalDialog = false }
                )
            }
            SettingSetup(THEME, sharedPref) {
                sharedPref[THEME.key] = it
                recreateActivity()
            }
            SettingSetup(CURRENCY_SYMBOL, sharedPref) {
                sharedPref[CURRENCY_SYMBOL.key] = it
                updateCurrencySymbol(SettingsUtils.getCurrencySymbol(it))
            }
            SettingSetup(CURRENCY_SYMBOL_SIDE, sharedPref) {
                sharedPref[CURRENCY_SYMBOL_SIDE.key] = it
                updateCurrencySymbolSide(it)
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
                    updateThousandsSymbol(newThousandsSymbol)
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
                    updateDecimalSymbol(newDecimalSymbol)
                    decimalSymbolSelectedValue = "\"$newDecimalSymbol\""
                }
            }
            SettingSetup(
                DECIMAL_NUMBER,
                optionSelectedDisplay = decimalNumberSelectedValue,
                updateOptionSelectedDisplay = { decimalNumberSelectedValue = it },
                sharedPref,
            ) {
                val currentValue = sharedPref[Key.KEY_DECIMAL_NUMBER, "yes"]
                if (currentValue != it) {
                    openDecimalDialog = true
                }
            }
            SettingSetup(DATE_FORMAT, sharedPref) {
                sharedPref[DATE_FORMAT.key] = it
                updateDateFormatter(try { it.toInt() } catch (e: NumberFormatException) { 0 })
            }
            SettingSetup(LANGUAGE, sharedPref) {
                sharedPref[LANGUAGE.key] = it
                sharedPref[Key.KEY_MANUAL_LANGUAGE] = true
                recreateActivity()
            }
        }
    }
}

/**
 *  Helper function for setting up [Setting] Composable. [setting] is option that will be used here.
 *  [sharedPref] is used to retrieve the current value. [onConfirm] determines how to handle user
 *  confirmation.
 *  This version is used when no additional dialogs are required when selecting a different value
 *  for [setting].
 */
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

/**
 *  Helper function for setting up [Setting] Composable. [setting] is option that will be used here.
 *  [optionSelectedDisplay] is the String that tells the user the currently selected option.
 *  [updateOptionSelectedDisplay] is used to update [optionSelectedDisplay]. [sharedPref] is used
 *  to retrieve the current value. [onConfirm] determines how to handle user confirmation.
 *  This version is used when additional dialogs are required when selecting a different value
 *  for [setting].
 */
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

/**
 *  Composable that displays a single [setting]. [optionsMap] is the map of values that user will be
 *  able to select from. [optionSelectedValue] is the currently selected key.
 *  [optionSelectedDisplay] is value that is paired to [optionSelectedValue] in [optionsMap].
 *  [optionSelectedDisplay] is also displayed to the user before selecting. [onConfirm] determines
 *  how to handle user confirmation.
 */
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
            .testTag(setting.name)
    ) {
        if (openDialog) {
            PWListAlertDialog(
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
                modifier = Modifier.testTag("${setting.name} $optionSelectedDisplay"),
                style = MaterialTheme.typography.subtitle2
            )
        }
    }
}

@Preview
@Composable
fun SettingsScreenPreview() {
    PreviewHelper {
        SettingsScreen(
            recreateActivity = { },
            updateNumberSymbols = { _, _ -> },
            updateDecimalNumber = { },
            updateCurrencySymbol = { },
            updateCurrencySymbolSide = { },
            updateThousandsSymbol = { },
            updateDecimalSymbol = { },
            updateDateFormatter = { }
        )
    }
}

@Preview
@Composable
fun SettingPreview() {
    PreviewHelper {
        Setting(
            setting = THOUSANDS_SYMBOL,
            optionsMap = mapOf("period" to ".", "comma" to ","),
            optionSelectedValue = "comma",
            optionSelectedDisplay = ",",
            onConfirm = { }
        )
    }
}