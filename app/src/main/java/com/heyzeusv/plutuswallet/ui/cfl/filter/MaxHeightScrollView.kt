package com.heyzeusv.plutuswallet.ui.cfl.filter

import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import androidx.core.widget.NestedScrollView

/**
 *  Custom ScrollView that has a max height of 55% of screen.
 */
class MaxHeightScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val displayMetrics: DisplayMetrics = context.resources.displayMetrics
        val height55Percent: Int = (displayMetrics.heightPixels * .55f).toInt()
        val heightMS: Int = MeasureSpec.makeMeasureSpec(height55Percent, MeasureSpec.AT_MOST)
        super.onMeasure(widthMeasureSpec, heightMS)
    }
}