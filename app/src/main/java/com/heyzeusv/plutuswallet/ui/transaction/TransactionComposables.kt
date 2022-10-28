package com.heyzeusv.plutuswallet.ui.transaction

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.Surface
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
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
import com.heyzeusv.plutuswallet.util.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun TransactionCompose(
    tranVM: TransactionViewModel,
    onBackPressed: () -> Unit
) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val saveSuccess by tranVM.saveSuccess.collectAsState()
    val saved = stringResource(R.string.snackbar_saved)
    val showFutureDialog by tranVM.showFutureDialog.collectAsState()

    LaunchedEffect(key1 = saveSuccess) {
        if (saveSuccess) {
            scaffoldState.snackbarHostState.showSnackbar(
                message = saved,
                duration = SnackbarDuration.Short
            )
            tranVM.updateSaveSuccess(false)
        }
    }

    PlutusWalletTheme {
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                AppBar(
                    onBackPressed = onBackPressed,
                    onSavePressed = { tranVM.saveTransaction() }
                )
            },
            backgroundColor = MaterialTheme.colors.background
        ) {
            if (showFutureDialog) {
                FutureAlertDialog(
                    onConfirm = { tranVM.futureDialogConfirm() },
                    onDismiss = { tranVM.futureDialogDismiss() }
                )
            }
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(all = dimensionResource(R.dimen.cardFullPadding)),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colors.onBackground,
                elevation = dimensionResource((R.dimen.cardElevation))
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    TransactionTextField(
                        textField = TransactionTextFields.TITLE,
                        tranVM = tranVM,
                        modifier = Modifier.padding(
                            top = dimensionResource(R.dimen.textFToParentTopPadding)
                        )
                    )
                    TransactionDate(
                        tranVM = tranVM,
                        modifier = Modifier.padding(
                            top = dimensionResource(R.dimen.textFToTextFWHelperTopPadding)
                        )
                    )
                    TransactionDropDownMenu(
                        type = TransactionDropMenus.ACCOUNT,
                        tranVM = tranVM,
                        modifier = Modifier.padding(
                            top = dimensionResource(R.dimen.textFToViewTopPadding)
                        )
                    )
                    TransactionNumberInput(
                        numberField = if (tranVM.setVals.decimalPlaces) {
                            TransactionNumberFields.TOTAL_DECIMAL
                        } else {
                            TransactionNumberFields.TOTAL_INTEGER
                        },
                        tranVM = tranVM,
                        modifier = Modifier.padding(
                            top = dimensionResource(R.dimen.textFToViewTopPadding)
                        )
                    )
                    TransactionCategories(
                        tranVM = tranVM,
                        modifier = Modifier.padding(
                            top = dimensionResource(R.dimen.chipToTextFWHelperTopPadding)
                        )
                    )
                    TransactionTextField(
                        textField = TransactionTextFields.MEMO,
                        tranVM = tranVM,
                        modifier = Modifier.padding(
                            top = dimensionResource(R.dimen.textFToViewTopPadding)
                        )
                    )
                    TransactionRepeating(
                        tranVM = tranVM,
                        scope = scope,
                        modifier = Modifier.padding(
                            top = dimensionResource(R.dimen.chipToTextFWHelperTopPadding)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun AppBar(
    onBackPressed: () -> Unit,
    onSavePressed: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.transaction),
                color = MaterialTheme.colors.onBackground
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Go Back",
                    tint = MaterialTheme.colors.onBackground
                )
            }
        },
        actions = {
            IconButton(onClick = onSavePressed) {
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
    TITLE(R.string.transaction_title, R.string.transaction_title_hint, R.integer.maxLengthTitle),
    MEMO(R.string.transaction_memo, R.string.transaction_memo_hint, R.integer.maxLengthMemo),
}

@Composable
fun TransactionTextField(
    textField: TransactionTextFields,
    tranVM: TransactionViewModel,
    modifier: Modifier = Modifier,
    textFieldModifier: Modifier = Modifier
) {

    val value by when(textField) {
        TransactionTextFields.TITLE -> tranVM.title.collectAsState()
        TransactionTextFields.MEMO -> tranVM.memo.collectAsState()
    }
    val maxLength = integerResource(textField.length)

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                if (it.length <= maxLength) {
                    when (textField) {
                        TransactionTextFields.TITLE -> tranVM.updateTitle(it)
                        TransactionTextFields.MEMO -> tranVM.updateMemo(it)
                    }
                }
            },
            modifier = textFieldModifier.fillMaxWidth(),
            label = { Text(stringResource(textField.labelId)) },
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
                text = "${value.length}/$maxLength",
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
    val tran = tranVM.tran.collectAsState()
    val dateString by tranVM.date.observeAsState()
    val source = remember { MutableInteractionSource() }
    val selectDate by tranVM.selectDate.collectAsState()

    DisableSelection {
        if (selectDate) {
            val date = tran.value.date
            DateUtils.datePickerDialog(LocalView.current, date, tranVM::onDateSelected).show()
            tranVM.updateSelectDate(false)
        }
        OutlinedTextField(
            value = dateString!!,
            onValueChange = { },
            modifier = modifier
                .fillMaxWidth()
                .onFocusChanged {
                    if (it.isFocused) tranVM.updateSelectDate(true)
                },
            readOnly = true,
            label = { Text(text = stringResource(R.string.transaction_date)) },
            interactionSource = source,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colors.secondary,
                focusedLabelColor = MaterialTheme.colors.secondary
            )
        )
    }

    if (source.collectIsPressedAsState().value) tranVM.updateSelectDate(true)
}

enum class TransactionDropMenus(val labelId: Int, val createNewId: Int, val alertTitleId: Int) {
    ACCOUNT(R.string.transaction_account, R.string.account_create, R.string.alert_dialog_create_account),
    EXPENSE(R.string.transaction_category, R.string.category_create, R.string.alert_dialog_create_category),
    INCOME(R.string.transaction_category, R.string.category_create, R.string.alert_dialog_create_category),
    PERIOD(R.string.transaction_period, 0, 0)
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
    val showDialog by tranVM.showInputDialog.collectAsState()
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
                InputAlertDialog(
                    tranVM = tranVM,
                    type = type,
                    onDismiss = { tranVM.updateInputDialog(false) }
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
                                tranVM.updateInputDialog(true)
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
fun InputAlertDialog(
    tranVM: TransactionViewModel,
    type: TransactionDropMenus,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }
    val showDialog by tranVM.showInputDialog.collectAsState()
    var isError by remember { mutableStateOf(false) }
    val createNew = stringResource(type.createNewId)

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = {
                        if (text.isNotBlank()) {
                            isError = false
                            when (type) {
                                TransactionDropMenus.ACCOUNT -> tranVM.insertAccount(text, createNew)
                                else  -> tranVM.insertCategory(text, createNew)
                            }
                            tranVM.updateInputDialog(false)
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
            title = { Text(text = stringResource(type.alertTitleId)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.padding(top = 4.dp),
                        label = { Text(text = stringResource(R.string.alert_dialog_input_hint)) },
                        isError = isError,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = MaterialTheme.colors.onSurface,
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

enum class TransactionNumberFields(val labelId: Int, val length: Int) {
    TOTAL_DECIMAL(R.string.transaction_total, R.integer.maxLengthTotalDecimal),
    TOTAL_INTEGER(R.string.transaction_total, R.integer.maxLengthTotalInteger),
    FREQUENCY(R.string.transaction_frequency, R.integer.maxLengthFrequency)
}

@Composable
fun TransactionNumberInput(
    numberField: TransactionNumberFields,
    tranVM: TransactionViewModel,
    modifier: Modifier = Modifier,
    numberFieldModifier: Modifier = Modifier
) {
    val textFieldValue by when (numberField) {
        TransactionNumberFields.FREQUENCY -> tranVM.frequencyFieldValue.collectAsState()
        else -> tranVM.totalFieldValue.collectAsState()
    }
    val maxLength = integerResource(numberField.length)

    Column(modifier = modifier) {
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = {
                if (it.text.length <= maxLength) {
                    when (numberField) {
                        TransactionNumberFields.FREQUENCY -> tranVM.updateFrequencyFieldValue(it.text)
                        else -> tranVM.updateTotalFieldValue(it.text)
                    }
                }
            },
            modifier = numberFieldModifier.fillMaxWidth(),
            label = { Text(stringResource(numberField.labelId)) },
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionRepeating(
    tranVM: TransactionViewModel,
    scope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    val visible by tranVM.repeat.collectAsState()
    val bringIntoView = remember { BringIntoViewRequester() }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(key1 = visible) { if (visible && tranVM.newTran) focusRequester.requestFocus() }

    Column(modifier = modifier) {
        TransactionChip(
            chip = TransactionChips.REPEAT,
            tranVM = tranVM
        )
        AnimatedVisibility(visible = visible) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = dimensionResource(R.dimen.textFToViewTopPadding)
                    )
                    .bringIntoViewRequester(bringIntoView),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TransactionDropDownMenu(
                    type = TransactionDropMenus.PERIOD,
                    tranVM = tranVM,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp)
                )
                TransactionNumberInput(
                    numberField = TransactionNumberFields.FREQUENCY,
                    tranVM = tranVM,
                    modifier = Modifier
                        .weight(1f)
                        .padding(
                            start = 4.dp,
                            bottom = dimensionResource(R.dimen.textFWHelperToParentBottomPadding)
                        ),
                    numberFieldModifier = Modifier
                        .focusRequester(focusRequester)
                        .onFocusEvent { focusState ->
                            if (focusState.isFocused) {
                                scope.launch {
                                    bringIntoView.bringIntoView()
                                }
                            }
                        }
                )
            }
        }
    }
}

@Composable
fun FutureAlertDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(R.string.alert_dialog_yes))
            }
        },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.alert_dialog_no))
            }
        },
        title = { Text(text = stringResource(R.string.alert_dialog_future_transaction)) },
        text = {
            Text(
                text = stringResource(R.string.alert_dialog_future_transaction_warning),
                color = MaterialTheme.colors.onSurface
            )
        }
    )
}