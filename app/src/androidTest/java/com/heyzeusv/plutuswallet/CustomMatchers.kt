package com.heyzeusv.plutuswallet

import android.view.View
import androidx.test.espresso.matcher.BoundedMatcher
import com.github.mikephil.charting.charts.PieChart
import org.hamcrest.Description
import org.hamcrest.Matcher

/**
 *  CustomMatchers to be used with Espresso for testing
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
    }
}