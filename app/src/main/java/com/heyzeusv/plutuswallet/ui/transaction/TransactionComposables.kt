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
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.Card
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Dialog
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

    val transaction by tranVM.transaction.collectAsState()
    val title by tranVM.title.collectAsState()
    val date by tranVM.date.collectAsState()
    val account by tranVM.account.collectAsState()
    val total by tranVM.totalFieldValue.collectAsState()
    val typeSelected by tranVM.typeSelected.collectAsState()
    val expenseCat by tranVM.expenseCat.collectAsState()
    val incomeCat by tranVM.incomeCat.collectAsState()
    val memo by tranVM.memo.collectAsState()
    val repeat by tranVM.repeat.collectAsState()
    val period by tranVM.period.collectAsState()
    val frequency by tranVM.frequencyFieldValue.collectAsState()

    val accountList by tranVM.accountList.collectAsState()
    val expenseCatList by tranVM.expenseCatList.collectAsState()
    val incomeCatList by tranVM.incomeCatList.collectAsState()
    val periodList by tranVM.periodList.collectAsState()

    val showAccountDialog by tranVM.showAccountDialog.collectAsState()
    val showExpenseDialog by tranVM.showExpenseDialog.collectAsState()
    val showIncomeDialog by tranVM.showIncomeDialog.collectAsState()
    val showFutureDialog by tranVM.showFutureDialog.collectAsState()
    val showDateDialog by tranVM.showDateDialog.collectAsState()

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
            if (showDateDialog) {
                val dateObj = transaction.date
                DateUtils.datePickerDialog(LocalView.current, dateObj, tranVM::onDateSelected).show()
                tranVM.updateDateDialog(false)
            }
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
                        value = title,
                        onValueChanged = { tranVM.updateTitle(it) },
                        label = stringResource(R.string.transaction_title),
                        helper = stringResource(R.string.transaction_title_hint),
                        maxLength = integerResource(R.integer.maxLengthTitle),
                        modifier = Modifier.padding(
                            top = dimensionResource(R.dimen.textFToParentTopPadding)
                        )
                    )
                    TransactionDate(
                        date = date,
                        label = stringResource(R.string.transaction_date),
                        onPressed = { tranVM.updateDateDialog(true) },
                        modifier = Modifier.padding(
                            top = dimensionResource(R.dimen.textFToTextFWHelperTopPadding)
                        )
                    )
                    TransactionDropDownMenu(
                        value = account,
                        list = accountList,
                        onClick = tranVM::updateAccount,
                        label = stringResource(R.string.transaction_account),
                        createNew = stringResource(R.string.account_create),
                        showInputDialog = showAccountDialog,
                        updateInputDialog = { tranVM.updateAccountDialog(true) },
                        dialogTitle = stringResource(R.string.alert_dialog_create_account),
                        dialogOnConfirm = tranVM::insertAccount,
                        dialogOnDismiss = { tranVM.updateAccountDialog(false) },
                        modifier = Modifier.padding(
                            top = dimensionResource(R.dimen.textFToViewTopPadding)
                        )
                    )
                    TransactionNumberInput(
                        textFieldValue = total,
                        onValueChanged = tranVM::updateTotalFieldValue,
                        label = stringResource(R.string.transaction_total),
                        maxLength = integerResource(if (tranVM.setVals.decimalPlaces) {
                            R.integer.maxLengthTotalDecimal
                        } else {
                            R.integer.maxLengthTotalInteger
                        }),
                        modifier = Modifier.padding(
                            top = dimensionResource(R.dimen.textFToViewTopPadding)
                        )
                    )
                    when (typeSelected) {
                        TransactionType.EXPENSE -> {
                            TransactionCategories(
                                typeSelected = typeSelected,
                                chipExpenseOnClick = { tranVM.updateTypeSelected(TransactionType.EXPENSE) },
                                chipIncomeOnClick = { tranVM.updateTypeSelected(TransactionType.INCOME) },
                                category = expenseCat,
                                categoryList = expenseCatList,
                                dropDownOnClick = tranVM::updateExpenseCat,
                                showInputDialog = showExpenseDialog,
                                updateInputDialog = { tranVM.updateExpenseDialog(true) },
                                dialogOnConfirm = tranVM::insertCategory,
                                dialogOnDismiss = { tranVM.updateExpenseDialog(false) },
                                modifier = Modifier.padding(
                                    top = dimensionResource(R.dimen.chipToTextFWHelperTopPadding)
                                )
                            )
                        }
                        TransactionType.INCOME -> {
                            TransactionCategories(
                                typeSelected = typeSelected,
                                chipExpenseOnClick = { tranVM.updateTypeSelected(TransactionType.EXPENSE) },
                                chipIncomeOnClick = { tranVM.updateTypeSelected(TransactionType.INCOME) },
                                category = incomeCat,
                                categoryList = incomeCatList,
                                dropDownOnClick = tranVM::updateIncomeCat,
                                showInputDialog = showIncomeDialog,
                                updateInputDialog = { tranVM.updateIncomeDialog(true) },
                                dialogOnConfirm = tranVM::insertCategory,
                                dialogOnDismiss = { tranVM.updateIncomeDialog(false) },
                                modifier = Modifier.padding(
                                    top = dimensionResource(R.dimen.chipToTextFWHelperTopPadding)
                                )
                            )
                        }
                    }
                    TransactionTextField(
                        value = memo,
                        onValueChanged = { tranVM.updateMemo(it) },
                        label = stringResource(R.string.transaction_memo),
                        helper = stringResource(R.string.transaction_memo_hint),
                        maxLength = integerResource(R.integer.maxLengthMemo),
                        modifier = Modifier.padding(top = dimensionResource(R.dimen.textFToViewTopPadding))
                    )
                    TransactionRepeating(
                        newTransaction = tranVM.newTran,
                        chipSelected = repeat,
                        chipOnClick = { tranVM.updateRepeat(!repeat) },
                        dropDownValue = period,
                        dropDownList = periodList,
                        dropDownOnClick = tranVM::updatePeriod,
                        inputValue = frequency,
                        inputOnValueChanged = tranVM::updateFrequencyFieldValue,
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

@Composable
fun TransactionTextField(
    value: String,
    onValueChanged: (String) -> Unit,
    label: String,
    helper: String,
    maxLength: Int,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { if (it.length <= maxLength) onValueChanged(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = label) },
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
                text = helper,
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
    date: String,
    label: String,
    onPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val source = remember { MutableInteractionSource() }
    if (source.collectIsPressedAsState().value) onPressed()

    DisableSelection {
        OutlinedTextField(
            value = date,
            onValueChange = { },
            modifier = modifier.fillMaxWidth(),
            readOnly = true,
            label = { Text(text = label) },
            interactionSource = source,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colors.secondary,
                focusedLabelColor = MaterialTheme.colors.secondary
            )
        )
    }
}

@Composable
fun TransactionDropDownMenu(
    value: String,
    list: MutableList<String>,
    onClick: (String) -> Unit,
    label: String,
    createNew: String,
    showInputDialog: Boolean,
    updateInputDialog: () -> Unit,
    dialogTitle: String,
    dialogOnConfirm: (String, String) -> Unit,
    dialogOnDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    // used to make sure DropdownMenuItems are the same size as OutlinedTextField
    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    val source = remember { MutableInteractionSource() }
    if (source.collectIsPressedAsState().value) expanded = true

    Column(modifier = modifier) {
        DisableSelection {
            if (showInputDialog) {
                expanded = false
                InputAlertDialog(
                    createNew = createNew,
                    title = dialogTitle,
                    onConfirm = dialogOnConfirm,
                    onDismiss = dialogOnDismiss
                )
            }
            OutlinedTextField(
                value = value,
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
                list.forEach { name ->
                    DropdownMenuItem(
                        onClick = {
                            if (name == createNew) {
                                updateInputDialog()
                            } else {
                                onClick(name)
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
}

@Composable
fun InputAlertDialog(
    createNew: String,
    title: String,
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = MaterialTheme.shapes.medium,
            elevation = 8.dp
        ) {
            Column(modifier = modifier.padding(top = 16.dp, bottom = 4.dp)) {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text(text = title)
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 4.dp, end = 16.dp)
                                .height(16.dp),
                            color = MaterialTheme.colors.error,
                            style = MaterialTheme.typography.caption
                        )
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = stringResource(R.string.alert_dialog_cancel).uppercase(),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    TextButton(
                        onClick = {
                            if (text.isNotBlank()) {
                                isError = false
                                onConfirm(text, createNew)
                            } else {
                                isError = true
                            }
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.alert_dialog_save).uppercase(),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionNumberInput(
    textFieldValue: TextFieldValue,
    onValueChanged: (String) -> Unit,
    label: String,
    maxLength: Int,
    modifier: Modifier = Modifier,
    numberFieldModifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = { if (it.text.length <= maxLength) onValueChanged(it.text) },
            modifier = numberFieldModifier.fillMaxWidth(),
            label = { Text(label) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colors.secondary,
                focusedLabelColor = MaterialTheme.colors.secondary
            )
        )
        Text(
            text = "${textFieldValue.text.length}/$maxLength}",
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
    typeSelected: TransactionType,
    chipExpenseOnClick: () -> Unit,
    chipIncomeOnClick: () -> Unit,
    category: String,
    categoryList: MutableList<String>,
    dropDownOnClick: (String) -> Unit,
    showInputDialog: Boolean,
    updateInputDialog: () -> Unit,
    dialogOnConfirm: (String, String) -> Unit,
    dialogOnDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                selected = typeSelected == TransactionType.EXPENSE,
                onClick = chipExpenseOnClick,
                label = stringResource(R.string.type_expense),
                showIcon = false,
                modifier = Modifier.weight(1f)
            )
            TransactionChip(
                selected = typeSelected == TransactionType.INCOME,
                onClick = chipIncomeOnClick,
                label = stringResource(R.string.type_income),
                showIcon = false,
                modifier = Modifier.weight(1f)
            )
        }
        TransactionDropDownMenu(
            value = category,
            list = categoryList,
            onClick = dropDownOnClick,
            label = stringResource(R.string.transaction_category),
            createNew = stringResource(R.string.category_create),
            showInputDialog = showInputDialog,
            updateInputDialog = updateInputDialog,
            dialogTitle = stringResource(R.string.alert_dialog_create_category),
            dialogOnConfirm = dialogOnConfirm,
            dialogOnDismiss = dialogOnDismiss,
            modifier = Modifier.padding(
                top = dimensionResource(R.dimen.textFToViewTopPadding)
            )
        )
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalUnitApi::class)
@Composable
fun TransactionChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    showIcon: Boolean,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(
            width = 2.dp,
            color = if (selected) {
                MaterialTheme.colors.secondary
            } else {
                colorResource(R.color.colorButtonUnselected)
            }
        ),
        colors = ChipDefaults.filterChipColors(
            backgroundColor = Color.White,
            selectedBackgroundColor = Color.White
        ),
        trailingIcon = {
            if (showIcon) {
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
                text = label.uppercase(),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                color = if (selected) {
                    MaterialTheme.colors.secondary
                } else {
                    colorResource(R.color.colorButtonUnselected)
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
    newTransaction: Boolean,
    chipSelected: Boolean,
    chipOnClick: () -> Unit,
    dropDownValue: String,
    dropDownList: MutableList<String>,
    dropDownOnClick: (String) -> Unit,
    inputValue: TextFieldValue,
    inputOnValueChanged: (String) -> Unit,
    scope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    val bringIntoView = remember { BringIntoViewRequester() }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(key1 = chipSelected) {
        if (chipSelected && newTransaction) focusRequester.requestFocus()
    }

    Column(modifier = modifier) {
        TransactionChip(
            selected = chipSelected,
            onClick = chipOnClick,
            label = stringResource(R.string.transaction_repeat),
            showIcon = false
        )
        AnimatedVisibility(visible = chipSelected) {
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
                    value = dropDownValue,
                    list = dropDownList,
                    onClick = dropDownOnClick,
                    label = stringResource(R.string.transaction_period),
                    createNew = stringResource(R.string.blank_string),
                    updateInputDialog = {},
                    showInputDialog = false,
                    dialogTitle = stringResource(R.string.blank_string),
                    dialogOnConfirm = { _, _ -> },
                    dialogOnDismiss = {},
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp)
                )
                TransactionNumberInput(
                    textFieldValue = inputValue,
                    onValueChanged = inputOnValueChanged,
                    label = stringResource(R.string.transaction_frequency),
                    maxLength = integerResource(R.integer.maxLengthFrequency),
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