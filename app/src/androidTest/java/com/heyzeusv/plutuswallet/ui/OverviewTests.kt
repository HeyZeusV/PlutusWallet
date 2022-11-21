package com.heyzeusv.plutuswallet.ui

import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.heyzeusv.plutuswallet.data.DummyDataUtil
import com.heyzeusv.plutuswallet.ui.cfl.CFLViewModel
import com.heyzeusv.plutuswallet.ui.cfl.tranlist.TransactionListViewModel
import com.heyzeusv.plutuswallet.ui.theme.LocalPWColors
import com.heyzeusv.plutuswallet.ui.theme.PWDarkColors
import com.heyzeusv.plutuswallet.ui.theme.PWLightColors
import com.heyzeusv.plutuswallet.ui.theme.PlutusWalletTheme
import com.heyzeusv.plutuswallet.ui.transaction.TransactionViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule

@HiltAndroidTest
class OverviewTests {

    @get:Rule(order = 1)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 2)
    var composeRule = createAndroidComposeRule<MainActivity>()

    val dd = DummyDataUtil()

    @Before
    fun setUp() {
        hiltRule.inject()
        composeRule.setContent {
            val pwColors = if (isSystemInDarkTheme()) PWDarkColors else PWLightColors
            CompositionLocalProvider(LocalPWColors provides pwColors) {
                PlutusWalletTheme {
                    PlutusWalletApp(
                        tranListVM = composeRule.activity.viewModels<TransactionListViewModel>().value,
                        cflVM = composeRule.activity.viewModels<CFLViewModel>().value,
                        tranVM = composeRule.activity.viewModels<TransactionViewModel>().value
                    )
                }
            }
        }
    }
}