package com.heyzeusv.plutuswallet.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.heyzeusv.plutuswallet.R
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
import com.heyzeusv.plutuswallet.util.theme.PlutusWalletTheme
import com.heyzeusv.plutuswallet.ui.transaction.TransactionCard
import com.heyzeusv.plutuswallet.ui.transaction.TransactionViewModel
import com.heyzeusv.plutuswallet.ui.transaction.tranVMSetup
import com.heyzeusv.plutuswallet.util.AboutDestination
import com.heyzeusv.plutuswallet.util.AccountsDestination
import com.heyzeusv.plutuswallet.util.AppBarActions
import com.heyzeusv.plutuswallet.util.CategoriesDestination
import com.heyzeusv.plutuswallet.util.Key
import com.heyzeusv.plutuswallet.util.OverviewDestination
import com.heyzeusv.plutuswallet.util.PWDestination
import com.heyzeusv.plutuswallet.util.PWDrawerItems
import com.heyzeusv.plutuswallet.util.PWScreens
import com.heyzeusv.plutuswallet.util.SettingsDestination
import com.heyzeusv.plutuswallet.util.TransactionDestination
import com.heyzeusv.plutuswallet.util.PreferenceHelper.get
import com.heyzeusv.plutuswallet.util.navigateSingleTopTo
import com.heyzeusv.plutuswallet.util.navigateToTransactionWithId
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val theme = sharedPref[Key.KEY_THEME, "-1"].toInt()
            PlutusWalletTheme(theme) {
                // set status bar color
                val systemUiController = rememberSystemUiController()
                systemUiController.setSystemBarsColor(
                    color = MaterialTheme.colors.primary,
                    darkIcons = false
                )
                PlutusWalletApp()
            }
        }
    }
}

/**
 *  Main Composable of entire app. Contains a Scaffold which handles AppBar and Drawer. Its content
 *  is a NavHost which handles which Screen Composables to display.
 */
@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun PlutusWalletApp(
    navController: NavHostController = rememberNavController()
) {
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStack?.destination
    val currentScreen = PWScreens.find {
        currentDestination?.route?.contains(it.route) ?: false
    } ?: OverviewDestination

    var appBarActions by remember { mutableStateOf(AppBarActions()) }

    val activity = LocalContext.current as Activity

    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    val accountListPagerState = rememberPagerState()
    val categoryListPagerState = rememberPagerState()

    val setVM: SettingsViewModel = hiltViewModel()
    val setVals by setVM.setVals.collectAsState()

    Scaffold(
        modifier = Modifier.testTag(stringResource(R.string.tt_app_scaffold)),
        scaffoldState = scaffoldState,
        topBar = {
            PWAppBar(
                currentScreen = currentScreen,
                onNavPressed = { coroutineScope.launch { appBarActions.onNavPressed.invoke() }},
                onActionLeftPressed = { appBarActions.onActionLeftPressed.invoke() },
                onActionRightPressed = { appBarActions.onActionRightPressed.invoke() }
            )
        },
        // TODO: Make PWDrawer a child composable that opens/closes using AnimateVisibility
        drawerContent = {
            PWDrawer(
                closeDrawer = { coroutineScope.launch { scaffoldState.drawerState.close() }},
                navigateTo = { navController.navigateSingleTopTo(it) }
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

                TransactionCard(
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
                val categoryVM = hiltViewModel<CategoryViewModel>()
                ListCard(
                    viewModel = categoryVM,
                    appBarActionSetup = { appBarActions = it },
                    showSnackbar = { msg -> scaffoldState .snackbarHostState.showSnackbar(msg) },
                    navigateUp = { navController.navigateUp() },
                    pagerState = categoryListPagerState
                )
            }
            composable(SettingsDestination.route) {
                SettingsScreen(
                    setVM,
                    appBarActionSetup = { appBarActions = it },
                    navigateUp = { navController.navigateUp() },
                    recreateActivity = { activity.recreate() }
                )
            }
            composable(AboutDestination.route) {
                AboutScreen(
                    appBarActionSetup = { appBarActions = it },
                    navigateUp = { navController.navigateUp() }
                )
            }
        }
    }
}

/**
 *  Composable for top app bar that is displayed throughout the app. [currentScreen] provides
 *  various information: title, icons/descriptions for navigation, left, and right actions.
 *  [onNavPressed] handles action when navigation button is pressed. [onActionLeftPressed] handles
 *  action when left action button is pressed. [onActionRightPressed] handles action when right
 *  action button is pressed.
 */
@Composable
fun PWAppBar(
    currentScreen: PWDestination,
    onNavPressed: () -> Unit,
    onActionLeftPressed: () -> Unit,
    onActionRightPressed: () -> Unit,
) {
    // retrieve string resources
    val actionLeftDescription = stringResource(currentScreen.actionLeftDescription)
    val actionRightDescription = stringResource(currentScreen.actionRightDescription)
    val title = stringResource(currentScreen.title)

    TopAppBar(
        title = {
            Text(
                text = title,
                modifier = Modifier.testTag(stringResource(R.string.tt_app_barTitle, title)),
                color = MaterialTheme.colors.onBackground
            )
        },
        modifier = Modifier.testTag(stringResource(R.string.tt_app_bar)),
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

/**
 *  Composable for Drawer, which is displayed when navigation button on top app bar is pressed on
 *  Overview screen. [closeDrawer] allows for closure of Drawer when item is pressed without having
 *  to pass DrawerState. [navigateTo] allows for navigation to various screens depending on
 *  [PWDestination.route] passed.
 */
@Composable
fun PWDrawer(
    closeDrawer: () -> Unit,
    navigateTo: (String) -> Unit
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
                    .padding(bottom = 8.dp),
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
                            closeDrawer()
                            navigateTo(item.route)
                        },
                        icon = item.icon,
                        label = stringResource(item.labelId)
                    )
                }
            }
        }
    }
}

/**
 *  Composable for individual Drawer item. [onClick] determines action when item is pressed. [icon]
 *  represents the screen item is for. [label] is name of the screen item is for.
 */
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
            .testTag(stringResource(R.string.tt_app_drawItem, label)),
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

@Preview
@Composable
fun PWDrawerPreview() {
    PlutusWalletTheme {
        PWDrawer({ }, { })
    }
}

@Preview
@Composable
fun PWDrawerItemPreview() {
    PlutusWalletTheme {
        PWDrawerItem(
            onClick = { },
            icon = Icons.Filled.AccountBalance,
            label = stringResource(R.string.accounts)
        )
    }
}