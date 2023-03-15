package com.heyzeusv.plutuswallet

import androidx.activity.ComponentActivity
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertAny
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.text.TextLayoutResult
import androidx.navigation.NavHostController
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import com.heyzeusv.plutuswallet.data.model.TranListItemFull
import com.heyzeusv.plutuswallet.util.TransactionType.EXPENSE
import com.heyzeusv.plutuswallet.util.theme.PWLightColors
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals

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

/**
 *  Assertion that looks at editable text [value].
 */
fun SemanticsNodeInteraction.assertEditTextEquals(value: String) : SemanticsNodeInteraction =
    assert(hasEditTextExactly(value))

/**
 *  Matcher that checks if EditableText matches [value].
 */
fun hasEditTextExactly(value: String): SemanticsMatcher =
    SemanticsMatcher("${SemanticsProperties.EditableText.name} is $value") { node ->
        var actual = ""
        node.config.getOrNull(SemanticsProperties.EditableText)?.let { actual = it.text }
        return@SemanticsMatcher actual == value
    }

/**
 *  Multiple assertions that checks that all the required data of given [tlif] is correctly
 *  displayed. Used by multiple test files.
 */
fun <A : ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<A>, A>.checkTlifIsDisplayed(
    tlif: TranListItemFull
) {
    val colors = PWLightColors
    onNode(hasTestTag("${tlif.tli.id}"), useUnmergedTree = true).onChildren()
        .assertAny(hasText(tlif.tli.title))
        .assertAny(hasText(tlif.tli.account))
        .assertAny(hasText(tlif.formattedDate))
        .assertAny(hasText(tlif.tli.category))
    // total requires extra check of text color
    onNode(
        hasText(text = tlif.formattedTotal) and hasParent(hasTestTag("${tlif.tli.id}")),
        useUnmergedTree = true
    )
        .assertIsDisplayed()
        .assertTextColor(
            if (tlif.tli.type == EXPENSE.type) colors.expense else colors.income
        )
}

/**
 *  Assertion that checks if current screen matches [expectedRouteName] by check back stack.
 */
fun NavHostController.assertCurrentRouteName(expectedRouteName: String) {
    assertEquals(expectedRouteName, currentBackStackEntry?.destination?.route)
}

fun SnackbarHostState.assertDisplayedMessage(expectedMessageId: Int, vararg args: Any?) {
    runBlocking {
        val actualSnackbarText = snapshotFlow { currentSnackbarData }
            .filterNotNull().first().message
        val expectedSnackbarText = InstrumentationRegistry.getInstrumentation().targetContext
            .resources.getString(expectedMessageId, *args)
        assertEquals(expectedSnackbarText, actualSnackbarText)
    }
}

/**
 *  Assertion that checks if node's background color matches [expectedBackground].
 *  Found [here](https://stackoverflow.com/a/70682865)
 */
fun SemanticsNodeInteraction.assertBackgroundColor(expectedBackground: Color) {
    val capturedName = captureToImage().colorSpace.name
    assertEquals(expectedBackground.colorSpace.name, capturedName)
}