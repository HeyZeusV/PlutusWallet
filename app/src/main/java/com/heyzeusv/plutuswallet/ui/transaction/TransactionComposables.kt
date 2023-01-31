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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.DataInterface
import com.heyzeusv.plutuswallet.ui.AppBarActions
import com.heyzeusv.plutuswallet.ui.BackPressHandler
import com.heyzeusv.plutuswallet.ui.theme.LocalPWColors
import com.heyzeusv.plutuswallet.ui.theme.alertDialogButton
import com.heyzeusv.plutuswallet.ui.theme.chipTextStyle
import com.heyzeusv.plutuswallet.util.DateUtils
import com.heyzeusv.plutuswallet.util.PWAlertDialog
import kotlinx.coroutines.launch

/**
 *  Composable that displays the entire Transaction screen.
 *  Data that is displayed is retrieved from [tranVM]. [snackbarHostState] is used to
 *  display Snackbar on successful save. [navController] allows user to navigate back to
 *  OverviewScreen onBackPressed.
 */
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun TransactionScreen(
    tranVM: TransactionViewModel,
    tranId: Int,
    appBarActionSetup: (AppBarActions) -> Unit,
    snackbarHostState: SnackbarHostState,
    navController: NavController
) {
    if (tranVM.retrieveTransaction) {
        tranVM.retrieveTransaction(tranId)
    }

    // used for SnackBar
    val saveSuccess by tranVM.saveSuccess.collectAsState()
    val saveSuccessMessage = stringResource(R.string.snackbar_saved)

    // data to be displayed
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

    // drop down menu lists
    val accountList by tranVM.accountList.collectAsState()
    val expenseCatList by tranVM.expenseCatList.collectAsState()
    val incomeCatList by tranVM.incomeCatList.collectAsState()
    val periodList by tranVM.periodList.collectAsState()

    // determine which dialog to display
    val showAccountDialog by tranVM.showAccountDialog.collectAsState()
    val showExpenseDialog by tranVM.showExpenseDialog.collectAsState()
    val showIncomeDialog by tranVM.showIncomeDialog.collectAsState()
    val showFutureDialog by tranVM.showFutureDialog.collectAsState()

    // used by DatePicker
    val dateObj = transaction.date
    val view = LocalView.current

    // displays SnackBar when saveSuccess is true
    LaunchedEffect(key1 = saveSuccess) {
        if (saveSuccess) {
            snackbarHostState.showSnackbar(saveSuccessMessage)
            tranVM.updateSaveSuccess(false)
        }
    }
    // set up AppBar actions
    appBarActionSetup(
        AppBarActions(
            onNavPressed = { tranVM.retrieveTransaction = true },
            onActionRightPressed = { tranVM.saveTransaction() }
        )
    )
    if (showFutureDialog) {
        PWAlertDialog(
            onConfirmText = stringResource(R.string.alert_dialog_yes),
            onConfirm = { tranVM.futureDialogConfirm() },
            onDismissText = stringResource(R.string.alert_dialog_no),
            onDismiss = { tranVM.futureDialogDismiss() },
            title = stringResource(R.string.alert_dialog_future_transaction),
            message = stringResource(R.string.alert_dialog_future_transaction_warning)
        )
    }
    BackPressHandler(
        onBackPressed = {
            tranVM.updateSaveSuccess(false)
            navController.navigateUp()
        }
    )
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = dimensionResource(R.dimen.cardFullPadding)),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colors.onBackground,
        elevation = dimensionResource(R.dimen.cardElevation)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = dimensionResource(R.dimen.card_content_pad))
                .verticalScroll(rememberScrollState())
        ) {
            TransactionTextInput(
                value = title,
                onValueChanged = tranVM::updateTitle,
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
                    DateUtils.datePickerDialog(view, dateObj, tranVM::onDateSelected).show()
                },
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
                value = total,
                onValueChanged = tranVM::updateTotalFieldValue,
                label = stringResource(R.string.transaction_total),
                maxLength = integerResource(
                    if (tranVM.setVals.decimalPlaces) {
                        R.integer.maxLengthTotalDecimal
                    } else {
                        R.integer.maxLengthTotalInteger
                    }
                ),
                modifier = Modifier.padding(
                    top = dimensionResource(R.dimen.textFToViewTopPadding)
                )
            )
            when (typeSelected) {
                TransactionType.EXPENSE -> {
                    TransactionCategories(
                        typeSelected = typeSelected,
                        chipExpenseOnClick = {
                            tranVM.updateTypeSelected(TransactionType.EXPENSE)
                        },
                        chipIncomeOnClick = {
                            tranVM.updateTypeSelected(TransactionType.INCOME)
                        },
                        dropDownValue = expenseCat,
                        dropDownList = expenseCatList,
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
                        chipExpenseOnClick = {
                            tranVM.updateTypeSelected(TransactionType.EXPENSE)
                        },
                        chipIncomeOnClick = {
                            tranVM.updateTypeSelected(TransactionType.INCOME)
                        },
                        dropDownValue = incomeCat,
                        dropDownList = incomeCatList,
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
            TransactionTextInput(
                value = memo,
                onValueChanged = { tranVM.updateMemo(it) },
                label = stringResource(R.string.transaction_memo),
                helper = stringResource(R.string.transaction_memo_hint),
                maxLength = integerResource(R.integer.maxLengthMemo),
                modifier = Modifier.padding(
                    top = dimensionResource(R.dimen.textFToViewTopPadding)
                )
            )
            TransactionRepeating(
                newTransaction = tranVM.tranId == 0,
                chipSelected = repeat,
                chipOnClick = { tranVM.updateRepeat(!repeat) },
                dropDownValue = period,
                dropDownList = periodList,
                dropDownOnClick = tranVM::updatePeriod,
                inputValue = frequency,
                inputOnValueChanged = tranVM::updateFrequencyFieldValue,
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
                .testTag(label)
            ,
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
                .testTag(label)
            ,
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
                InputAlertDialog(
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
                label = { Text(label)},
                trailingIcon = {
                    Icon(
                        imageVector = if (expanded) {
                            Icons.Filled.KeyboardArrowUp
                        } else {
                            Icons.Filled.KeyboardArrowDown
                        },
                        contentDescription = "Expand/Collapse",
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
 *  Composable that displays an AlertDialog with [title] that allows for user input.
 *  [onConfirm] handles user input. [onDismiss] closes the AlertDialog.
 */
@Composable
fun InputAlertDialog(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    data: DataInterface = Account(0, ""),
    onConfirm: (String) -> Unit = { },
    onConfirmData: (DataInterface, String) -> Unit = { _, _ -> }
) {
    var text by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    /**
     *  There is an AlertDialog() composable in the compose library, but I could not edit the padding.
     *  Using Dialog() allows for much more customization.
     */
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
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = dimensionResource(R.dimen.id_tf_topPad))
                            .testTag("AlertDialog input"),
                        label = { Text(text = stringResource(R.string.alert_dialog_input_hint)) },
                        trailingIcon = {
                            if (isError) {
                                Icon(
                                    imageVector = Icons.Filled.Error,
                                    contentDescription = null
                                )
                            }
                        },
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
                                .padding(horizontal = dimensionResource(R.dimen.tfh_horiPad))
                                .padding(top = dimensionResource(R.dimen.tfh_topPad))
                                .height(dimensionResource(R.dimen.tfh_height)),
                            color = MaterialTheme.colors.error,
                            style = MaterialTheme.typography.caption
                        )
                    } else {
                        Spacer(
                            modifier = Modifier
                                .padding(top = dimensionResource(R.dimen.tfh_topPad))
                                .height(dimensionResource(R.dimen.tfh_height))
                        )
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
                        onClick = {
                            if (text.isNotBlank()) {
                                isError = false
                                onConfirm(text)
                                onConfirmData(data, text)
                            } else {
                                isError = true
                            }
                        }
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
                .testTag(label)
            ,
            label = { Text(label) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colors.secondary,
                focusedLabelColor = MaterialTheme.colors.secondary
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
 *  Composable for selectable button/chip. [selected] determines if chip is selected. [onClick]
 *  is called when chip is selected. [label] is the text displayed on the chip. [showIcon]
 *  determines if a trailing icon should be displayed.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PlutusWalletButtonChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    showIcon: Boolean,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    selectedBackgroundColor: Color = Color.White,
    selectedTextColor: Color = MaterialTheme.colors.secondary,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .testTag(label),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(
            width = dimensionResource(R.dimen.button_chip_border_width),
            color = if (selected) {
                MaterialTheme.colors.secondary
            } else {
                LocalPWColors.current.unselected
            }
        ),
        colors = ChipDefaults.filterChipColors(
            backgroundColor = backgroundColor,
            selectedBackgroundColor = selectedBackgroundColor
        ),
        leadingIcon = {
            // this icon is used to center text when trailing icon exists
            if (showIcon) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "",
                    tint = MaterialTheme.colors.onSurface.copy(alpha = 0f)
                )
            }
        },
        trailingIcon = {
            if (showIcon) {
                Icon(
                    imageVector = if (selected) {
                        Icons.Filled.KeyboardArrowUp
                    } else {
                        Icons.Filled.KeyboardArrowDown
                    },
                    contentDescription = "Expand/Collapse",
                    tint = if (selected) {
                        MaterialTheme.colors.secondary
                    } else {
                        LocalPWColors.current.unselected
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
                    selectedTextColor
                } else {
                    LocalPWColors.current.unselected
                },
                style = chipTextStyle
            )
        }
    )
}

/**
 *  Composable that brings together [PlutusWalletButtonChip]s and [TransactionDropDownMenu] to handle
 *  Category selection. [typeSelected] determines which chip is currently selected, which is
 *  updated by [chipExpenseOnClick] and [chipIncomeOnClick]. [dropDownValue] is the currently
 *  selected value. [dropDownList] is the values displayed when the drop down menu is expanded.
 *  [dropDownOnClick] is called whenever a value is selected from menu. [showInputDialog]
 *  determines if [InputAlertDialog] should be displayed, which is updated by [updateInputDialog].
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
            PlutusWalletButtonChip(
                selected = typeSelected == TransactionType.EXPENSE,
                onClick = chipExpenseOnClick,
                label = stringResource(R.string.type_expense),
                showIcon = false,
                modifier = Modifier
                    .height(dimensionResource(R.dimen.transaction_button_chip_height))
                    .weight(1f)
            )
            PlutusWalletButtonChip(
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
 *  Composable that combines [PlutusWalletButtonChip], [TransactionDropDownMenu], and
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
    dropDownList: MutableList<String>,
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
        PlutusWalletButtonChip(
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