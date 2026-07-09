package org.mewx.wenku8.reader.slider

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import org.mewx.wenku8.reader.slider.base.Slider

class SlidingLayout : ViewGroup {
    private var downMotionX = 0
    private var downMotionY = 0
    private var downMotionTime = 0L

    private var onTapListener: OnTapListener? = null
    private var slider: Slider? = null
    private var adapter: SlidingAdapter<*>? = null
    private var restoredAdapterState: Parcelable? = null
    private var restoredClassLoader: ClassLoader? = null
    private var slideChangeListener: OnSlideChangeListener? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    fun setSlider(slider: Slider) {
        this.slider = slider
        slider.init(this)
        resetFromAdapter()
    }

    @Suppress("UNCHECKED_CAST")
    fun getAdapter(): SlidingAdapter<Any?> {
        return requireNotNull(adapter) { "SlidingAdapter has not been set." } as SlidingAdapter<Any?>
    }

    fun setAdapter(adapter: SlidingAdapter<*>) {
        this.adapter = adapter
        adapter.setSlidingLayout(this)

        if (restoredAdapterState != null) {
            adapter.restoreState(restoredAdapterState, restoredClassLoader)
            restoredAdapterState = null
            restoredClassLoader = null
        }

        resetFromAdapter()
        postInvalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downMotionX = event.x.toInt()
                downMotionY = event.y.toInt()
                downMotionTime = System.currentTimeMillis()
            }
            MotionEvent.ACTION_UP -> computeTapMotion(event)
        }

        return requireNotNull(slider) { "Slider has not been set." }.onTouchEvent(event) || super.onTouchEvent(event)
    }

    fun setOnTapListener(listener: OnTapListener?) {
        onTapListener = listener
    }

    private fun computeTapMotion(event: MotionEvent) {
        val listener = onTapListener ?: return

        val xDiff = kotlin.math.abs(event.x.toInt() - downMotionX)
        val yDiff = kotlin.math.abs(event.y.toInt() - downMotionY)
        val timeDiff = System.currentTimeMillis() - downMotionTime

        if (xDiff < 5 && yDiff < 5 && timeDiff < 200) {
            listener.onSingleTap(event)
        }
    }

    override fun computeScroll() {
        super.computeScroll()
        requireNotNull(slider) { "Slider has not been set." }.computeScroll()
    }

    fun slideNext() {
        requireNotNull(slider) { "Slider has not been set." }.slideNext()
    }

    fun slidePrevious() {
        requireNotNull(slider) { "Slider has not been set." }.slidePrevious()
    }

    fun interface OnTapListener {
        fun onSingleTap(event: MotionEvent)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            child.layout(0, 0, child.measuredWidth, child.measuredHeight)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
        for (i in 0 until childCount) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    class SavedState : View.BaseSavedState {
        var adapterState: Parcelable? = null
        var loader: ClassLoader? = null

        constructor(superState: Parcelable?) : super(superState)

        @Suppress("DEPRECATION")
        constructor(parcel: Parcel, loader: ClassLoader?) : super(parcel) {
            val safeLoader = loader ?: javaClass.classLoader
            adapterState = parcel.readParcelable(safeLoader)
            this.loader = safeLoader
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeParcelable(adapterState, flags)
        }

        override fun toString(): String {
            return "BaseSlidingLayout.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + "}"
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> =
                object : Parcelable.ClassLoaderCreator<SavedState> {
                    override fun createFromParcel(parcel: Parcel, loader: ClassLoader?): SavedState {
                        return SavedState(parcel, loader)
                    }

                    override fun createFromParcel(parcel: Parcel): SavedState {
                        return SavedState(parcel, null)
                    }

                    override fun newArray(size: Int): Array<SavedState?> {
                        return arrayOfNulls(size)
                    }
                }
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val savedState = SavedState(super.onSaveInstanceState())
        adapter?.let {
            savedState.adapterState = it.saveState()
        }
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)

        val currentAdapter = adapter
        if (currentAdapter != null) {
            currentAdapter.restoreState(state.adapterState, state.loader)
            resetFromAdapter()
        } else {
            restoredAdapterState = state.adapterState
            restoredClassLoader = state.loader
        }
    }

    fun resetFromAdapter() {
        removeAllViews()
        val currentSlider = slider
        val currentAdapter = adapter
        if (currentSlider != null && currentAdapter != null) {
            currentSlider.resetFromAdapter(currentAdapter)
        }
    }

    fun setOnSlideChangeListener(listener: OnSlideChangeListener?) {
        slideChangeListener = listener
    }

    interface OnSlideChangeListener {
        fun onSlideScrollStateChanged(touchResult: Int)

        fun onSlideSelected(obj: Any?)
    }

    fun slideScrollStateChanged(moveDirection: Int) {
        slideChangeListener?.onSlideScrollStateChanged(moveDirection)
    }

    fun slideSelected(obj: Any?) {
        slideChangeListener?.onSlideSelected(obj)
    }
}
