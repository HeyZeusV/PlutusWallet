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
import com.heyzeusv.plutuswallet.ui.account.AccountViewModel
import com.heyzeusv.plutuswallet.ui.category.CategoryViewModel
import com.heyzeusv.plutuswallet.ui.cfl.chart.ChartViewModel
import com.heyzeusv.plutuswallet.ui.cfl.filter.FilterViewModel
import com.heyzeusv.plutuswallet.ui.cfl.tranlist.TransactionListViewModel
import com.heyzeusv.plutuswallet.ui.theme.LocalPWColors
import com.heyzeusv.plutuswallet.ui.theme.PWDarkColors
import com.heyzeusv.plutuswallet.ui.theme.PWLightColors
import com.heyzeusv.plutuswallet.ui.theme.PlutusWalletColors
import com.heyzeusv.plutuswallet.ui.theme.PlutusWalletTheme
import com.heyzeusv.plutuswallet.ui.transaction.TransactionType.EXPENSE
import com.heyzeusv.plutuswallet.ui.transaction.TransactionType.INCOME
import com.heyzeusv.plutuswallet.ui.transaction.TransactionViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import java.math.RoundingMode
import java.text.DateFormat
import java.text.DecimalFormat
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() = runTest {
        hiltRule.inject()
        composeRule.activity.apply {
            res = resources
            setContent {
                pwColors = if (isSystemInDarkTheme()) PWDarkColors else PWLightColors
                CompositionLocalProvider(LocalPWColors provides pwColors) {
                    PlutusWalletTheme {
                        PlutusWalletApp(
                            tranListVM = viewModels<TransactionListViewModel>().value,
                            chartVM = viewModels<ChartViewModel>().value,
                            filterVM = viewModels<FilterViewModel>().value,
                            tranVM = viewModels<TransactionViewModel>().value,
                            accountVM = viewModels<AccountViewModel>().value,
                            categoryVM = viewModels<CategoryViewModel>().value
                        )
                    }
                }
            }
        }
        repo = (fakeRepo as FakeAndroidRepository)
        repo.accountListEmit(dd.accList)
        repo.accountsUsedListEmit(
            dd.accList.filter { acc -> dd.tranList.any { it.account == acc.name }}.distinct()
        )
        repo.accountNameListEmit(dd.accList.map { it.name })
        repo.expenseCatNameListEmit(dd.catList.filter { it.type == EXPENSE.type }.map { it.name })
        repo.incomeCatNameListEmit(dd.catList.filter { it.type == INCOME.type }.map { it.name })
        repo.expenseCatListEmit(dd.catList.filter { it.type == EXPENSE.type })
        repo.expenseCatUsedListEmit(
            dd.catList.filter { cat ->
                cat.type == EXPENSE.type && dd.tranList.any { it.category == cat.name }
            }.distinct()
        )
        repo.incomeCatListEmit(dd.catList.filter { it.type == INCOME.type })
        repo.incomeCatUsedListEmit(
            dd.catList.filter { cat ->
                cat.type == INCOME.type && dd.tranList.any { it.category == cat.name }
            }.distinct()
        )
    }

    @AfterEach
    fun afterEach() {
        repo.resetLists()
    }
}