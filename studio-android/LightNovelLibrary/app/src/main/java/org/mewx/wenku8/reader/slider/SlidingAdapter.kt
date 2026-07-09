package org.mewx.wenku8.reader.slider

import android.os.Bundle
import android.os.Parcelable
import android.view.View

abstract class SlidingAdapter<T> {
    private val views = arrayOfNulls<View>(3)
    private var currentViewIndex = 0
    private var slidingLayout: SlidingLayout? = null

    fun setSlidingLayout(slidingLayout: SlidingLayout) {
        this.slidingLayout = slidingLayout
    }

    fun getUpdatedCurrentView(): View {
        var currentView = views[currentViewIndex]
        if (currentView == null) {
            currentView = getView(null, getCurrent())
            views[currentViewIndex] = currentView
        } else {
            val updatedView = getView(currentView, getCurrent())
            if (currentView !== updatedView) {
                currentView = updatedView
                views[currentViewIndex] = updatedView
            }
        }
        return currentView
    }

    fun getCurrentView(): View {
        var currentView = views[currentViewIndex]
        if (currentView == null) {
            currentView = getView(null, getCurrent())
            views[currentViewIndex] = currentView
        }
        return currentView
    }

    private fun viewAt(index: Int): View? {
        return views[(index + 3) % 3]
    }

    private fun setViewAt(index: Int, view: View?) {
        views[(index + 3) % 3] = view
    }

    fun getUpdatedNextView(): View? {
        var nextView = viewAt(currentViewIndex + 1)
        val hasNext = hasNext()
        if (nextView == null && hasNext) {
            nextView = getView(null, getNext())
            setViewAt(currentViewIndex + 1, nextView)
        } else if (hasNext) {
            val updatedView = getView(nextView, getNext())
            if (updatedView !== nextView) {
                nextView = updatedView
                setViewAt(currentViewIndex + 1, nextView)
            }
        }
        return nextView
    }

    fun getNextView(): View? {
        var nextView = viewAt(currentViewIndex + 1)
        if (nextView == null && hasNext()) {
            nextView = getView(null, getNext())
            setViewAt(currentViewIndex + 1, nextView)
        }
        return nextView
    }

    fun getUpdatedPreviousView(): View? {
        var previousView = viewAt(currentViewIndex - 1)
        val hasPrevious = hasPrevious()
        if (previousView == null && hasPrevious) {
            previousView = getView(null, getPrevious())
            setViewAt(currentViewIndex - 1, previousView)
        } else if (hasPrevious) {
            val updatedView = getView(previousView, getPrevious())
            if (updatedView !== previousView) {
                previousView = updatedView
                setViewAt(currentViewIndex - 1, previousView)
            }
        }
        return previousView
    }

    fun setPreviousView(view: View?) {
        setViewAt(currentViewIndex - 1, view)
    }

    fun setNextView(view: View?) {
        setViewAt(currentViewIndex + 1, view)
    }

    fun setCurrentView(view: View?) {
        setViewAt(currentViewIndex, view)
    }

    fun getPreviousView(): View? {
        var previousView = viewAt(currentViewIndex - 1)
        if (previousView == null && hasPrevious()) {
            previousView = getView(null, getPrevious())
            setViewAt(currentViewIndex - 1, previousView)
        }
        return previousView
    }

    fun moveToNext() {
        computeNext()
        currentViewIndex = (currentViewIndex + 1) % 3
    }

    fun moveToPrevious() {
        computePrevious()
        currentViewIndex = (currentViewIndex + 2) % 3
    }

    abstract fun getView(contentView: View?, item: T): View

    abstract fun getCurrent(): T

    abstract fun getNext(): T

    abstract fun getPrevious(): T

    abstract fun hasNext(): Boolean

    abstract fun hasPrevious(): Boolean

    protected abstract fun computeNext()

    protected abstract fun computePrevious()

    open fun saveState(): Bundle? = null

    open fun restoreState(parcelable: Parcelable?, loader: ClassLoader?) {
        currentViewIndex = 0
        views[0] = null
        views[1] = null
        views[2] = null
    }

    fun notifyDataSetChanged() {
        slidingLayout?.resetFromAdapter()
        slidingLayout?.postInvalidate()
    }
}
