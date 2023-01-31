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
 *  [route] is used by Navigation to determine which screen to display. The rest of the parameters
 *  are used to determine TopAppBar content. [title] is the string resource id to be displayed.
 *  [navIcon] and [navDescription] are used for the navigate action to the left of the title.
 *  The TopAppBar will at most have 2 actions to the right of the title which [actionLeftIcon],
 *  [actionLeftDescription], [actionRightIcon], and [actionRightDescription] correspond to.
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
object OverviewDestination: PWDestination {
    override val route = "overview"
    override val title = R.string.cfl_overview
    override val navIcon = Icons.Filled.Menu
    override val navDescription = R.string.cfl_drawer_description
    override val actionLeftIcon = Icons.Filled.FilterAlt
    override val actionLeftDescription = R.string.cfl_menu_filter
    override val actionRightIcon = Icons.Filled.Add
    override val actionRightDescription = R.string.cfl_menu_transaction
}

object TransactionDestination: PWDestination {
    override val route = "transaction"
    override val title = R.string.transaction
    override val actionRightIcon = Icons.Filled.Save
    override val actionRightDescription = R.string.transaction_save

    const val id_arg = "transaction_id"
    val arguments = listOf(
        navArgument(id_arg) { type = NavType.IntType }
    )
    val routeWithArg = "${route}/{${id_arg}}"
}

object AccountsDestination: PWDestination {
    override val route = "accounts"
    override val title = R.string.accounts
    override val actionRightIcon = Icons.Filled.Add
    override val actionRightDescription = R.string.account_new
}

object CategoriesDestination: PWDestination {
    override val route = "categories"
    override val title = R.string.categories
    override val actionRightIcon = Icons.Filled.Add
    override val actionRightDescription = R.string.category_new
}

object SettingsDestination: PWDestination {
    override val route = "settings"
    override val title = R.string.settings
}

object AboutDestination: PWDestination {
    override val route = "about"
    override val title = R.string.about
}

val PWScreens = listOf(
    OverviewDestination, TransactionDestination, AccountsDestination,
    CategoriesDestination, SettingsDestination, AboutDestination
)

val Blank: ImageVector get() = materialIcon(name = "Filled.Blank") { materialPath { } }