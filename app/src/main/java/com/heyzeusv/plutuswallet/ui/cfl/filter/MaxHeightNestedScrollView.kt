package com.heyzeusv.plutuswallet.ui.cfl.filter

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.core.widget.NestedScrollView
import com.heyzeusv.plutuswallet.R

/**
 *  NestedScrollView provided by Android does not support maxHeight. This version accepts maxHeight
 *  attribute.
 *  Taken from: [https://stackoverflow.com/questions/59727706/android-custom-class-for-setting-the-max-height-of-a-nestedscrollview-does-not]
 */
class MaxHeightNestedScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {

    private var maxHeight = -1

    init {
        init(context, attrs, defStyleAttr)
    }

    private fun init(context: Context, attrs: AttributeSet, defStyleAttr: Int) {

        val a: TypedArray = context.obtainStyledAttributes(
            attrs, R.styleable.MaxHeightNestedScrollView, defStyleAttr, 0
        )
        // retrieves maxHeight value from attribute defined in attrs.xml
        maxHeight =
            a.getDimensionPixelSize(R.styleable.MaxHeightNestedScrollView_maxHeight, 0)
        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        // if maxHeight has been set in layout then ensure that ScrollView is not taller
        // than given height else follow given layout_height.
        if (maxHeight > 0) {
            val heightMS: Int = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST)
            super.onMeasure(widthMeasureSpec, heightMS)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }
}