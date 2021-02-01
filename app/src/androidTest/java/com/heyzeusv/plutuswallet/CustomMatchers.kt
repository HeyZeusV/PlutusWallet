package com.heyzeusv.plutuswallet

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.chip.ChipGroup
import com.heyzeusv.plutuswallet.util.bindingadapters.getSelectedChipId
import org.hamcrest.Description
import org.hamcrest.Matcher

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
    }
}