package com.heyzeusv.plutuswallet.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.ui.base.BaseActivity
import com.heyzeusv.plutuswallet.ui.cfl.CFLViewModel
import com.heyzeusv.plutuswallet.ui.cfl.tranlist.TransactionListViewModel
import com.heyzeusv.plutuswallet.ui.overview.OverviewScreen
import com.heyzeusv.plutuswallet.ui.theme.LocalPWColors
import com.heyzeusv.plutuswallet.ui.theme.PWDarkColors
import com.heyzeusv.plutuswallet.ui.theme.PWLightColors
import com.heyzeusv.plutuswallet.ui.theme.PlutusWalletTheme
import com.heyzeusv.plutuswallet.ui.transaction.TransactionScreen
import com.heyzeusv.plutuswallet.ui.transaction.TransactionViewModel
import com.heyzeusv.plutuswallet.util.Key
import com.heyzeusv.plutuswallet.util.PreferenceHelper.get
import com.heyzeusv.plutuswallet.util.PreferenceHelper.set
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 *  Handles the loading and replacement of fragments into their containers, as well as
 *  starting Settings/About Activities.
 */
@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private val tranListVM: TransactionListViewModel by viewModels()
    private val tranVM: TransactionViewModel by viewModels()

    // shared ViewModels
    private val cflVM: CFLViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tranVM.apply {
            emptyTitle = getString((R.string.transaction_empty_title))
            // array used by Period DropDownMenu
            updatePeriodList(
                mutableListOf(
                    getString(R.string.period_days), getString(R.string.period_weeks),
                    getString(R.string.period_months), getString(R.string.period_years)
                )
            )
            prepareLists(getString(R.string.account_create), getString(R.string.category_create))
        }

        AppCompatDelegate.setDefaultNightMode(sharedPref[Key.KEY_THEME, "-1"].toInt())

        setContent {
            val pwColors = if (isSystemInDarkTheme()) PWDarkColors else PWLightColors
            CompositionLocalProvider(LocalPWColors provides pwColors) {
                PlutusWalletTheme {
                    PlutusWalletApp(
                        tranListVM = tranListVM,
                        cflVM = cflVM,
                        tranVM = tranVM
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // loads if view mode changed
        val themeChanged: Boolean = sharedPref[Key.KEY_THEME_CHANGED, false]
        if (themeChanged) {
            sharedPref[Key.KEY_THEME_CHANGED] = false
            // destroys then restarts Activity in order to have updated theme
            recreate()
        }

        // loads if language changed
        val languageChanged: Boolean = sharedPref[Key.KEY_LANGUAGE_CHANGED, false]
        if (languageChanged) {
            // saving into SharedPreferences
            sharedPref[Key.KEY_LANGUAGE_CHANGED] = false
            // destroys then restarts Activity in order to have updated language
            recreate()
        }
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun PlutusWalletApp(
    tranListVM: TransactionListViewModel,
    cflVM: CFLViewModel,
    tranVM: TransactionViewModel
) {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStack?.destination
    val currentScreen = PWScreens.find { it.route == currentDestination?.route } ?: OverviewDestination
    val activity = LocalContext.current as Activity

    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    /**
     *  TODO: Convert tInfoLiveData from CFLViewModel to StateFlow, whenever the filter is updated
     *  TODO: tInfoLiveData will get updated as well causing tranList to be updated.
     */
    val filterInfo by cflVM.filterInfo.collectAsState()
    val tranList by tranListVM.tranList.collectAsState()
    val tranListShowDeleteDialog by tranListVM.showDeleteDialog.collectAsState()

    PlutusWalletTheme {
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
                            navController.navigateUp()
                            // TODO make this universal for all Snackbars
                            tranVM.updateSaveSuccess(false)
                        }
                    },
                    onActionLeftPressed = {},
                    onActionRightPressed = {
                        when (currentScreen) {
                            OverviewDestination -> {
                                tranVM.retrieveTransaction(0)
                                navController.navigateSingleTopTo(TransactionDestination.route)
                            }
                            TransactionDestination -> { tranVM.saveTransaction() }
                            AccountsDestination -> {}
                            CategoriesDestination -> {}
                        }
                    }
                )
            },
            drawerContent = { PWDrawer() },
            drawerGesturesEnabled = false,
            backgroundColor = MaterialTheme.colors.background
        ) {
            NavHost(
                navController = navController,
                startDestination = OverviewDestination.route
            ) {
                composable(OverviewDestination.route) {
                    tranListVM.futureTransactions()
                    OverviewScreen(
                        tranListVM = tranListVM,
                        tranList = tranList,
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
                    )
                }
                composable(route = TransactionDestination.route) {
                    TransactionScreen(
                        tranVM = tranVM,
                        snackbarHostState = scaffoldState.snackbarHostState,
                        navController = navController
                    )
                }
                composable(AccountsDestination.route){ }
                composable(CategoriesDestination.route) { }
                composable(SettingsDestination.route) { }
                composable(AboutDestination.route) { }
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

    TopAppBar(
        title = {
            Text(
                text = stringResource(currentScreen.title),
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
fun PWDrawer() {
    Icons.Filled.AccountBalance
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
                    onClick = { /*TODO*/ },
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
    PlutusWalletTheme {
        PWDrawer()
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
            .padding(
                horizontal = 16.dp,
                vertical = 16.dp
            ),
        horizontalArrangement = Arrangement.spacedBy(
            space = 8.dp
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label
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