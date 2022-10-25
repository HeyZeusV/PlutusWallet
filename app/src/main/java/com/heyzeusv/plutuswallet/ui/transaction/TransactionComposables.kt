package com.heyzeusv.plutuswallet.ui.transaction

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material.IconButton
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
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.toSize
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.ui.theme.PlutusWalletTheme

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun TransactionCompose(
    tranVM: TransactionViewModel,
    onBackPressed: () -> Unit
) {
    PlutusWalletTheme {
        Scaffold(
            topBar = {
                AppBar(
                    tranVM = tranVM,
                    onBackPressed = onBackPressed
                )
            },
            backgroundColor = MaterialTheme.colors.background
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(all = dimensionResource(R.dimen.cardFullPadding)),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colors.onBackground,
                elevation = dimensionResource((R.dimen.cardElevation))
            ) {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    item {
                        TransactionTextField(
                            textField = TransactionTextFields.TITLE,
                            tranVM = tranVM
                        )
                    }
                    item {
                        TransactionDate(
                            tranVM = tranVM,
                            modifier = Modifier.padding(
                                top = dimensionResource(R.dimen.textFToTextFWHelperTopPadding)
                            )
                        )
                    }
                    item {
                        TransactionDropDownMenu(
                            type = TransactionDropMenus.ACCOUNT,
                            tranVM = tranVM,
                            modifier = Modifier.padding(
                                top = dimensionResource(R.dimen.textFToViewTopPadding)
                            )
                        )
                    }
                    item {
                        TransactionCurrencyInput(
                            tranVM = tranVM,
                            modifier = Modifier.padding(
                                top = dimensionResource(R.dimen.textFToViewTopPadding)
                            )
                        )
                    }
                    item {
                        TransactionCategories(
                            tranVM = tranVM,
                            modifier = Modifier.padding(
                                top = dimensionResource(R.dimen.chipToTextFWHelperTopPadding)
                            )
                        )
                    }
                    item {
                        TransactionTextField(
                            textField = TransactionTextFields.MEMO,
                            tranVM = tranVM,
                            modifier = Modifier.padding(
                                top = dimensionResource(R.dimen.textFToViewTopPadding)
                            )
                        )
                    }
                    item {
                        TransactionRepeating(
                            tranVM = tranVM,
                            modifier = Modifier.padding(
                                top = dimensionResource(R.dimen.chipToTextFWHelperTopPadding)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppBar(
    tranVM: TransactionViewModel,
    onBackPressed: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.transaction),
                color = MaterialTheme.colors.onBackground
            )
        },
        navigationIcon = {
            IconButton(onClick = { onBackPressed() }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Go Back",
                    tint = MaterialTheme.colors.onBackground
                )
            }
        },
        actions = {
            IconButton(onClick = { tranVM.saveTransaction() }) {
                Icon(
                    imageVector = Icons.Filled.Save,
                    contentDescription = "Save Icon",
                    tint = MaterialTheme.colors.onBackground
                )
            }
        }
    )
}

enum class TransactionTextFields(val labelId: Int, val helperId: Int, val length: Int) {
    TITLE(R.string.transaction_title, R.string.transaction_title_hint, 32),
    MEMO(R.string.transaction_memo, R.string.transaction_memo_hint, 512),
    FREQUENCY(R.string.transaction_frequency, R.string.transaction_blank_hint, 2)
}

@Composable
fun TransactionTextField(
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
                    when (textField) {
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
            singleLine = true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colors.secondary,
                focusedLabelColor = MaterialTheme.colors.secondary
            )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 4.dp, end = 16.dp)
                .height(16.dp),
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
            modifier = modifier.fillMaxWidth(),
            readOnly = true,
            label = { Text(text = stringResource(R.string.transaction_date)) },
            interactionSource = source,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colors.secondary,
                focusedLabelColor = MaterialTheme.colors.secondary
            )
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
                        tint = if (expanded) {
                            MaterialTheme.colors.secondary
                        } else {
                            colorResource(R.color.colorButtonUnselected)
                        }
                    )
                },
                interactionSource = source,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colors.secondary,
                    focusedLabelColor = MaterialTheme.colors.secondary
                )
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
                Column {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.padding(top = 4.dp),
                        label = { Text(text = stringResource(R.string.alert_dialog_input_hint)) },
                        isError = isError,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = MaterialTheme.colors.secondary,
                            focusedLabelColor = MaterialTheme.colors.secondary
                        )
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
    modifier: Modifier = Modifier
) {
    val textFieldValue by tranVM.totalFieldValue.collectAsState()
    val maxLength = when(tranVM.setVals.decimalPlaces) {
        true -> integerResource(R.integer.maxLengthTotal)
        false -> integerResource(R.integer.maxLengthTotalNoDecimal)
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = {
                tranVM.updateTotalFieldValue(it.text)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.transaction_total)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colors.secondary,
                focusedLabelColor = MaterialTheme.colors.secondary
            )
        )
        Text(
            text = "${textFieldValue.text.length}/$maxLength",
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 4.dp, end = 16.dp)
                .height(16.dp),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.caption
        )
    }
}

@Composable
fun TransactionCategories(
    tranVM: TransactionViewModel,
    modifier: Modifier = Modifier
) {
    val typeSelected by tranVM.typeSelected.collectAsState()

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                space = 8.dp,
                alignment = Alignment.CenterHorizontally
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TransactionChip(
                chip = TransactionChips.EXPENSE,
                tranVM = tranVM,
                modifier = Modifier.weight(1f)
            )
            TransactionChip(
                chip = TransactionChips.INCOME,
                tranVM = tranVM,
                modifier = Modifier.weight(1f)
            )
        }
        TransactionDropDownMenu(
            type = if (typeSelected) TransactionDropMenus.INCOME else TransactionDropMenus.EXPENSE,
            tranVM = tranVM,
            modifier = Modifier.padding(
                top = dimensionResource(R.dimen.textFToViewTopPadding)
            )
        )
    }
}

enum class TransactionChips(val labelId: Int, val icon: Boolean) {
    EXPENSE(R.string.type_expense, false),
    INCOME(R.string.type_income, false),
    REPEAT(R.string.transaction_repeat, true)
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalUnitApi::class)
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
                TransactionChips.EXPENSE -> tranVM.updateTypeSelected(false)
                TransactionChips.INCOME -> tranVM.updateTypeSelected(true)
                TransactionChips.REPEAT -> tranVM.updateRepeat(!selected)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(
            width = 2.dp,
            color = when {
                (chip == TransactionChips.INCOME || chip == TransactionChips.REPEAT) && selected -> {
                    MaterialTheme.colors.secondary
                }
                chip == TransactionChips.EXPENSE && !selected -> MaterialTheme.colors.secondary
                else -> colorResource(R.color.colorButtonUnselected)
            }
        ),
        colors = ChipDefaults.filterChipColors(
            backgroundColor = Color.White,
            selectedBackgroundColor = Color.White
        ),
        trailingIcon = {
            if (chip.icon) {
                Icon(
                    imageVector = if (selected) {
                        Icons.Filled.KeyboardArrowUp
                    } else {
                        Icons.Filled.KeyboardArrowDown
                    },
                    contentDescription = "content",
                    tint = if (selected) {
                        MaterialTheme.colors.secondary
                    } else {
                         colorResource(R.color.colorButtonUnselected)
                    }
                )
            }
        },
        content = {
            Text(
                text = stringResource(chip.labelId).uppercase(),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                color = when {
                    (chip == TransactionChips.INCOME || chip == TransactionChips.REPEAT) && selected -> {
                        MaterialTheme.colors.secondary
                    }
                    chip == TransactionChips.EXPENSE && !selected -> MaterialTheme.colors.secondary
                    else -> colorResource(R.color.colorButtonUnselected)
                },
                fontSize = TextUnit(18f, TextUnitType.Sp),
                fontWeight = FontWeight.Medium,
                letterSpacing = TextUnit(1f, TextUnitType.Sp),
                textAlign = TextAlign.Center
            )
        }
    )
}

@Composable
fun TransactionRepeating(
    tranVM: TransactionViewModel,
    modifier: Modifier = Modifier
) {
    val visible by tranVM.repeat.collectAsState()

    Column(modifier = modifier) {
        TransactionChip(TransactionChips.REPEAT, tranVM)
        AnimatedVisibility(visible = visible) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = dimensionResource(R.dimen.textFToViewTopPadding)
                    ),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TransactionDropDownMenu(TransactionDropMenus.PERIOD, tranVM,
                    Modifier
                        .weight(1f)
                        .padding(end = 4.dp))
                TransactionTextField(TransactionTextFields.FREQUENCY, tranVM,
                    Modifier
                        .weight(1f)
                        .padding(start = 4.dp))
            }
        }
    }
}