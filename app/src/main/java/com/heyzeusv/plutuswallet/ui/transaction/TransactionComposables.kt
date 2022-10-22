package com.heyzeusv.plutuswallet.ui.transaction

import android.content.SharedPreferences
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.AlertDialog
import androidx.compose.material.ChipDefaults
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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

enum class TransactionTextFields(val labelId: Int, val helperId: Int, val length: Int) {
    TITLE(R.string.transaction_title, R.string.transaction_title_hint, 32),
    MEMO(R.string.transaction_memo, R.string.transaction_memo_hint, 512),
    FREQUENCY(R.string.transaction_frequency, R.string.transaction_frequency_hint, 2)
}

@Composable
fun TransactionTextInput(
    textField: TransactionTextFields,
    tranVM: TransactionViewModel,
    modifier: Modifier = Modifier,
) {

    val value by when(textField) {
        TransactionTextFields.TITLE -> tranVM.title.collectAsState()
        TransactionTextFields.MEMO -> tranVM.memo.collectAsState()
        TransactionTextFields.FREQUENCY -> tranVM.frequency.collectAsState()
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                if (it.length <= textField.length) {
                    when(textField) {
                        TransactionTextFields.TITLE -> tranVM.updateTitle(it)
                        TransactionTextFields.MEMO -> tranVM.updateMemo(it)
                        TransactionTextFields.FREQUENCY -> tranVM.updateFrequency(it)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(textField.labelId)) },
            keyboardOptions = if (textField == TransactionTextFields.FREQUENCY) {
                KeyboardOptions(keyboardType = KeyboardType.Number)
            } else {
                KeyboardOptions.Default
            },
            singleLine = true
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 4.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(textField.helperId),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.caption
            )
            Text(
                text = "${value.length}/${textField.length}",
                style = MaterialTheme.typography.caption
            )
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
            label = { Text(text = stringResource(R.string.transaction_date)) },
            interactionSource = source
        )
    }

    if (source.collectIsPressedAsState().value) tranVM.selectDateOC(tranVM.tranLD.value!!.date)
}

enum class TransactionDropMenus(val labelId: Int) {
    ACCOUNT(R.string.transaction_account),
    EXPENSE(R.string.transaction_category),
    INCOME(R.string.transaction_category),
    PERIOD(R.string.transaction_period)
}

@Composable
fun TransactionDropDownMenu(
    type: TransactionDropMenus,
    tranVM: TransactionViewModel,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    // used to make sure DropdownMenuItems are the same size as OutlinedTextField
    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    val source = remember { MutableInteractionSource() }
    val showDialog by tranVM.showDialog.collectAsState()
    val typeSelected by tranVM.typeSelected.collectAsState()

    val value = when {
        type == TransactionDropMenus.ACCOUNT -> tranVM.account.collectAsState()
        type == TransactionDropMenus.PERIOD -> tranVM.period.collectAsState()
        typeSelected -> tranVM.incomeCat.collectAsState()
        else -> tranVM.expenseCat.collectAsState()
    }
    val list by when {
        type == TransactionDropMenus.ACCOUNT -> tranVM.accountList.observeAsState()
        type == TransactionDropMenus.PERIOD -> tranVM.periodArray.observeAsState()
        typeSelected -> tranVM.incomeCatList.observeAsState()
        else -> tranVM.expenseCatList.observeAsState()
    }
    val label = stringResource(type.labelId)

    Column(modifier = modifier) {
        DisableSelection {
            if (showDialog) {
                expanded = false
                AlertDialogInput(
                    tranVM = tranVM,
                    onDismiss = { tranVM.updateShowDialog(false) }
                )
            }
            OutlinedTextField(
                value = value.value,
                onValueChange = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        textFieldSize = coordinates.size.toSize()
                    },
                readOnly = true,
                label = { Text(label)},
                trailingIcon = {
                    Icon(
                        imageVector = if (expanded) {
                            Icons.Filled.KeyboardArrowUp
                        } else {
                            Icons.Filled.KeyboardArrowDown
                        },
                        contentDescription = "content",
                        modifier = Modifier.clickable { expanded = !expanded }
                    )
                },
                interactionSource = source
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .width(with(LocalDensity.current) { textFieldSize.width.toDp() })
                    .padding(start = 12.dp)
            ) {
                list!!.forEachIndexed { index, name ->
                    DropdownMenuItem(
                        onClick = {
                            if (index == list!!.size - 1) {
                                tranVM.updateShowDialog(true)
                            } else {
                                when(type) {
                                    TransactionDropMenus.ACCOUNT -> tranVM.updateAccount(name)
                                    TransactionDropMenus.EXPENSE -> tranVM.updateExpenseCat(name)
                                    TransactionDropMenus.INCOME -> tranVM.updateIncomeCat(name)
                                    TransactionDropMenus.PERIOD -> tranVM.updatePeriod(name)
                                }
                                expanded = false
                            }
                        }
                    ) {
                        Text(text = name)
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
                    Text(text = stringResource(R.string.alert_dialog_save))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(R.string.alert_dialog_cancel))
                }
            },
            modifier = modifier,
            title = { Text(text = stringResource(R.string.alert_dialog_create_account)) },
            text = {
                Column() {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.padding(top = 4.dp),
                        label = { Text(text = stringResource(R.string.alert_dialog_input_hint)) },
                        isError = isError
                    )
                    if (isError) {
                        Text(
                            text = stringResource(R.string.alert_dialog_input_error),
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
    tranVM: TransactionViewModel,
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

    val textFieldValue by tranVM.totalFieldValue.collectAsState()

    OutlinedTextField(
        value = textFieldValue, // text,
        onValueChange = {
            val formattedAmount = formatAmount(it.text, decimalPlaces, decimalFormatter, integerFormatter)
            tranVM.updateTotalFieldValue(formattedAmount, TextRange(formattedAmount.length))
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        label = { Text(stringResource(R.string.transaction_total)) },
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

@Composable
fun TransactionCategories(
    tranVM: TransactionViewModel,
    modifier: Modifier = Modifier
) {
    val typeSelected by tranVM.typeSelected.collectAsState()

    Column(
        modifier = modifier
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(
                space = 8.dp,
                alignment = Alignment.CenterHorizontally
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TransactionChip(
                TransactionChips.EXPENSE,
                tranVM,
                modifier.weight(1f)
            )
            TransactionChip(
                TransactionChips.INCOME,
                tranVM,
                modifier.weight(1f)
            )
        }
        TransactionDropDownMenu(
            type = if (typeSelected) TransactionDropMenus.INCOME else TransactionDropMenus.EXPENSE,
            tranVM = tranVM,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

enum class TransactionChips(val labelId: Int) {
    EXPENSE(R.string.type_expense),
    INCOME(R.string.type_income),
    REPEAT(R.string.transaction_repeat)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TransactionChip(
    chip: TransactionChips,
    tranVM: TransactionViewModel,
    modifier: Modifier = Modifier
) {
    val selected by when(chip) {
        TransactionChips.EXPENSE, TransactionChips.INCOME -> tranVM.typeSelected.collectAsState()
        TransactionChips.REPEAT -> tranVM.repeat.collectAsState()
    }
    FilterChip(
        selected = when(chip) {
            TransactionChips.EXPENSE -> !selected
            TransactionChips.INCOME -> selected
            TransactionChips.REPEAT -> selected
        },
        onClick = {
            when(chip) {
                TransactionChips.EXPENSE, TransactionChips.INCOME -> tranVM.updateTypeSelected(!selected)
                TransactionChips.REPEAT -> tranVM.updateRepeat(!selected)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(
            width = 1.dp,
            color = when {
                (chip == TransactionChips.INCOME || chip == TransactionChips.REPEAT) && selected -> {
                    colorResource(R.color.colorButtonBackground)
                }
                chip == TransactionChips.EXPENSE && !selected -> colorResource(R.color.colorButtonBackground)
                else -> colorResource(R.color.colorButtonUnselected)
            }
        ),
        colors = ChipDefaults.filterChipColors(
            backgroundColor = Color.White,
            selectedBackgroundColor = Color.White
        ),
        content = {
            Text(
                text = stringResource(chip.labelId),
                modifier = modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    )
}

@Composable
fun TransactionRepeating(
    tranVM: TransactionViewModel
) {
    val visible by tranVM.repeat.collectAsState()

    Column() {
        TransactionChip(TransactionChips.REPEAT, tranVM, Modifier.padding(horizontal = 12.dp))
        AnimatedVisibility(
            visible = visible
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TransactionDropDownMenu(TransactionDropMenus.PERIOD, tranVM, Modifier.weight(1f).padding(start = 12.dp, end = 4.dp))
                TransactionTextInput(TransactionTextFields.FREQUENCY, tranVM, Modifier.weight(1f).padding(start = 4.dp, end = 12.dp))
            }
        }
    }
}