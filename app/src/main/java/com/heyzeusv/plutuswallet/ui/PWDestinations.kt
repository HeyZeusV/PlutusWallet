package com.heyzeusv.plutuswallet.ui

/**
 *  Contract for information needed on every navigation destination.
 */
interface PWDestination {
    val route: String
}

/**
 *  Navigation destinations.
 */
object Overview: PWDestination {
    override val route = "overview"
}

object Transaction: PWDestination {
    override val route = "transaction"
}

object Accounts: PWDestination {
    override val route = "accounts"
}

object Categories: PWDestination {
    override val route = "categories"
}

object Settings: PWDestination {
    override val route = "settings"
}

object About: PWDestination {
    override val route = "about"
}

val PWScreens = listOf(Overview, Transaction, Accounts, Categories, Settings, Accounts)