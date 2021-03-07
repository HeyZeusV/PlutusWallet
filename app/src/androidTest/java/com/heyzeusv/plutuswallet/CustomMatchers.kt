package com.heyzeusv.plutuswallet

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.matcher.BoundedMatcher
import com.github.mikephil.charting.charts.PieChart
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputLayout
import com.heyzeusv.plutuswallet.util.bindingadapters.getSelectedChipId
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

/**
 *  CustomMatchers to be used with Espresso for testing
 */
class CustomMatchers {

    companion object {

        /**
         *  Checks if id of Chip currently selected in ChipGroup matches the given [id].
         */
        fun chipSelected(id: Int): Matcher<View> {

            return object : BoundedMatcher<View, ChipGroup>(ChipGroup::class.java) {

                override fun describeTo(description: Description?) {

                    description?.appendText("Chip id is: $id")
                }

                override fun matchesSafely(chipGroup: ChipGroup): Boolean {

                    return chipGroup.getSelectedChipId() == id
                }

            }
        }

        /**
         *  Checks if the number of entries in RecyclerView matches given [size].
         */
        fun rvSize(size: Int): Matcher<View> {
            
            return object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {
                
                override fun describeTo(description: Description?) {
                    
                    description?.appendText("with list size: $size")
                }

                override fun matchesSafely(recyclerView: RecyclerView): Boolean {
                    
                    return recyclerView.adapter?.itemCount == size
                }
            }
        }

        /**
         *  Uses [matcher] to check View with [targetViewId] within
         *  ViewHolder at [pos] in RecyclerView.
         */
        fun rvViewHolder(pos: Int, matcher: Matcher<View>, targetViewId: Int): Matcher<View> {

            return object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {

                override fun describeTo(description: Description?) {

                    description?.appendText("has view id $matcher at position $pos")
                }

                override fun matchesSafely(rv: RecyclerView): Boolean {

                    val viewHolder = rv.findViewHolderForAdapterPosition(pos)
                    val targetView = viewHolder?.itemView?.findViewById<View>(targetViewId)
                    return matcher.matches(targetView)
                }
            }
        }

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
         *  Used when multiple Views have the same id. [matcher] will check View at [index].
         */
        fun withIndex(matcher: Matcher<View>, index: Int): Matcher<View> {

            return object : TypeSafeMatcher<View>() {

                var currentIndex = 0

                override fun describeTo(description: Description?) {

                    description?.appendText("with index: $index")
                }

                override fun matchesSafely(view: View): Boolean {

                    return matcher.matches(view) && currentIndex++ == index
                }
            }
        }

        /**
         *  Checks that TextView text is color of [colorId].
         */
        fun withTextColor(colorId: Int): Matcher<View> {

            return object : BoundedMatcher<View, TextView>(TextView::class.java) {

                override fun describeTo(description: Description?) {

                    description?.appendText("with text color: $colorId")
                }

                override fun matchesSafely(tv: TextView): Boolean {

                    val id = ContextCompat.getColor(tv.context, colorId)
                    return tv.currentTextColor == id
                }
            }
        }

        /**
         *  Checks TextInputLayout [prefix] text.
         */
        fun withPrefix(prefix: String?): Matcher<View> {

            return object : BoundedMatcher<View, TextInputLayout>(TextInputLayout::class.java) {

                override fun describeTo(description: Description?) {

                    description?.appendText("with prefix: $prefix")
                }

                override fun matchesSafely(til: TextInputLayout): Boolean {

                    return til.prefixText == prefix
                }
            }
        }

        /**
         *  Checks TextInputLayout [suffix] text.
         */
        fun withSuffix(suffix: String?): Matcher<View> {

            return object : BoundedMatcher<View, TextInputLayout>(TextInputLayout::class.java) {

                override fun describeTo(description: Description?) {

                    description?.appendText("with suffix: $suffix")
                }

                override fun matchesSafely(til: TextInputLayout): Boolean {

                    return til.suffixText == suffix
                }
            }
        }

        /**
         *  Checks that MaterialButton text and stroke color matches [colorId].
         */
        fun withTextAndStrokeColor(colorId: Int): Matcher<View> {

            return object : BoundedMatcher<View, MaterialButton>(MaterialButton::class.java) {

                override fun describeTo(description: Description?) {

                    description?.appendText("with color id: $colorId")
                }

                override fun matchesSafely(button: MaterialButton): Boolean {

                    val color: Int = ContextCompat.getColor(button.context, colorId)
                    val strokeColor: Boolean = color == button.strokeColor.defaultColor
                    val textColor: Boolean = color == button.currentTextColor

                    return strokeColor && textColor
                }
            }
        }
    }
}