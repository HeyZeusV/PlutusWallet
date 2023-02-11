package com.heyzeusv.plutuswallet.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
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
import androidx.compose.material.Surface
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.preference.PreferenceManager
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.model.DataDialog
import com.heyzeusv.plutuswallet.data.model.SettingsValues
import com.heyzeusv.plutuswallet.ui.about.AboutScreen
import com.heyzeusv.plutuswallet.ui.list.AccountViewModel
import com.heyzeusv.plutuswallet.ui.base.BaseActivity
import com.heyzeusv.plutuswallet.ui.list.CategoryViewModel
import com.heyzeusv.plutuswallet.ui.list.ListCard
import com.heyzeusv.plutuswallet.ui.overview.ChartViewModel
import com.heyzeusv.plutuswallet.ui.overview.FilterViewModel
import com.heyzeusv.plutuswallet.ui.overview.TransactionListViewModel
import com.heyzeusv.plutuswallet.ui.overview.OverviewScreen
import com.heyzeusv.plutuswallet.ui.settings.SettingsScreen
import com.heyzeusv.plutuswallet.ui.settings.SettingsViewModel
import com.heyzeusv.plutuswallet.util.theme.LocalPWColors
import com.heyzeusv.plutuswallet.util.theme.PWDarkColors
import com.heyzeusv.plutuswallet.util.theme.PWLightColors
import com.heyzeusv.plutuswallet.util.theme.PlutusWalletColors
import com.heyzeusv.plutuswallet.util.theme.PlutusWalletTheme
import com.heyzeusv.plutuswallet.ui.transaction.TransactionScreen
import com.heyzeusv.plutuswallet.ui.transaction.TransactionViewModel
import com.heyzeusv.plutuswallet.util.AboutDestination
import com.heyzeusv.plutuswallet.util.AccountsDestination
import com.heyzeusv.plutuswallet.util.CategoriesDestination
import com.heyzeusv.plutuswallet.util.Key
import com.heyzeusv.plutuswallet.util.DataListSelectedAction.CREATE
import com.heyzeusv.plutuswallet.util.OverviewDestination
import com.heyzeusv.plutuswallet.util.PWDestination
import com.heyzeusv.plutuswallet.util.PWScreens
import com.heyzeusv.plutuswallet.util.SettingsDestination
import com.heyzeusv.plutuswallet.util.TransactionDestination
import com.heyzeusv.plutuswallet.util.TransactionType.EXPENSE
import com.heyzeusv.plutuswallet.util.TransactionType.INCOME
import com.heyzeusv.plutuswallet.util.PreferenceHelper.get
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 *  Handles the loading and replacement of fragments into their containers, as well as
 *  starting Settings/About Activities.
 */
@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private val categoryVM: CategoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val theme = sharedPref[Key.KEY_THEME, "-1"].toInt()
            val pwColors: PlutusWalletColors
            when (theme) {
                1 -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    pwColors = PWLightColors
                }
                2 -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    pwColors = PWDarkColors
                }
                else -> {
                    pwColors = if (isSystemInDarkTheme()) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        PWDarkColors
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        PWLightColors
                    }
                }
            }
            CompositionLocalProvider(LocalPWColors provides pwColors) {
                PlutusWalletTheme(
                    darkTheme = when (theme) {
                        1 -> false
                        2 -> true
                        else -> isSystemInDarkTheme()
                    }
                ) {
                    val systemUiController = rememberSystemUiController()
                    systemUiController.setSystemBarsColor(
                        color = MaterialTheme.colors.primary,
                        darkIcons = false
                    )
                    PlutusWalletApp(
                        categoryVM
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
    categoryVM: CategoryViewModel
) {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStack?.destination
    val currentScreen = PWScreens.find {
        currentDestination?.route?.contains(it.route) ?: false
    } ?: OverviewDestination
    var appBarActions by remember { mutableStateOf(AppBarActions()) }

    val activity = LocalContext.current as Activity

    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    val expenseCatList by categoryVM.expenseCatList.collectAsState()
    val incomeCatList by categoryVM.incomeCatList.collectAsState()
    val expenseCatUsedList by categoryVM.expenseCatUsedList.collectAsState()
    val incomeCatUsedList by categoryVM.incomeCatUsedList.collectAsState()
    val categoryListShowDialog by categoryVM.showDialog.collectAsState()
//    val categoryListExists by categoryVM.categoryExists.collectAsState()

    val accountListPagerState = rememberPagerState()
    val categoryListPagerState = rememberPagerState()

    val setVM: SettingsViewModel = viewModel()
    val setVals by setVM.setVals.collectAsState()

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            PWAppBar(
                currentScreen = currentScreen,
                onNavPressed = {
                    coroutineScope.launch {
                        appBarActions.onNavPressed.invoke()
                    }
                    when (currentScreen) {
                        CategoriesDestination -> categoryVM.updateCategoryExists("")
                        else -> {}
                    }
                },
                onActionLeftPressed = { appBarActions.onActionLeftPressed.invoke() },
                onActionRightPressed = {
                    appBarActions.onActionRightPressed.invoke()
                    when (currentScreen) {
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
        drawerScrimColor = LocalPWColors.current.backgroundOverlay,
        backgroundColor = MaterialTheme.colors.background
    ) {
        NavHost(
            navController = navController,
            startDestination = OverviewDestination.route
        ) {
            composable(OverviewDestination.route) {
                val tranListVM = hiltViewModel<TransactionListViewModel>().apply {
                    this.setVals = setVals
                }
                val chartVM = hiltViewModel<ChartViewModel>().apply { this.setVals = setVals }
                val filterVM = hiltViewModel<FilterViewModel>()

                tranListVM.futureTransactions()

                OverviewScreen(
                    tranListVM,
                    chartVM,
                    filterVM,
                    appBarActionSetup = { appBarActions = it },
                    showSnackbar = { msg -> scaffoldState.snackbarHostState.showSnackbar(msg) },
                    drawerState = scaffoldState.drawerState,
                    navigateToTransaction = { id -> navController.navigateToTransactionWithId(id) },
                    activityFinish = { activity.finishAndRemoveTask() }
                )
            }
            composable(
                route = TransactionDestination.routeWithArg,
                arguments = TransactionDestination.arguments
            ) { navBackStackEntry ->
                val tranVM = hiltViewModel<TransactionViewModel>().apply {
                    tranVMSetup(setVals, LocalContext.current)
                }
                val tranId =
                    navBackStackEntry.arguments?.getInt(TransactionDestination.id_arg) ?: 0

                TransactionScreen(
                    tranVM,
                    tranId,
                    appBarActionSetup = { appBarActions = it },
                    showSnackbar = { msg -> scaffoldState.snackbarHostState.showSnackbar(msg) },
                    navigateUp = { navController.navigateUp() }
                )
            }
            composable(AccountsDestination.route) {
                val accountVM = hiltViewModel<AccountViewModel>()
                ListCard(
                    viewModel = accountVM,
                    appBarActionSetup = { appBarActions = it },
                    showSnackbar = { msg -> scaffoldState.snackbarHostState.showSnackbar(msg) },
                    navigateUp = { navController.navigateUp() },
                    pagerState = accountListPagerState,
                )
            }
            composable(CategoriesDestination.route) {
//                val expenseSubtitle = stringResource(R.string.type_expense)
//                val incomeSubtitle = stringResource(R.string.type_income)
                ListCard(
                    pagerState = categoryListPagerState,
//                    snackbarHostState = scaffoldState.snackbarHostState,
                    dataLists = listOf(expenseCatList, incomeCatList),
                    usedDataLists = listOf(expenseCatUsedList, incomeCatUsedList),
                    listSubtitles = listOf(R.string.type_expense, R.string.type_income),
                    onClick = categoryVM::updateDialog,
                    showDialog = categoryListShowDialog,
                    createDialogTitle = stringResource(R.string.alert_dialog_create_category),
                    createDialogOnConfirm = categoryVM::createNewCategory,
                    deleteDialogTitle = stringResource(R.string.alert_dialog_delete_category),
                    deleteDialogOnConfirm = categoryVM::deleteCategory,
                    editDialogTitle = stringResource(R.string.alert_dialog_edit_category),
                    editDialogOnConfirm = categoryVM::editCategory,
                    dialogOnDismiss = categoryVM::updateDialog,
//                    itemExists = categoryListExists
                )
            }
            composable(SettingsDestination.route) {
                val sharedPref =
                    PreferenceManager.getDefaultSharedPreferences(LocalContext.current)
                SettingsScreen(setVM, sharedPref) { activity.recreate() }
            }
            composable(AboutDestination.route) { AboutScreen() }
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
        },
        backgroundColor = MaterialTheme.colors.primary,
        elevation = 0.dp
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
            .background(MaterialTheme.colors.primary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.mipmap.ic_launcher_foreground),
                contentDescription = stringResource(R.string.app_icon),
                modifier = Modifier
                    .scale(1.5f)
                    .padding(bottom = 8.dp)
                    ,
                tint = Color.Unspecified
            )
            Text(
                text = stringResource(R.string.app_name),
                color = MaterialTheme.colors.onBackground,
                style = MaterialTheme.typography.h4
            )
        }
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.surface)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                items(PWDrawerItems.values()) { item ->
                    PWDrawerItem(
                        onClick = {
                            when (item) {
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

/**
 *  Navigates app to [route]
 */
fun NavHostController.navigateSingleTopTo(route: String) =
    this.navigate(route) {
        // pressing back from any screen would pop back stack to Overview
        popUpTo(this@navigateSingleTopTo.graph.findStartDestination().id) { saveState = true }
        // only 1 copy of a destination is ever created
        launchSingleTop = true
        // previous data and state is saved
        restoreState = true
    }

/**
 *  Used whenever navigating to TransactionScreen, opens the Transaction screen while passing
 *  [tranId] as argument to determine which Transaction to open
 */
fun NavHostController.navigateToTransactionWithId(tranId: Int) {
    this.navigateSingleTopTo("${TransactionDestination.route}/$tranId")
}

/**
 *  Data class that is passed to each screen to determine the AppBar actions when pressed on the
 *  specified screen.
 */
data class AppBarActions(
    val onNavPressed: suspend () -> Unit = { },
    val onActionLeftPressed: () -> Unit = { },
    val onActionRightPressed: () -> Unit = { }
)

/**
 *  Update SettingsValues in TransactionViewModel with updated [sv].
 *
 *  TransactionViewModel requires several translated strings, but I don't want to have it hold
 *  context in order to get string resources. This extension function retrieves all strings
 *  required using the provided [context].
 */
private fun TransactionViewModel.tranVMSetup(sv: SettingsValues, context: Context) {
    this.apply {
        setVals = sv

        emptyTitle = context.getString(R.string.transaction_empty_title)
        accountCreate = context.getString(R.string.account_create)
        categoryCreate = context.getString(R.string.category_create)
        // array used by Period DropDownMenu
        updatePeriodList(
            mutableListOf(
                context.getString(R.string.period_days), context.getString(R.string.period_weeks),
                context.getString(R.string.period_months), context.getString(R.string.period_years)
            )
        )
    }
}