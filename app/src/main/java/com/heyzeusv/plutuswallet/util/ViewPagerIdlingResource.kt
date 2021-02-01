package com.heyzeusv.plutuswallet.util

import androidx.test.espresso.IdlingResource
import androidx.test.espresso.IdlingResource.ResourceCallback
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2

/**
 *  IdlingResource for ViewPager2, will be idle when ViewPager is transitioning from page to page
 *
 *  https://stackoverflow.com/questions/31056918/wait-for-view-pager-animations-with-espresso/32763454
 */
class ViewPager2IdlingResource(viewPager: ViewPager2, name: String) : IdlingResource {

    private val name: String
    // Default to idle since we can't query the scroll state.
    private var isIdle = true
    private var resourceCallback: ResourceCallback? = null

    init {
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                // Treat dragging as idle, or Espresso will block itself when swiping.
                isIdle = (state == ViewPager.SCROLL_STATE_IDLE
                        || state == ViewPager.SCROLL_STATE_DRAGGING)
                if (isIdle && resourceCallback != null) {
                    resourceCallback!!.onTransitionToIdle()
                }
            }
        })
        this.name = name
    }

    override fun getName(): String {

        return name
    }

    override fun isIdleNow(): Boolean {

        return isIdle
    }

    override fun registerIdleTransitionCallback(resourceCallback: ResourceCallback) {
        this.resourceCallback = resourceCallback
    }
}
