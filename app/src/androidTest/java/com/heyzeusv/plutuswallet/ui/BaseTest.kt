package com.heyzeusv.plutuswallet.ui

import android.content.SharedPreferences
import android.content.res.Resources
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.preference.PreferenceManager
import com.heyzeusv.plutuswallet.data.DummyAndroidDataUtil
import com.heyzeusv.plutuswallet.data.FakeAndroidRepository
import com.heyzeusv.plutuswallet.data.PWRepositoryInterface
import com.heyzeusv.plutuswallet.util.theme.PWDarkColors
import com.heyzeusv.plutuswallet.util.theme.PWLightColors
import com.heyzeusv.plutuswallet.util.theme.PlutusWalletColors
import com.heyzeusv.plutuswallet.util.theme.PlutusWalletTheme
import com.heyzeusv.plutuswallet.util.Key
import com.heyzeusv.plutuswallet.util.TransactionType.EXPENSE
import com.heyzeusv.plutuswallet.util.TransactionType.INCOME
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
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

    @Inject lateinit var fakeRepo: PWRepositoryInterface
    lateinit var repo: FakeAndroidRepository
    protected lateinit var pwColors: PlutusWalletColors
    protected lateinit var res: Resources
    protected lateinit var sharedPref: SharedPreferences

    val dd = DummyAndroidDataUtil()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() = runTest {
        hiltRule.inject()
        composeRule.activity.apply {
            sharedPref = PreferenceManager.getDefaultSharedPreferences(baseContext)
            res = resources
            setContent {
                val theme = sharedPref.getString(Key.KEY_THEME.key, "-1")!!.toInt()
                pwColors = when (theme) {
                    1 -> PWLightColors
                    2 -> PWDarkColors
                    else -> if (isSystemInDarkTheme()) PWDarkColors else PWLightColors
                }
                PlutusWalletTheme {
                    PlutusWalletApp()
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