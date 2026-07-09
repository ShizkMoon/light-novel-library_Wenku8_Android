package org.mewx.wenku8.reader.slider.base

import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.widget.Scroller
import org.mewx.wenku8.global.GlobalConfig
import org.mewx.wenku8.reader.slider.SlidingAdapter
import org.mewx.wenku8.reader.slider.SlidingLayout

class OverlappedSlider : BaseSlider() {
    private lateinit var scroller: Scroller
    private var velocityTracker: VelocityTracker? = null

    private var velocityValue = 0
    private var limitDistance = 0
    private var screenWidth = 0
    private var maximumFlingVelocity = 0

    private var touchResult = MOVE_NO_RESULT
    private var direction = MOVE_NO_RESULT
    private var mode = MODE_NONE

    private var scrollerView: View? = null
    private var startX = 0
    private lateinit var slidingLayout: SlidingLayout

    private val adapter: SlidingAdapter<Any?>
        get() = slidingLayout.getAdapter()

    override fun init(slidingLayout: SlidingLayout) {
        this.slidingLayout = slidingLayout
        scroller = Scroller(slidingLayout.context)
        screenWidth = slidingLayout.context.resources.displayMetrics.widthPixels
        maximumFlingVelocity = ViewConfiguration.get(slidingLayout.context).scaledMaximumFlingVelocity
        limitDistance = screenWidth / 3
    }

    override fun resetFromAdapter(adapter: SlidingAdapter<*>) {
        slidingLayout.addView(this.adapter.getCurrentView())

        if (this.adapter.hasNext()) {
            val nextView = this.adapter.getNextView()!!
            slidingLayout.addView(nextView, 0)
            nextView.scrollTo(0, 0)
        }

        if (this.adapter.hasPrevious()) {
            val previousView = this.adapter.getPreviousView()!!
            slidingLayout.addView(previousView)
            previousView.scrollTo(screenWidth, 0)
        }

        slidingLayout.slideSelected(this.adapter.getCurrent())
    }

    fun getTopView(): View? = adapter.getPreviousView()

    fun getCurrentShowView(): View = adapter.getCurrentView()

    override fun onTouchEvent(event: MotionEvent): Boolean {
        obtainVelocityTracker(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (scroller.isFinished) {
                    startX = event.x.toInt()
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (!scroller.isFinished) {
                    return false
                }
                if (startX == 0) {
                    startX = event.x.toInt()
                }
                val distance = startX - event.x.toInt()
                if (direction == MOVE_NO_RESULT) {
                    if (adapter.hasNext() && distance > 0) {
                        direction = MOVE_TO_LEFT
                    } else if (adapter.hasPrevious() && distance < 0) {
                        direction = MOVE_TO_RIGHT
                    }
                }
                if (mode == MODE_NONE &&
                    ((direction == MOVE_TO_LEFT && adapter.hasNext()) ||
                        (direction == MOVE_TO_RIGHT && adapter.hasPrevious()))
                ) {
                    mode = MODE_MOVE
                }

                if (mode == MODE_MOVE) {
                    if ((direction == MOVE_TO_LEFT && distance <= 0) || (direction == MOVE_TO_RIGHT && distance >= 0)) {
                        mode = MODE_NONE
                    }
                }

                if (direction != MOVE_NO_RESULT) {
                    scrollerView = if (direction == MOVE_TO_LEFT) {
                        getCurrentShowView()
                    } else {
                        getTopView()
                    }

                    if (mode == MODE_MOVE) {
                        velocityTracker!!.computeCurrentVelocity(1000, maximumFlingVelocity.toFloat())
                        if (!GlobalConfig.isEinkModeEnabled()) {
                            if (direction == MOVE_TO_LEFT) {
                                scrollerView!!.scrollTo(distance, 0)
                            } else {
                                scrollerView!!.scrollTo(screenWidth + distance, 0)
                            }
                        }
                    } else {
                        val scrollX = scrollerView!!.scrollX
                        if (direction == MOVE_TO_LEFT && scrollX != 0 && adapter.hasNext()) {
                            scrollerView!!.scrollTo(0, 0)
                        } else if (direction == MOVE_TO_RIGHT && adapter.hasPrevious() && screenWidth != kotlin.math.abs(scrollX)) {
                            scrollerView!!.scrollTo(screenWidth, 0)
                        }
                    }
                }

                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                if (scrollerView == null) {
                    return false
                }
                val scrollX = scrollerView!!.scrollX
                velocityValue = velocityTracker!!.xVelocity.toInt()

                var time = 500
                val isEinkMode = GlobalConfig.isEinkModeEnabled()
                if (isEinkMode) {
                    time = 0
                }

                if (mode == MODE_MOVE && direction == MOVE_TO_LEFT) {
                    if (scrollX > limitDistance || velocityValue < -time) {
                        touchResult = MOVE_TO_LEFT
                        if (velocityValue < -time) {
                            time = if (isEinkMode) 0 else 200
                        }
                        if (isEinkMode) {
                            scrollerView!!.scrollTo(screenWidth, 0)
                            invalidate()
                            moveToNext()
                            touchResult = MOVE_NO_RESULT
                        } else {
                            scroller.startScroll(scrollX, 0, screenWidth - scrollX, 0, time)
                        }
                    } else {
                        touchResult = MOVE_NO_RESULT
                        if (!isEinkMode) {
                            scroller.startScroll(scrollX, 0, -scrollX, 0, time)
                        }
                    }
                } else if (mode == MODE_MOVE && direction == MOVE_TO_RIGHT) {
                    if (screenWidth - scrollX > limitDistance || velocityValue > time) {
                        touchResult = MOVE_TO_RIGHT
                        if (velocityValue > time) {
                            time = if (isEinkMode) 0 else 250
                        }
                        if (isEinkMode) {
                            scrollerView!!.scrollTo(0, 0)
                            invalidate()
                            moveToPrevious()
                            touchResult = MOVE_NO_RESULT
                        } else {
                            scroller.startScroll(scrollX, 0, -scrollX, 0, time)
                        }
                    } else {
                        touchResult = MOVE_NO_RESULT
                        if (!isEinkMode) {
                            scroller.startScroll(scrollX, 0, screenWidth - scrollX, 0, time)
                        }
                    }
                }

                resetVariables()
                invalidate()
            }
        }
        return true
    }

    private fun resetVariables() {
        direction = MOVE_NO_RESULT
        mode = MODE_NONE
        startX = 0
        releaseVelocityTracker()
    }

    fun moveToNext(): Boolean {
        if (!adapter.hasNext()) {
            return false
        }

        val previousView = adapter.getPreviousView()
        if (previousView != null) {
            slidingLayout.removeView(previousView)
        }
        var newNextView = previousView

        adapter.moveToNext()

        if (adapter.hasNext()) {
            if (newNextView != null) {
                val updatedNextView = adapter.getView(newNextView, adapter.getNext())
                if (updatedNextView !== newNextView) {
                    adapter.setNextView(updatedNextView)
                    newNextView = updatedNextView
                }
            } else {
                newNextView = adapter.getNextView()
            }
            slidingLayout.addView(newNextView, 0)
            newNextView!!.scrollTo(0, 0)
        }

        slidingLayout.slideSelected(adapter.getCurrent())
        return true
    }

    fun moveToPrevious(): Boolean {
        if (!adapter.hasPrevious()) {
            return false
        }

        val nextView = adapter.getNextView()
        if (nextView != null) {
            slidingLayout.removeView(nextView)
        }
        var newPreviousView = nextView

        adapter.moveToPrevious()
        slidingLayout.slideSelected(adapter.getCurrent())

        if (adapter.hasPrevious()) {
            if (newPreviousView != null) {
                val updatedPreviousView = adapter.getView(newPreviousView, adapter.getPrevious())
                if (newPreviousView !== updatedPreviousView) {
                    adapter.setPreviousView(updatedPreviousView)
                    newPreviousView = updatedPreviousView
                }
            } else {
                newPreviousView = adapter.getPreviousView()
            }
            slidingLayout.addView(newPreviousView)
            newPreviousView!!.scrollTo(screenWidth, 0)
        }

        return true
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollerView!!.scrollTo(scroller.currX, scroller.currY)
            invalidate()
        } else if (scroller.isFinished && touchResult != MOVE_NO_RESULT) {
            if (touchResult == MOVE_TO_LEFT) {
                moveToNext()
            } else {
                moveToPrevious()
            }
            touchResult = MOVE_NO_RESULT
            invalidate()
        }
    }

    private fun invalidate() {
        slidingLayout.postInvalidate()
    }

    private fun obtainVelocityTracker(event: MotionEvent) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
        velocityTracker!!.addMovement(event)
    }

    private fun releaseVelocityTracker() {
        velocityTracker?.recycle()
        velocityTracker = null
    }

    override fun slideNext() {
        if (!adapter.hasNext() || !scroller.isFinished) {
            return
        }

        scrollerView = getCurrentShowView()

        if (GlobalConfig.isEinkModeEnabled()) {
            scrollerView!!.scrollTo(screenWidth, 0)
            moveToNext()
        } else {
            scroller.startScroll(0, 0, screenWidth, 0, 500)
            touchResult = MOVE_TO_LEFT
            slidingLayout.slideScrollStateChanged(MOVE_TO_LEFT)
        }

        invalidate()
    }

    override fun slidePrevious() {
        if (!adapter.hasPrevious() || !scroller.isFinished) {
            return
        }

        scrollerView = getTopView()

        if (GlobalConfig.isEinkModeEnabled()) {
            scrollerView!!.scrollTo(0, 0)
            moveToPrevious()
        } else {
            scroller.startScroll(screenWidth, 0, -screenWidth, 0, 500)
            touchResult = MOVE_TO_RIGHT
            slidingLayout.slideScrollStateChanged(MOVE_TO_RIGHT)
        }

        invalidate()
    }
}
