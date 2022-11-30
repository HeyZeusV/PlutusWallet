package com.heyzeusv.plutuswallet.ui

import android.content.res.Resources
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.text.TextLayoutResult
import com.heyzeusv.plutuswallet.R
import com.heyzeusv.plutuswallet.data.DummyDataUtil
import com.heyzeusv.plutuswallet.data.Repository
import com.heyzeusv.plutuswallet.data.FakeAndroidRepository
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.AfterEach

@HiltAndroidTest
class OverviewTests {

    @get:Rule(order = 1)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 2)
    var composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var fakeRepo: Repository
    lateinit var repo: FakeAndroidRepository
    lateinit var pwColors: PlutusWalletColors
    lateinit var res: Resources

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

    @Test
    fun overview_displayList() {
        // check that we are on Overview screen
        composeRule.onNodeWithText(res.getString(R.string.cfl_overview)).assertExists()

        dd.tranList.forEach { item ->
            // checks that all required information is being displayed
            composeRule.onNodeWithText(item.title).assertExists()
            composeRule.onNodeWithText(item.account).assertExists()
            composeRule.onNodeWithText(dateFormatter.format(item.date)).assertExists()
            // extra check to make sure text is correct color
            composeRule.onNodeWithText(
                text = "\$${totalFormatter.format(item.total)}",
                useUnmergedTree = true
            )
                .assertExists()
                .assertTextColor(if (item.type == "Expense") pwColors.expense else pwColors.income)
            composeRule.onNodeWithText(item.category).assertExists()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun overview_deleteTransaction() = runTest {
        // check that we are on Overview screen
        composeRule.onNodeWithText(res.getString(R.string.cfl_overview)).assertExists()

        composeRule.onNode(hasTestTag("${dd.tran2.id}")).performTouchInput { longClick() }
        // checks that AlertDialog is being displayed and press confirm button
        composeRule.onNode(hasTestTag("AlertDialog")).assertExists()
        composeRule.onNode(
            hasTestTag("AlertDialog confirm"),
            useUnmergedTree = true
        ).performClick()

        // rerun repo call to get updated list
        repo.getIvt()

        // check that correct Transaction has been deleted
        composeRule.onNode(hasTestTag("${dd.tran2.id}")).assertDoesNotExist()
    }

    /**
     *  Assertion that looks at text [color]
     */
    fun SemanticsNodeInteraction.assertTextColor(color: Color): SemanticsNodeInteraction =
        assert(isOfColor(color))

    /**
     *  Matcher that checks if text color matches [color] by checking node's TextLayoutResult.
     *  Found on StackOverflow [here](https://stackoverflow.com/a/71077459)
     *  Google does have a section explaining how to make custom semantics properties for testing
     *  [here](https://developer.android.com/jetpack/compose/testing#custom-semantics-properties),
     *  but they have a warning that it shouldn't be used for visual properties like colors...
     */
    private fun isOfColor(color: Color): SemanticsMatcher = SemanticsMatcher(
        "${SemanticsProperties.Text.name} is of color '$color'"
    ) {
        val textLayoutResults = mutableListOf<TextLayoutResult>()
        it.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(textLayoutResults)
        return@SemanticsMatcher if (textLayoutResults.isEmpty()) {
            false
        } else {
            textLayoutResults.first().layoutInput.style.color == color
        }
    }
}