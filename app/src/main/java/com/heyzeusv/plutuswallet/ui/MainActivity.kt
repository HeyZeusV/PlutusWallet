package com.heyzeusv.plutuswallet.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.PermDeviceInformation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.model.DataDialog
import com.heyzeusv.plutuswallet.ui.account.AccountViewModel
import com.heyzeusv.plutuswallet.ui.base.BaseActivity
import com.heyzeusv.plutuswallet.ui.category.CategoryViewModel
import com.heyzeusv.plutuswallet.ui.cfl.chart.ChartViewModel
import com.heyzeusv.plutuswallet.ui.cfl.filter.FilterViewModel
import com.heyzeusv.plutuswallet.ui.cfl.tranlist.TransactionListViewModel
import com.heyzeusv.plutuswallet.ui.overview.OverviewScreen
import com.heyzeusv.plutuswallet.ui.theme.LocalPWColors
import com.heyzeusv.plutuswallet.ui.theme.PWDarkColors
import com.heyzeusv.plutuswallet.ui.theme.PWLightColors
import com.heyzeusv.plutuswallet.ui.theme.PlutusWalletTheme
import com.heyzeusv.plutuswallet.ui.transaction.DataListSelectedAction.CREATE
import com.heyzeusv.plutuswallet.ui.transaction.FilterState
import com.heyzeusv.plutuswallet.ui.transaction.TransactionScreen
import com.heyzeusv.plutuswallet.ui.transaction.TransactionType.EXPENSE
import com.heyzeusv.plutuswallet.ui.transaction.TransactionType.INCOME
import com.heyzeusv.plutuswallet.ui.transaction.TransactionViewModel
import com.heyzeusv.plutuswallet.util.Key
import com.heyzeusv.plutuswallet.util.PreferenceHelper.get
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 *  Handles the loading and replacement of fragments into their containers, as well as
 *  starting Settings/About Activities.
 */
@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private val filterVM: FilterViewModel by viewModels()
    private val tranVM: TransactionViewModel by viewModels()
    private val accountVM: AccountViewModel by viewModels()
    private val categoryVM: CategoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tranVM.apply {
            emptyTitle = getString(R.string.transaction_empty_title)
            accountCreate = getString(R.string.account_create)
            categoryCreate = getString(R.string.category_create)
            // array used by Period DropDownMenu
            updatePeriodList(
                mutableListOf(
                    getString(R.string.period_days), getString(R.string.period_weeks),
                    getString(R.string.period_months), getString(R.string.period_years)
                )
            )
        }

        setContent {
            val pwColors = when (sharedPref[Key.KEY_THEME, "-1"].toInt()) {
                1 -> PWLightColors
                2 -> PWDarkColors
                else -> if (isSystemInDarkTheme()) PWDarkColors else PWLightColors
            }
            CompositionLocalProvider(LocalPWColors provides pwColors) {
                PlutusWalletTheme {
                    PlutusWalletApp(
                        sharedPref,
                        filterVM,
                        tranVM,
                        accountVM,
                        categoryVM,
                        recreateActivity = { recreate() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun PlutusWalletApp(
    sharedPref: SharedPreferences,
    filterVM: FilterViewModel,
    tranVM: TransactionViewModel,
    accountVM: AccountViewModel,
    categoryVM: CategoryViewModel,
    recreateActivity: () -> Unit
) {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStack?.destination
    val currentScreen =
        PWScreens.find { it.route == currentDestination?.route } ?: OverviewDestination
    val activity = LocalContext.current as Activity

    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    val filterInfo by filterVM.filterInfo.collectAsState()

    val showFilter by filterVM.showFilter.collectAsState()
    val accountFilterSelected by filterVM.accountFilter.collectAsState()
    val accountNameList by filterVM.accountList.collectAsState()
    val accountSelected by filterVM.accountSelected.collectAsState()
    val categoryFilterSelected by filterVM.categoryFilter.collectAsState()
    val filterTypeSelected by filterVM.typeSelected.collectAsState()
    val expenseCatNameList by filterVM.expenseCatList.collectAsState()
    val expenseCatSelected by filterVM.expenseCatSelected.collectAsState()
    val incomeCatNameList by filterVM.incomeCatList.collectAsState()
    val incomeCatSelected by filterVM.incomeCatSelected.collectAsState()
    val dateFilterSelected by filterVM.dateFilter.collectAsState()
    val startDateString by filterVM.startDateString.collectAsState()
    val endDateString by filterVM.endDateString.collectAsState()
    val filterState by filterVM.filterState.collectAsState()
    val filterStateMessage = stringResource(filterState.stringId)

    val accountList by accountVM.accountList.collectAsState()
    val accountsUsedList by accountVM.accountsUsedList.collectAsState()
    val accountListShowDialog by accountVM.showDialog.collectAsState()
    val accountListExistsName by accountVM.accountExists.collectAsState()

    val expenseCatList by categoryVM.expenseCatList.collectAsState()
    val incomeCatList by categoryVM.incomeCatList.collectAsState()
    val expenseCatUsedList by categoryVM.expenseCatUsedList.collectAsState()
    val incomeCatUsedList by categoryVM.incomeCatUsedList.collectAsState()
    val categoryListShowDialog by categoryVM.showDialog.collectAsState()
    val categoryListExists by categoryVM.categoryExists.collectAsState()

    val accountListPagerState = rememberPagerState()
    val categoryListPagerState = rememberPagerState()

    val setVM: SettingsViewModel = viewModel()
    val setVals by setVM.setVals.collectAsState()

    PlutusWalletTheme {
        LaunchedEffect(key1 = filterState) {
            if (filterState != FilterState.VALID) {
                scaffoldState.snackbarHostState.showSnackbar(filterStateMessage)
                filterVM.updateFilterState(FilterState.VALID)
            }
        }
        BackPressHandler(
            onBackPressed =  {
                if (scaffoldState.drawerState.isOpen) {
                    coroutineScope.launch { scaffoldState.drawerState.close() }
                } else if (!navController.navigateUp()) {
                    activity.finish()
                }
            }
        )
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                PWAppBar(
                    currentScreen = currentScreen,
                    onNavPressed = {
                        if (currentScreen == OverviewDestination) {
                            coroutineScope.launch {
                                scaffoldState.drawerState.open()
                            }
                        } else {
                            when (currentScreen) {
                                TransactionDestination -> tranVM.updateSaveSuccess(false)
                                AccountsDestination -> accountVM.updateAccountExists("")
                                CategoriesDestination -> categoryVM.updateCategoryExists("")
                                else -> {}
                            }
                            navController.navigateUp()
                        }
                    },
                    onActionLeftPressed = {
                        when (currentScreen) {
                            OverviewDestination -> { filterVM.updateShowFilter(!showFilter) }
                            else -> {}
                        }
                    },
                    onActionRightPressed = {
                        when (currentScreen) {
                            OverviewDestination -> {
                                tranVM.retrieveTransaction(0)
                                navController.navigateSingleTopTo(TransactionDestination.route)
                            }
                            TransactionDestination -> { tranVM.saveTransaction() }
                            AccountsDestination -> { accountVM.updateDialog(DataDialog(CREATE, 0)) }
                            CategoriesDestination -> {
                                val type =
                                    if (categoryListPagerState.currentPage == 0) EXPENSE else INCOME
                                categoryVM.updateDialog(DataDialog(CREATE, 0, type))
                            }
                            else -> {}
                        }
                    }
                )
            },
            // TODO: Make PWDrawer a child composable that opens/closes using AnimateVisibility
            drawerContent = {
                PWDrawer(
                    closeDrawer = {
                        coroutineScope.launch {
                            scaffoldState.drawerState.close()
                        }
                    },
                    navController
                )
            },
            drawerGesturesEnabled = false,
            backgroundColor = MaterialTheme.colors.background
        ) {
            NavHost(
                navController = navController,
                startDestination = OverviewDestination.route
            ) {
                composable(OverviewDestination.route) {
                    val tranListVM = hiltViewModel<TransactionListViewModel>()
                    val chartVM = hiltViewModel<ChartViewModel>()

                    val tranListShowDeleteDialog by tranListVM.showDeleteDialog.collectAsState()
                    tranListVM.futureTransactions()

                    OverviewScreen(
                        filterInfo,
                        setVals,
                        tranListVM,
                        chartVM,
                        tranListPreviousMaxId = tranListVM.previousMaxId,
                        tranListUpdatePreviousMaxId = tranListVM::updatePreviousMaxId,
                        tranListItemOnLongClick = tranListVM::updateDeleteDialog,
                        tranListItemOnClick = { tranId ->
                            tranVM.retrieveTransaction(tranId)
                            navController.navigateSingleTopTo(TransactionDestination.route)
                        },
                        tranListShowDeleteDialog = tranListShowDeleteDialog,
                        tranListDialogOnConfirm = { tranId ->
                            tranListVM.deleteTransaction(tranId)
                            tranListVM.updateDeleteDialog(-1)
                        },
                        tranListDialogOnDismiss = { tranListVM.updateDeleteDialog(-1) },
                        showFilter = showFilter,
                        updateShowFilter = filterVM::updateShowFilter,
                        accountFilterSelected = accountFilterSelected,
                        accountFilterOnClick = filterVM::updateAccountFilter,
                        accountNameList,
                        accountSelected,
                        accountChipOnClick = filterVM::updateAccountSelected,
                        categoryFilterSelected = categoryFilterSelected,
                        categoryFilterOnClick = filterVM::updateCategoryFilter,
                        filterTypeSelected,
                        filterUpdateTypeSelected = filterVM::updateTypeSelected,
                        categoryList = if (filterTypeSelected == EXPENSE)
                            expenseCatNameList else incomeCatNameList,
                        categorySelected = if (filterTypeSelected == EXPENSE)
                            expenseCatSelected else incomeCatSelected,
                        categoryChipOnClick = if (filterTypeSelected == EXPENSE)
                            filterVM::updateExpenseCatSelected else filterVM::updateIncomeCatSelected,
                        dateFilterSelected,
                        dateFilterOnClick = filterVM::updateDateFilter,
                        startDateString,
                        startDateOnClick = filterVM::updateStartDateString,
                        endDateString,
                        endDateOnClick = filterVM::updateEndDateString,
                        applyOnClick = filterVM::applyFilter
                    )
                }
                composable(route = TransactionDestination.route) {
                    TransactionScreen(
                        tranVM = tranVM,
                        snackbarHostState = scaffoldState.snackbarHostState,
                        navController = navController
                    )
                }
                composable(AccountsDestination.route){
                    ListCard(
                        pagerState = accountListPagerState,
                        snackbarHostState = scaffoldState.snackbarHostState,
                        dataLists = listOf(accountList),
                        usedDataLists = listOf(accountsUsedList),
                        onClick = accountVM::updateDialog,
                        showDialog = accountListShowDialog,
                        createDialogTitle = stringResource(R.string.alert_dialog_create_account),
                        createDialogOnConfirm = accountVM::createNewAccount,
                        deleteDialogTitle = stringResource(R.string.alert_dialog_delete_account),
                        deleteDialogOnConfirm = accountVM::deleteAccount,
                        editDialogTitle = stringResource(R.string.alert_dialog_edit_account),
                        editDialogOnConfirm = accountVM::editAccount,
                        dialogOnDismiss = accountVM::updateDialog,
                        existsName = accountListExistsName
                    )
                }
                composable(CategoriesDestination.route) {
                    val expenseSubtitle = stringResource(R.string.type_expense)
                    val incomeSubtitle = stringResource(R.string.type_income)
                    ListCard(
                        pagerState = categoryListPagerState,
                        snackbarHostState = scaffoldState.snackbarHostState,
                        dataLists = listOf(expenseCatList, incomeCatList),
                        usedDataLists = listOf(expenseCatUsedList, incomeCatUsedList),
                        listSubtitles = listOf(expenseSubtitle, incomeSubtitle),
                        onClick = categoryVM::updateDialog,
                        showDialog = categoryListShowDialog,
                        createDialogTitle = stringResource(R.string.alert_dialog_create_category),
                        createDialogOnConfirm = categoryVM::createNewCategory,
                        deleteDialogTitle = stringResource(R.string.alert_dialog_delete_category),
                        deleteDialogOnConfirm = categoryVM::deleteCategory,
                        editDialogTitle = stringResource(R.string.alert_dialog_edit_category),
                        editDialogOnConfirm = categoryVM::editCategory,
                        dialogOnDismiss = categoryVM::updateDialog,
                        existsName = categoryListExists
                    )
                }
                composable(SettingsDestination.route) {
                    SettingsScreen(setVM, sharedPref, recreateActivity)
                }
                composable(AboutDestination.route) { AboutScreen() }
            }
        }
    }
}

@Composable
fun PWAppBar(
    currentScreen: PWDestination,
    onNavPressed: () -> Unit,
    onActionLeftPressed: () -> Unit,
    onActionRightPressed: () -> Unit,
) {
    val actionLeftDescription = stringResource(currentScreen.actionLeftDescription)
    val actionRightDescription = stringResource(currentScreen.actionRightDescription)
    val title = stringResource(currentScreen.title)

    TopAppBar(
        title = {
            Text(
                text = title,
                modifier = Modifier.testTag("AppBar $title"),
                color = MaterialTheme.colors.onBackground
            )
        },
        navigationIcon = {
            IconButton(onClick = { onNavPressed() }) {
                Icon(
                    imageVector = currentScreen.navIcon,
                    contentDescription = stringResource(currentScreen.navDescription),
                    tint = MaterialTheme.colors.onBackground
                )
            }
        },
        actions = {
            if (actionLeftDescription.isNotBlank()) {
                IconButton(onClick = { onActionLeftPressed() }) {
                    Icon(
                        imageVector = currentScreen.actionLeftIcon,
                        contentDescription = actionLeftDescription,
                        tint = MaterialTheme.colors.onBackground
                    )
                }
            }
            if (actionRightDescription.isNotBlank()) {
                IconButton(onClick = { onActionRightPressed() }) {
                    Icon(
                        imageVector = currentScreen.actionRightIcon,
                        contentDescription = actionRightDescription,
                        tint = MaterialTheme.colors.onBackground
                    )
                }
            }
        }
    )
}

@Preview
@Composable
fun PWAppBarPreview() {
    PlutusWalletTheme {
        PWAppBar(
            currentScreen = OverviewDestination,
            onNavPressed = { },
            onActionLeftPressed = { },
            onActionRightPressed = { },
        )
    }
}

/**
 *  Might have to remove later, depending on Navigation
 */
enum class PWDrawerItems(val icon: ImageVector, val labelId: Int) {
    ACCOUNTS(Icons.Filled.AccountBalance, R.string.accounts),
    CATEGORIES(Icons.Filled.Category, R.string.categories),
    SETTINGS(Icons.Filled.Settings, R.string.settings),
    ABOUT(Icons.Filled.PermDeviceInformation, R.string.about)
}

@Composable
fun PWDrawer(
    closeDrawer: () -> Unit,
    navController: NavHostController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.onBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.primary)
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.mipmap.ic_launcher_foreground),
                contentDescription = stringResource(R.string.app_icon),
                modifier = Modifier
                    .scale(1.5f)
                    .padding(bottom = 8.dp),
                tint = Color.Unspecified
            )
            Text(
                text = stringResource(R.string.app_name),
                color = MaterialTheme.colors.onBackground,
                style = MaterialTheme.typography.h4
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            items(PWDrawerItems.values()) { item ->
                PWDrawerItem(
                    onClick = {
                        when(item) {
                            PWDrawerItems.ACCOUNTS -> {
                                closeDrawer()
                                navController.navigateSingleTopTo(AccountsDestination.route)
                            }
                            PWDrawerItems.CATEGORIES -> {
                                closeDrawer()
                                navController.navigateSingleTopTo(CategoriesDestination.route)
                            }
                            PWDrawerItems.ABOUT -> {
                                closeDrawer()
                                navController.navigateSingleTopTo(AboutDestination.route)
                            }
                            PWDrawerItems.SETTINGS -> {
                                closeDrawer()
                                navController.navigateSingleTopTo(SettingsDestination.route)
                            }
                        }
                    },
                    icon = item.icon,
                    label = stringResource(item.labelId)
                )
            }
        }
    }
}

@Preview
@Composable
fun PWDrawerPreview() {
    val previewNavHostController = rememberNavController()
    PlutusWalletTheme {
        PWDrawer(
            {},
            previewNavHostController
        )
    }
}

@Composable
fun PWDrawerItem(
    onClick: () -> Unit,
    icon: ImageVector,
    label: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 16.dp)
            .clickable { onClick() }
            .testTag("DrawerItem $label"),
        horizontalArrangement = Arrangement.spacedBy(
            space = 8.dp
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        Text(
            text = label,
            modifier = Modifier.align(Alignment.CenterVertically),
            style = MaterialTheme.typography.body2
        )
    }
}

@Preview
@Composable
fun PWDrawerItemPreview() {
    PlutusWalletTheme {
        PWDrawerItem(
            onClick = { /*TODO*/ },
            icon = Icons.Filled.AccountBalance,
            label = stringResource(R.string.accounts)
        )
    }
}

/**
 *  Executes [onBackPressed] whenever bottom navigation back button is pressed
 *  Found here: [https://www.valueof.io/blog/intercept-back-press-button-in-jetpack-compose]
 */
@Composable
fun BackPressHandler(
    backPressedDispatcher: OnBackPressedDispatcher? =
        LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher,
    onBackPressed: () -> Unit
) {
    val backCallback = remember {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        }
    }

    // add the callback on any back press and remove on dispose.
    DisposableEffect(key1 = backPressedDispatcher) {
        backPressedDispatcher?.addCallback(backCallback)
        onDispose { backCallback.remove() }
    }
}

fun NavHostController.navigateSingleTopTo(route: String) =
    this.navigate(route) {
        // pressing back from any screen would pop back stack to Overview
        popUpTo(this@navigateSingleTopTo.graph.findStartDestination().id) { saveState = true }
        // only 1 copy of a destination is ever created
        launchSingleTop = true
        // previous data and state is saved
        restoreState = true
    }