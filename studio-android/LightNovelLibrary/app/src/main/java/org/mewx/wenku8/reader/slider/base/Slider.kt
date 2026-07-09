package org.mewx.wenku8.reader.slider.base

import android.view.MotionEvent
import org.mewx.wenku8.reader.slider.SlidingAdapter
import org.mewx.wenku8.reader.slider.SlidingLayout

interface Slider {
    fun init(slidingLayout: SlidingLayout)

    fun resetFromAdapter(adapter: SlidingAdapter<*>)

    fun onTouchEvent(event: MotionEvent): Boolean

    fun computeScroll()

    fun slideNext()

    fun slidePrevious()
}
