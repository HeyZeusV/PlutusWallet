package com.heyzeusv.plutuswallet.ui.transaction

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.toSize
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.model.Transaction
import com.heyzeusv.plutuswallet.ui.AppBarActions
import com.heyzeusv.plutuswallet.ui.BackPressHandler
import com.heyzeusv.plutuswallet.ui.PWButton
import com.heyzeusv.plutuswallet.ui.PreviewHelper
import com.heyzeusv.plutuswallet.ui.PreviewHelperCard
import com.heyzeusv.plutuswallet.util.theme.LocalPWColors
import com.heyzeusv.plutuswallet.util.DateUtils
import com.heyzeusv.plutuswallet.util.PWInputAlertDialog
import com.heyzeusv.plutuswallet.util.PWAlertDialog
import com.heyzeusv.plutuswallet.util.TransactionType
import java.util.Date
import kotlinx.coroutines.launch

/**
 *  Composable that displays Transaction Card.
 *  Data that is displayed is retrieved from [tranVM]. [tranId] is passed in through Navigation.
 *  [appBarActionSetup] determines what to do when an action item is pressed from the AppBar.
 *  [showSnackbar] display Snackbar on successful save.
 *  [navigateUp] allows for navigation back to OverviewScreen.
 */
@Composable
fun TransactionScreen(
    tranVM: TransactionViewModel,
    tranId: Int,
    appBarActionSetup: (AppBarActions) -> Unit,
    showSnackbar: suspend (String) -> Unit,
    navigateUp: () -> Unit
) {
    if (tranVM.retrieveTransaction) {
        tranVM.retrieveTransaction(tranId)
    }
    val totalMaxLength = integerResource(
        if (tranVM.setVals.decimalNumber == "yes") {
            R.integer.maxLengthTotalDecimal
        } else {
            R.integer.maxLengthTotalInteger
        }
    )

    // used for SnackBar
    val saveSuccess by tranVM.saveSuccess.collectAsState()
    val saveSuccessMessage = stringResource(R.string.snackbar_saved)

    // data to be displayed
    val transaction by tranVM.transaction.collectAsState()
    val title by tranVM.title.collectAsState()
    val date by tranVM.date.collectAsState()
    val account by tranVM.account.collectAsState()
    val total by tranVM.total.collectAsState()
    val typeSelected by tranVM.typeSelected.collectAsState()
    val selectedCat by tranVM.selectedCat.collectAsState()
    val memo by tranVM.memo.collectAsState()
    val repeat by tranVM.repeat.collectAsState()
    val period by tranVM.period.collectAsState()
    val frequency by tranVM.frequency.collectAsState()

    // drop down menu lists
    val accountList by tranVM.accountList.collectAsState()
    val selectedCatList by tranVM.selectedCatList.collectAsState()
    val periodList by tranVM.periodList.collectAsState()

    // determine which dialog to display
    val showAccountDialog by tranVM.showAccountDialog.collectAsState()
    val showCategoryDialog by tranVM.showCategoryDialog.collectAsState()
    val showFutureDialog by tranVM.showFutureDialog.collectAsState()

    // set up AppBar actions
    appBarActionSetup(
        AppBarActions(
            onNavPressed = {
                tranVM.updateSaveSuccess(false)
                tranVM.retrieveTransaction = true
                navigateUp()
            },
            onActionRightPressed = { tranVM.saveTransaction() }
        )
    )
    BackPressHandler {
        tranVM.updateSaveSuccess(false)
        tranVM.retrieveTransaction = true
        navigateUp()
    }
    TransactionScreen(
        transaction,
        title,
        tranVM::updateTitle,
        date,
        tranVM::onDateSelected,
        account,
        tranVM::updateAccount,
        total,
        tranVM::updateTotal,
        totalMaxLength,
        typeSelected,
        tranVM::updateTypeSelected,
        selectedCat,
        tranVM::updateSelectedCat,
        memo,
        tranVM::updateMemo,
        repeat,
        tranVM::updateRepeat,
        period,
        tranVM::updatePeriod,
        frequency,
        tranVM::updateFrequency,
        accountList,
        selectedCatList,
        periodList,
        showAccountDialog,
        tranVM::updateAccountDialog,
        accountDialogOnConfirm = tranVM::insertAccount,
        showCategoryDialog,
        tranVM::updateCategoryDialog,
        categoryDialogOnConfirm = tranVM::insertCategory,
        showFutureDialog,
        futureDialogOnConfirm = { tranVM.futureDialogConfirm() },
        futureDialogOnDismiss = { tranVM.futureDialogDismiss() },
        saveSuccess,
        onSaveSuccess = {
            showSnackbar(saveSuccessMessage)
            tranVM.updateSaveSuccess(false)
        }
    )
}

/**
 *  Composable that displays TransactionScreen.
 *  All the data has been hoisted into above [TransactionScreen] thus allowing for easier testing.
 */
@Composable
fun TransactionScreen(
    transaction: Transaction,
    title: String,
    updateTitle: (String) -> Unit,
    date: String,
    onDateSelected: (Date) -> Unit,
    account: String,
    updateAccount: (String) -> Unit,
    total: TextFieldValue,
    updateTotal: (String) -> Unit,
    totalMaxLength: Int,
    typeSelected: TransactionType,
    updateTypeSelected: (TransactionType) -> Unit,
    selectedCat: String,
    updateSelectedCat: (String) -> Unit,
    memo: String,
    updateMemo: (String) -> Unit,
    repeat: Boolean,
    updateRepeat: (Boolean) -> Unit,
    period: String,
    updatePeriod: (String) -> Unit,
    frequency: TextFieldValue,
    updateFrequency: (String) -> Unit,
    accountList: List<String>,
    selectedCatList: List<String>,
    periodList: List<String>,
    showAccountDialog: Boolean,
    updateAccountDialog: (Boolean) -> Unit,
    accountDialogOnConfirm: (String) -> Unit,
    showCategoryDialog: Boolean,
    updateCategoryDialog: (Boolean) -> Unit,
    categoryDialogOnConfirm: (String) -> Unit,
    showFutureDialog: Boolean,
    futureDialogOnConfirm: () -> Unit,
    futureDialogOnDismiss: () -> Unit,
    saveSuccess: Boolean,
    onSaveSuccess: suspend () -> Unit
) {
    // used by DatePicker
    val dateObj = transaction.date
    val view = LocalView.current

    // displays SnackBar that Transaction was saved when saveSuccess is true
    LaunchedEffect(key1 = saveSuccess) { if (saveSuccess) onSaveSuccess() }
    if (showFutureDialog) {
        PWAlertDialog(
            onConfirmText = stringResource(R.string.alert_dialog_yes),
            onConfirm = { futureDialogOnConfirm() },
            onDismissText = stringResource(R.string.alert_dialog_no),
            onDismiss = { futureDialogOnDismiss() },
            title = stringResource(R.string.alert_dialog_future_transaction),
            message = stringResource(R.string.alert_dialog_future_transaction_warning)
        )
    }
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = dimensionResource(R.dimen.cardFullPadding)),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = dimensionResource(R.dimen.card_content_pad))
                .verticalScroll(rememberScrollState())
        ) {
            TransactionTextInput(
                value = title,
                onValueChanged = updateTitle,
                label = stringResource(R.string.transaction_title),
                helper = stringResource(R.string.transaction_title_hint),
                maxLength = integerResource(R.integer.maxLengthTitle),
                modifier = Modifier.padding(
                    top = dimensionResource(R.dimen.textFToParentTopPadding)
                )
            )
            TransactionDate(
                value = date,
                label = stringResource(R.string.transaction_date),
                onPressed = {
                    DateUtils.datePickerDialog(view, dateObj, onDateSelected).show()
                },
                modifier = Modifier.padding(
                    top = dimensionResource(R.dimen.textFToTextFWHelperTopPadding)
                )
            )
            TransactionDropDownMenu(
                value = account,
                list = accountList,
                onClick = updateAccount,
                label = stringResource(R.string.transaction_account),
                createNew = stringResource(R.string.account_create),
                showInputDialog = showAccountDialog,
                updateInputDialog = { updateAccountDialog(true) },
                dialogTitle = stringResource(R.string.alert_dialog_create_account),
                dialogOnConfirm = accountDialogOnConfirm,
                dialogOnDismiss = { updateAccountDialog(false) },
                modifier = Modifier.padding(
                    top = dimensionResource(R.dimen.textFToViewTopPadding)
                )
            )
            TransactionNumberInput(
                value = total,
                onValueChanged = updateTotal,
                label = stringResource(R.string.transaction_total),
                maxLength = totalMaxLength,
                modifier = Modifier.padding(
                    top = dimensionResource(R.dimen.textFToViewTopPadding)
                )
            )
            TransactionCategories(
                typeSelected = typeSelected,
                chipExpenseOnClick = { updateTypeSelected(TransactionType.EXPENSE) },
                chipIncomeOnClick = { updateTypeSelected(TransactionType.INCOME) },
                dropDownValue = selectedCat,
                dropDownList = selectedCatList,
                dropDownOnClick = updateSelectedCat,
                showInputDialog = showCategoryDialog,
                updateInputDialog = { updateCategoryDialog(true) },
                dialogOnConfirm = categoryDialogOnConfirm,
                dialogOnDismiss = { updateCategoryDialog(false) },
                modifier = Modifier.padding(
                    top = dimensionResource(R.dimen.chipToTextFWHelperTopPadding)
                )
            )
            TransactionTextInput(
                value = memo,
                onValueChanged = { updateMemo(it) },
                label = stringResource(R.string.transaction_memo),
                helper = stringResource(R.string.transaction_memo_hint),
                maxLength = integerResource(R.integer.maxLengthMemo),
                modifier = Modifier.padding(
                    top = dimensionResource(R.dimen.textFToViewTopPadding)
                )
            )
            TransactionRepeating(
                newTransaction = transaction.id == 0,
                chipSelected = repeat,
                chipOnClick = { updateRepeat(!repeat) },
                dropDownValue = period,
                dropDownList = periodList,
                dropDownOnClick = updatePeriod,
                inputValue = frequency,
                inputOnValueChanged = updateFrequency,
                modifier = Modifier.padding(
                    top = dimensionResource(R.dimen.chipToTextFWHelperTopPadding)
                )
            )
        }
    }
}

/**
 *  Composable for text input. [value] is a collected StateFlow that is updated by [onValueChanged].
 *  [label] lets user know what TextField is for, while [helper] gives a suggestion for input.
 *  [maxLength] is used to display counter for maximum characters allowed.
 */
@Composable
fun TransactionTextInput(
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
            modifier = Modifier
                .fillMaxWidth()
                .testTag(label),
            label = { Text(text = label) },
            singleLine = true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colors.secondary,
                focusedLabelColor = MaterialTheme.colors.secondary,
                unfocusedLabelColor = MaterialTheme.colors.secondary
            )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(R.dimen.tfh_horiPad))
                .padding(top = dimensionResource(R.dimen.tfh_topPad))
                .height(dimensionResource(R.dimen.tfh_height)),
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

/**
 *  Composable that displays date. TextField is used, but it is disabled, so that user cannot
 *  type in date. [value] is a collected StateFlow. [label] lets user know what TextField is for.
 *  [onPressed] updates dateDialog which causes DatePickerDialog to appear.
 */
@Composable
fun TransactionDate(
    value: String,
    label: String,
    onPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val source = remember { MutableInteractionSource() }
    if (source.collectIsPressedAsState().value) onPressed()

    DisableSelection {
        OutlinedTextField(
            value = value,
            onValueChange = { },
            modifier = modifier
                .fillMaxWidth()
                .testTag(label),
            readOnly = true,
            label = { Text(text = label) },
            interactionSource = source,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colors.secondary,
                focusedLabelColor = MaterialTheme.colors.secondary,
                unfocusedLabelColor = MaterialTheme.colors.secondary
            )
        )
    }
}

/**
 *  Composable that displays drop down menus. [value] is the currently selected value. [list]
 *  is the values displayed when the drop down menu is expanded. [onClick] is called whenever a
 *  value is selected from menu. [label] lets user know which drop down menu this is.
 *  [createNew] is the last item of list which has its own onClick, [updateInputDialog], which
 *  updates [showInputDialog], which shows InputAlertDialog with [dialogTitle].
 *  [dialogOnConfirm] saves (if no error) user's input, while [dialogOnDismiss] closes the dialog.
 */
@Composable
fun TransactionDropDownMenu(
    value: String,
    list: List<String>,
    onClick: (String) -> Unit,
    label: String,
    createNew: String,
    showInputDialog: Boolean,
    updateInputDialog: () -> Unit,
    dialogTitle: String,
    dialogOnConfirm: (String) -> Unit,
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
                PWInputAlertDialog(
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
                    }
                    .testTag(label),
                readOnly = true,
                label = { Text(label) },
                trailingIcon = {
                    Icon(
                        imageVector = if (expanded) {
                            Icons.Filled.KeyboardArrowUp
                        } else {
                            Icons.Filled.KeyboardArrowDown
                        },
                        contentDescription = stringResource(R.string.icon_cd_expandcollapse),
                        tint = if (expanded) {
                            MaterialTheme.colors.secondary
                        } else {
                            LocalPWColors.current.unselected
                        }
                    )
                },
                interactionSource = source,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colors.secondary,
                    focusedLabelColor = MaterialTheme.colors.secondary,
                    unfocusedLabelColor = MaterialTheme.colors.secondary
                )
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.width(with(LocalDensity.current) { textFieldSize.width.toDp() })
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
                        },
                        modifier = Modifier.testTag(name)
                    ) {
                        Text(text = name)
                    }
                }
            }
        }
    }
}

/**
 *  Composable for number input. [value] is a collected StateFlow that is updated by
 *  [onValueChanged]. [label] lets user know what TextField is for. [maxLength] is used to
 *  display counter for maximum characters allowed.
 */
@Composable
fun TransactionNumberInput(
    value: TextFieldValue,
    onValueChanged: (String) -> Unit,
    label: String,
    maxLength: Int,
    modifier: Modifier = Modifier,
    numberFieldModifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { if (it.text.length <= maxLength) onValueChanged(it.text) },
            modifier = numberFieldModifier
                .fillMaxWidth()
                .testTag(label),
            label = { Text(label) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colors.secondary,
                focusedLabelColor = MaterialTheme.colors.secondary,
                unfocusedLabelColor = MaterialTheme.colors.secondary
            )
        )
        Text(
            text = "${value.text.length}/$maxLength",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(R.dimen.tfh_horiPad))
                .padding(top = dimensionResource(R.dimen.tfh_topPad))
                .height(dimensionResource(R.dimen.tfh_height)),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.caption
        )
    }
}

/**
 *  Composable that brings together [PWButton]s and [TransactionDropDownMenu] to handle
 *  Category selection. [typeSelected] determines which chip is currently selected, which is
 *  updated by [chipExpenseOnClick] and [chipIncomeOnClick]. [dropDownValue] is the currently
 *  selected value. [dropDownList] is the values displayed when the drop down menu is expanded.
 *  [dropDownOnClick] is called whenever a value is selected from menu. [showInputDialog]
 *  determines if [PWInputAlertDialog] should be displayed, which is updated by [updateInputDialog].
 *  [dialogOnConfirm] saves (if no error) user's input, while [dialogOnDismiss] closes the dialog.
 */
@Composable
fun TransactionCategories(
    typeSelected: TransactionType,
    chipExpenseOnClick: () -> Unit,
    chipIncomeOnClick: () -> Unit,
    dropDownValue: String,
    dropDownList: List<String>,
    dropDownOnClick: (String) -> Unit,
    showInputDialog: Boolean,
    updateInputDialog: () -> Unit,
    dialogOnConfirm: (String) -> Unit,
    dialogOnDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                space = dimensionResource(R.dimen.tran_view_spacedBy),
                alignment = Alignment.CenterHorizontally
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PWButton(
                selected = typeSelected == TransactionType.EXPENSE,
                onClick = chipExpenseOnClick,
                label = stringResource(R.string.type_expense),
                showIcon = false,
                modifier = Modifier
                    .height(dimensionResource(R.dimen.transaction_button_chip_height))
                    .weight(1f)
            )
            PWButton(
                selected = typeSelected == TransactionType.INCOME,
                onClick = chipIncomeOnClick,
                label = stringResource(R.string.type_income),
                showIcon = false,
                modifier = Modifier
                    .height(dimensionResource(R.dimen.transaction_button_chip_height))
                    .weight(1f)
            )
        }
        TransactionDropDownMenu(
            value = dropDownValue,
            list = dropDownList,
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

/**
 *  Composable that combines [PWButton], [TransactionDropDownMenu], and
 *  [TransactionNumberInput] to handle repeating Transactions. [newTransaction] determines if
 *  scroll down animation should occur. [chipSelected] determines if chip is selected.
 *  [chipOnClick] is called when chip is selected. [dropDownValue] is currently selected value from
 *  drop down menu. [dropDownList] is the values displayed when the drop down menu is expanded.
 *  [dropDownOnClick] is called whenever a value is selected from drop down menu. [inputValue] is
 *  a collected StateFlow that is updated by [inputOnValueChanged].
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionRepeating(
    newTransaction: Boolean,
    chipSelected: Boolean,
    chipOnClick: () -> Unit,
    dropDownValue: String,
    dropDownList: List<String>,
    dropDownOnClick: (String) -> Unit,
    inputValue: TextFieldValue,
    inputOnValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val bringIntoView = remember { BringIntoViewRequester() }
    val focusRequester = remember { FocusRequester() }

    // gives focus to number input field
    LaunchedEffect(key1 = chipSelected) {
        if (newTransaction && chipSelected) focusRequester.requestFocus()
    }

    Column(modifier = modifier) {
        PWButton(
            selected = chipSelected,
            onClick = chipOnClick,
            label = stringResource(R.string.transaction_repeat),
            showIcon = true,
            modifier = Modifier.height(dimensionResource(R.dimen.transaction_button_chip_height))
        )
        AnimatedVisibility(visible = chipSelected) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = dimensionResource(R.dimen.textFToViewTopPadding))
                    .bringIntoViewRequester(bringIntoView),
                horizontalArrangement = Arrangement.spacedBy(
                    space = dimensionResource(R.dimen.tran_view_spacedBy)
                )
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
                    dialogOnConfirm = {},
                    dialogOnDismiss = {},
                    modifier = Modifier.weight(1f)
                )
                TransactionNumberInput(
                    value = inputValue,
                    onValueChanged = inputOnValueChanged,
                    label = stringResource(R.string.transaction_frequency),
                    maxLength = integerResource(R.integer.maxLengthFrequency),
                    modifier = Modifier
                        .weight(1f)
                        .padding(
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

@Preview
@Composable
fun TransactionScreenPreview() {
    PreviewHelper {
        TransactionScreen(
            transaction = Transaction(),
            title = "Transaction Title",
            updateTitle = { },
            date = "Transaction Date",
            onDateSelected = { },
            account = "Transaction Account",
            updateAccount = { },
            total = TextFieldValue("$12345.67"),
            updateTotal = { },
            totalMaxLength = 100,
            typeSelected = TransactionType.EXPENSE,
            updateTypeSelected = { },
            selectedCat = "Transaction Category",
            updateSelectedCat = { },
            memo = "Transaction Memo",
            updateMemo = { },
            repeat = true ,
            updateRepeat = { },
            period = "Weeks",
            updatePeriod = { },
            frequency = TextFieldValue("100"),
            updateFrequency = { },
            accountList = listOf("Test Account 1", "Test Account 2", "Test Account 3"),
            selectedCatList = listOf("Test Category 1", "Test Category 2", "Test Category 3"),
            periodList = listOf("Days", "Weeks", "Months", "Years"),
            showAccountDialog = false,
            updateAccountDialog = { },
            accountDialogOnConfirm = { },
            showCategoryDialog = false,
            updateCategoryDialog = { },
            categoryDialogOnConfirm = { },
            showFutureDialog = false,
            futureDialogOnConfirm = { },
            futureDialogOnDismiss = { },
            saveSuccess = false,
            onSaveSuccess = { }
        )
    }
}

@Preview
@Composable
fun TransactionTextInputPreview() {
    PreviewHelperCard {
        TransactionTextInput(
            value = "Test Value",
            onValueChanged = { },
            label = "Test Label",
            helper = "Test Helper",
            maxLength = 100
        )
    }
}

@Preview
@Composable
fun TransactionDatePreview() {
    PreviewHelperCard {
        TransactionDate(
            value = "Test Date",
            label = "Test Label",
            onPressed = { }
        )
    }
}

@Preview
@Composable
fun TransactionDropDownMenuPreview() {
    PreviewHelperCard {
        TransactionDropDownMenu(
            value = "Test Value",
            list = listOf("Test Value 1", "Test Value 2", "Test Value 3"),
            onClick = { },
            label = "Test Label",
            createNew = "Create New...",
            showInputDialog = false,
            updateInputDialog = { },
            dialogTitle = "Dialog Title",
            dialogOnConfirm = { },
            dialogOnDismiss = { }
        )
    }
}

@Preview
@Composable
fun TransactionNumberInputPreview() {
    PreviewHelperCard {
        TransactionNumberInput(
            value = TextFieldValue("$12345.67"),
            onValueChanged = { },
            label = "Test Label",
            maxLength = 1000
        )
    }
}

@Preview
@Composable
fun TransactionCategoriesPreview() {
    PreviewHelperCard {
        TransactionCategories(
            typeSelected = TransactionType.EXPENSE,
            chipExpenseOnClick = { },
            chipIncomeOnClick = { },
            dropDownValue = "Expense Value 1",
            dropDownList = listOf("Expense Value 1", "Expense Value 2", "Expense Value 3"),
            dropDownOnClick = { },
            showInputDialog = false,
            updateInputDialog = { },
            dialogOnConfirm = { },
            dialogOnDismiss = { }
        )
    }
}

@Preview
@Composable
fun TransactionRepeatingPreview() {
    PreviewHelperCard {
        TransactionRepeating(
            newTransaction = false,
            chipSelected = true,
            chipOnClick = {  },
            dropDownValue = "Weeks",
            dropDownList = listOf("Days", "Weeks", "Months", "Years"),
            dropDownOnClick = { },
            inputValue = TextFieldValue("10"),
            inputOnValueChanged = { }
        )
    }
}