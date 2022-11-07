package com.heyzeusv.plutuswallet.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PermDeviceInformation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.databinding.ActivityMainBinding
import com.heyzeusv.plutuswallet.ui.base.BaseActivity
import com.heyzeusv.plutuswallet.ui.cfl.tranlist.TransactionListViewModel
import com.heyzeusv.plutuswallet.ui.theme.PlutusWalletTheme
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

    // DataBinding
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(sharedPref[Key.KEY_THEME, "-1"].toInt())

        // setting up DataBinding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        // disables swipe to open drawer
        binding.activityDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    override fun onStart() {
        super.onStart()

        // uses nav_graph to determine where each button goes from NavigationView
        binding.activityNavView.setupWithNavController(findNavController(R.id.fragment_container))
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

    override fun onBackPressed() {

        if (binding.activityDrawer.isDrawerOpen(GravityCompat.START)) {
            // close drawer if it is open
            binding.activityDrawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MainComposable(
    tranListVM: TransactionListViewModel
) {
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    /**
     *  TODO: Convert tInfoLiveData from CFLViewModel to StateFlow, whenever the filter is updated
     *  TODO: tInfoLiveData will get updated as well causing tranList to be updated.
     */
    val tranList by tranListVM.tranList.collectAsState()

    PlutusWalletTheme {
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                PWAppBar(
                    title = stringResource(R.string.cfl_overview),
                    onNavPressed = {
                        coroutineScope.launch {
                            scaffoldState.drawerState.open()
                        }
                    },
                    navIcon = Icons.Filled.Menu,
                    navDescription = stringResource(R.string.cfl_drawer_description),
                    onActionLeftPressed = { /*TODO*/ },
                    actionLeftIcon = Icons.Filled.FilterAlt,
                    actionLeftDescription = stringResource(R.string.cfl_menu_filter),
                    onActionRightPressed = { /*TODO*/ },
                    actionRightIcon = Icons.Filled.Add,
                    actionRightDescription = stringResource(R.string.cfl_menu_transaction)
                )
            },
            drawerContent = {
                PWDrawer()
            },
            backgroundColor = MaterialTheme.colors.background
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimensionResource(R.dimen.cardFullPadding))
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.cfl_no_transactions),
                        textAlign = TextAlign.Center

                    )
                }
            }
        }
    }
}

@Composable
fun PWAppBar(
    title: String,
    onNavPressed: () -> Unit,
    navIcon: ImageVector,
    navDescription: String,
    onActionLeftPressed: () -> Unit,
    actionLeftIcon: ImageVector,
    actionLeftDescription: String,
    onActionRightPressed: () -> Unit,
    actionRightIcon: ImageVector,
    actionRightDescription: String
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                color = MaterialTheme.colors.onBackground
            )
        },
        navigationIcon = {
            IconButton(onClick = { onNavPressed() }) {
                Icon(
                    imageVector = navIcon,
                    contentDescription = navDescription,
                    tint = MaterialTheme.colors.onBackground
                )
            }
        },
        actions = {
            if (actionLeftDescription.isNotBlank()) {
                IconButton(onClick = { onActionLeftPressed() }) {
                    Icon(
                        imageVector = actionLeftIcon,
                        contentDescription = actionLeftDescription,
                        tint = MaterialTheme.colors.onBackground
                    )
                }
            }
            IconButton(onClick = { onActionRightPressed() }) {
                Icon(
                    imageVector = actionRightIcon,
                    contentDescription = actionRightDescription,
                    tint = MaterialTheme.colors.onBackground
                )
            }
        }
    )
}

@Preview
@Composable
fun PWAppBarPreview() {
    PlutusWalletTheme {
        PWAppBar(
            title = "Preview",
            onNavPressed = { },
            navIcon = Icons.Filled.Menu,
            navDescription = "Menu",
            onActionLeftPressed = { },
            actionLeftIcon = Icons.Filled.FilterAlt,
            actionLeftDescription = "Filter",
            onActionRightPressed = { },
            actionRightIcon = Icons.Filled.Add,
            actionRightDescription = "New"
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
    PWDrawerItem(
        onClick = { /*TODO*/ },
        icon = Icons.Filled.AccountBalance,
        label = stringResource(R.string.accounts)
    )
}