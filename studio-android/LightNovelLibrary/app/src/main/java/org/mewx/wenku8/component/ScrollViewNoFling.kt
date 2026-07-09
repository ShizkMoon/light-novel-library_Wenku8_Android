package org.mewx.wenku8.component

import android.content.Context
import android.util.AttributeSet
import android.widget.ScrollView

/**
 * A sticky scroll view.
 */
class ScrollViewNoFling : ScrollView {
    private val flingFactor = 1

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun fling(velocityY: Int) {
        super.fling(velocityY / flingFactor)
    }
}
