package com.heyzeusv.plutuswallet

import android.view.View
import com.google.android.material.chip.ChipGroup
import com.heyzeusv.plutuswallet.util.bindingadapters.getSelectedChipId
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

/**
 *  CustomMatchers to be used with Espresso for testing
 */
class CustomMatchers {

    companion object {

        // checks if id of chip currently selected in ChipGroup matches the given id
        fun chipSelected(id: Int): Matcher<View> {
            return object : TypeSafeMatcher<View>() {
                override fun describeTo(description: Description?) {
                    description?.appendText("Chip id is: $id")
                }

                override fun matchesSafely(view: View?): Boolean {
                    return (view as ChipGroup).getSelectedChipId() == id
                }

            }
        }
    }
}