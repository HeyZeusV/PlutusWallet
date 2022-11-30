package com.heyzeusv.plutuswallet.ui

import android.content.res.Resources
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.heyzeusv.plutuswallet.data.DummyDataUtil
import com.heyzeusv.plutuswallet.data.FakeAndroidRepository
import com.heyzeusv.plutuswallet.data.Repository
import com.heyzeusv.plutuswallet.ui.cfl.CFLViewModel
import com.heyzeusv.plutuswallet.ui.cfl.tranlist.TransactionListViewModel
import com.heyzeusv.plutuswallet.ui.theme.LocalPWColors
import com.heyzeusv.plutuswallet.ui.theme.PWDarkColors
import com.heyzeusv.plutuswallet.ui.theme.PWLightColors
import com.heyzeusv.plutuswallet.ui.theme.PlutusWalletColors
import com.heyzeusv.plutuswallet.ui.theme.PlutusWalletTheme
import com.heyzeusv.plutuswallet.ui.transaction.TransactionViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import java.math.RoundingMode
import java.text.DateFormat
import java.text.DecimalFormat
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.jupiter.api.AfterEach

@HiltAndroidTest
abstract class BaseTest {

    @get:Rule(order = 1)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 2)
    var composeRule = createAndroidComposeRule<MainActivity>()

    @Inject lateinit var fakeRepo: Repository
    lateinit var repo: FakeAndroidRepository
    protected lateinit var pwColors: PlutusWalletColors
    protected lateinit var res: Resources

    val dd = DummyDataUtil()
    val dateFormatter: DateFormat = DateFormat.getDateInstance(0)
    val totalFormatter = DecimalFormat("#,##0.00").apply { roundingMode = RoundingMode.HALF_UP }

    @Before
    fun setUp() {
        hiltRule.inject()
        composeRule.activity.apply {
            res = resources
            setContent {
                pwColors = if (isSystemInDarkTheme()) PWDarkColors else PWLightColors
                CompositionLocalProvider(LocalPWColors provides pwColors) {
                    PlutusWalletTheme {
                        PlutusWalletApp(
                            tranListVM = viewModels<TransactionListViewModel>().value,
                            cflVM = viewModels<CFLViewModel>().value,
                            tranVM = viewModels<TransactionViewModel>().value
                        )
                    }
                }
            }
        }
        repo = (fakeRepo as FakeAndroidRepository)
    }

    @AfterEach
    fun afterEach() {
        repo.resetLists()
    }
}