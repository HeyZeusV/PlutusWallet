package com.heyzeusv.plutuswallet.ui.transaction

import android.content.SharedPreferences
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.toSize
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.util.Key
import com.heyzeusv.plutuswallet.util.PreferenceHelper.get
import com.heyzeusv.plutuswallet.util.SettingsUtils
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

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

@Composable
fun TransactionCurrencyInput(
    sharedPref: SharedPreferences
) {
    // keys from separator symbols
    val decimalKey: String = sharedPref[Key.KEY_DECIMAL_SYMBOL, "period"]
    val thousandsKey: String = sharedPref[Key.KEY_THOUSANDS_SYMBOL, "comma"]
    val decimalPlaces: Boolean = sharedPref[Key.KEY_DECIMAL_PLACES, true]

    // symbols used
    val decimalSymbol: Char = SettingsUtils.getSeparatorSymbol(decimalKey)
    val thousandsSymbol: Char = SettingsUtils.getSeparatorSymbol(thousandsKey)

    // set up decimal/thousands symbol
    val customSymbols = DecimalFormatSymbols(Locale.US)
    customSymbols.decimalSeparator = decimalSymbol
    customSymbols.groupingSeparator = thousandsSymbol

    // formatters using custom symbols
    val decimalFormatter = DecimalFormat("#,##0.00", customSymbols)
    val integerFormatter = DecimalFormat("#,##0", customSymbols)
    decimalFormatter.roundingMode = RoundingMode.HALF_UP
    integerFormatter.roundingMode = RoundingMode.HALF_UP

    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = "",
                selection = TextRange.Zero
            )
        )
    }

    OutlinedTextField(
        value = textFieldValue, // text,
        onValueChange = {
            val formattedAmount = formatAmount(it.text, decimalPlaces, decimalFormatter, integerFormatter)
            textFieldValue = TextFieldValue(
                text = formattedAmount,
                selection = TextRange(formattedAmount.length)
            )
        },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

private fun formatAmount(
    amount: String,
    decimalPlaces: Boolean,
    decimalFormatter: DecimalFormat,
    integerFormatter: DecimalFormat
): String {

    val result: String = removeSymbols(amount, decimalPlaces)
    val amt: BigDecimal
    if (result.isEmpty()) return "" else amt = BigDecimal(result)

    // uses decimal formatter depending on number of decimal places entered
    return when {
        decimalPlaces -> decimalFormatter.format(amt)
        else -> integerFormatter.format(amt)
    }
}

private fun removeSymbols(
    numString: String,
    decimalPlaces: Boolean
): String {

    var chars = ""
    // retrieves only numbers in numString
    for (i: Char in numString) {
        if (i.isDigit()) chars += i
    }

    return when {
        // doesn't allow string to be empty
        decimalPlaces && chars == "" -> "0.00"
        // divides numbers by 100 in order to easily get decimal places
        decimalPlaces -> BigDecimal(chars)
            .divide(BigDecimal(100), 2, RoundingMode.HALF_UP).toString()
        // doesn't allow string to be empty
        chars == "" -> "0"
        // returns just a string of numbers
        else -> chars
    }
}

@Preview
@Composable
fun PreviewTransTextBox() {
    TransactionTextInput("Testing", "Title", "Helper Text", maxLength = 10)
}