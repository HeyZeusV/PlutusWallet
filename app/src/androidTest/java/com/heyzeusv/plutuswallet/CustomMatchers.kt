package com.heyzeusv.plutuswallet

import android.view.View
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.text.TextLayoutResult
import androidx.test.espresso.matcher.BoundedMatcher
import com.github.mikephil.charting.charts.PieChart
import org.hamcrest.Description
import org.hamcrest.Matcher

/**
 *  CustomMatchers to be used for testing
 */
class CustomMatchers {

    companion object {

        /**
         *  Checks that [category] with sum of [total] exists in dataSet of PieChart.
         */
        fun chartEntry(category: String, total: Float): Matcher<View> {

            return object : BoundedMatcher<View, PieChart>(PieChart::class.java) {

                override fun describeTo(description: Description?) {

                    description?.appendText("has entry with label $category and total of $total")
                }

                override fun matchesSafely(chart: PieChart): Boolean {

                    for (i in 0 until chart.data.dataSet.entryCount) {

                        val entry = chart.data.dataSet.getEntryForIndex(i)
                        if (entry.label == category && entry.value == total) return true
                    }
                    return false
                }
            }
        }

        /**
         *  Checks that PieChart has given [centerText].
         */
        fun chartText(centerText: String): Matcher<View> {

            return object : BoundedMatcher<View, PieChart>(PieChart::class.java) {

                override fun describeTo(description: Description?) {

                    description?.appendText("with center text: $centerText")
                }

                override fun matchesSafely(chart: PieChart): Boolean {

                    return chart.centerText == centerText
                }
            }
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
}