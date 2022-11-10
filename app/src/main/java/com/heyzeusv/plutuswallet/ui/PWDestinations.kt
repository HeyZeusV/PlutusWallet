package com.heyzeusv.plutuswallet.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.heyzeusv.plutuswallet.R

/**
 *  Contract for information needed on every navigation destination.
 */
interface PWDestination {
    val route: String
    val title: Int
    val navIcon: ImageVector get() = Icons.Filled.ArrowBack
    val navDescription: Int get() = R.string.navigate_back
    val actionLeftIcon: ImageVector get() = Blank
    val actionLeftDescription: Int get() = R.string.blank_string
    val actionRightIcon: ImageVector get() = Blank
    val actionRightDescription: Int get() = R.string.blank_string
}

/**
 *  Navigation destinations.
 */
object Overview: PWDestination {
    override val route = "overview"
    override val title = R.string.cfl_overview
    override val navIcon = Icons.Filled.Menu
    override val navDescription = R.string.cfl_drawer_description
    override val actionLeftIcon = Icons.Filled.FilterAlt
    override val actionLeftDescription = R.string.cfl_menu_filter
    override val actionRightIcon = Icons.Filled.Add
    override val actionRightDescription = R.string.cfl_menu_transaction
}

object Transaction: PWDestination {
    const val tranIdArg = "tranId"
    const val routePrefix = "transaction"
    override val route = "${routePrefix}/{${tranIdArg}}"
    override val title = R.string.transaction_title
    override val actionRightIcon = Icons.Filled.Save
    override val actionRightDescription = R.string.transaction_save

    val arguments = listOf(
        navArgument(tranIdArg) { type = NavType.IntType }
    )
}

object Accounts: PWDestination {
    override val route = "accounts"
    override val title = R.string.accounts
    override val actionRightIcon = Icons.Filled.Add
    override val actionRightDescription = R.string.account_new
}

object Categories: PWDestination {
    override val route = "categories"
    override val title = R.string.categories
    override val actionRightIcon = Icons.Filled.Add
    override val actionRightDescription = R.string.category_new
}

object Settings: PWDestination {
    override val route = "settings"
    override val title = R.string.settings
}

object About: PWDestination {
    override val route = "about"
    override val title = R.string.about
}

val PWScreens = listOf(Overview, Transaction, Accounts, Categories, Settings, Accounts)

val Blank: ImageVector get() = materialIcon(name = "Filled.Blank") { materialPath { } }