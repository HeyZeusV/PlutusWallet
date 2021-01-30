package com.heyzeusv.plutuswallet

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import org.hamcrest.Matcher

class CustomActions {

    companion object {

        /**
         *  Performs click on View with [id] within ViewHolder.
         *
         *  Usage: onView(withId(recyclerViewId))
         *      .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
         *          posOfViewHolder, rvViewHolderClick(idOfViewToClick)))
         */
        fun rvViewClick(id: Int): ViewAction {

            return object : ViewAction {

                override fun getConstraints(): Matcher<View>? {

                    return null
                }

                override fun getDescription(): String {

                    return "Click on child view in ViewHolder with id."
                }

                override fun perform(uiController: UiController, view: View) {

                    click().perform(uiController, view.findViewById(id))
                }
            }
        }
    }
}