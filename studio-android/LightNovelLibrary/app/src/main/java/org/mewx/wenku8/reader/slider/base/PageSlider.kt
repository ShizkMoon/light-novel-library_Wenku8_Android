package org.mewx.wenku8.reader.slider.base

import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.widget.Scroller
import org.mewx.wenku8.reader.slider.SlidingAdapter
import org.mewx.wenku8.reader.slider.SlidingLayout

class PageSlider : BaseSlider() {
    private lateinit var scroller: Scroller
    private var velocityTracker: VelocityTracker? = null

    private var velocityValue = 0
    private var limitDistance = 0
    private var screenWidth = 0
    private var maximumFlingVelocity = 0

    private var touchResult = MOVE_NO_RESULT
    private var direction = MOVE_NO_RESULT
    private var mode = MODE_NONE

    private var moveLastPage = false
    private var moveFirstPage = false

    private var startX = 0
    private var leftScrollerView: View? = null
    private var rightScrollerView: View? = null
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
        val currentView = this.adapter.getUpdatedCurrentView()
        slidingLayout.addView(currentView)
        currentView.scrollTo(0, 0)

        if (this.adapter.hasPrevious()) {
            val previousView = this.adapter.getUpdatedPreviousView()!!
            slidingLayout.addView(previousView)
            previousView.scrollTo(screenWidth, 0)
        }

        if (this.adapter.hasNext()) {
            val nextView = this.adapter.getUpdatedNextView()!!
            slidingLayout.addView(nextView)
            nextView.scrollTo(-screenWidth, 0)
        }

        slidingLayout.slideSelected(this.adapter.getCurrent())
    }

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
                    if (distance > 0) {
                        direction = MOVE_TO_LEFT
                        moveLastPage = !adapter.hasNext()
                        moveFirstPage = false
                        slidingLayout.slideScrollStateChanged(MOVE_TO_LEFT)
                    } else if (distance < 0) {
                        direction = MOVE_TO_RIGHT
                        moveFirstPage = !adapter.hasPrevious()
                        moveLastPage = false
                        slidingLayout.slideScrollStateChanged(MOVE_TO_RIGHT)
                    }
                }

                if (mode == MODE_NONE && (direction == MOVE_TO_LEFT || direction == MOVE_TO_RIGHT)) {
                    mode = MODE_MOVE
                }

                if (mode == MODE_MOVE) {
                    if ((direction == MOVE_TO_LEFT && distance <= 0) || (direction == MOVE_TO_RIGHT && distance >= 0)) {
                        mode = MODE_NONE
                    }
                }

                if (direction != MOVE_NO_RESULT) {
                    if (direction == MOVE_TO_LEFT) {
                        leftScrollerView = getCurrentShowView()
                        rightScrollerView = if (!moveLastPage) getBottomView() else null
                    } else {
                        rightScrollerView = getCurrentShowView()
                        leftScrollerView = if (!moveFirstPage) getTopView() else null
                    }

                    if (mode == MODE_MOVE) {
                        velocityTracker!!.computeCurrentVelocity(1000, maximumFlingVelocity.toFloat())
                        if (direction == MOVE_TO_LEFT) {
                            if (moveLastPage) {
                                leftScrollerView!!.scrollTo(distance / 2, 0)
                            } else {
                                leftScrollerView!!.scrollTo(distance, 0)
                                rightScrollerView!!.scrollTo(-screenWidth + distance, 0)
                            }
                        } else {
                            if (moveFirstPage) {
                                rightScrollerView!!.scrollTo(distance / 2, 0)
                            } else {
                                leftScrollerView!!.scrollTo(screenWidth + distance, 0)
                                rightScrollerView!!.scrollTo(distance, 0)
                            }
                        }
                    } else {
                        var scrollX = 0
                        if (leftScrollerView != null) {
                            scrollX = leftScrollerView!!.scrollX
                        } else if (rightScrollerView != null) {
                            scrollX = rightScrollerView!!.scrollX
                        }

                        if (direction == MOVE_TO_LEFT && scrollX != 0 && adapter.hasNext()) {
                            leftScrollerView!!.scrollTo(0, 0)
                            rightScrollerView?.scrollTo(screenWidth, 0)
                        } else if (direction == MOVE_TO_RIGHT && adapter.hasPrevious() && screenWidth != kotlin.math.abs(scrollX)) {
                            leftScrollerView?.scrollTo(-screenWidth, 0)
                            rightScrollerView!!.scrollTo(0, 0)
                        }
                    }
                }

                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                if ((leftScrollerView == null && direction == MOVE_TO_LEFT) ||
                    (rightScrollerView == null && direction == MOVE_TO_RIGHT)
                ) {
                    return false
                }

                var time = 500

                if (moveFirstPage && rightScrollerView != null) {
                    val rightScrollX = rightScrollerView!!.scrollX
                    scroller.startScroll(
                        rightScrollX,
                        0,
                        -rightScrollX,
                        0,
                        time * kotlin.math.abs(rightScrollX) / screenWidth,
                    )
                    touchResult = MOVE_NO_RESULT
                }

                if (moveLastPage && leftScrollerView != null) {
                    val leftScrollX = leftScrollerView!!.scrollX
                    scroller.startScroll(
                        leftScrollX,
                        0,
                        -leftScrollX,
                        0,
                        time * kotlin.math.abs(leftScrollX) / screenWidth,
                    )
                    touchResult = MOVE_NO_RESULT
                }

                if (!moveLastPage && !moveFirstPage && leftScrollerView != null) {
                    val scrollX = leftScrollerView!!.scrollX
                    velocityValue = velocityTracker!!.xVelocity.toInt()

                    if (mode == MODE_MOVE && direction == MOVE_TO_LEFT) {
                        if (scrollX > limitDistance || velocityValue < -time) {
                            touchResult = MOVE_TO_LEFT
                            if (velocityValue < -time) {
                                val tmpTime = 1000 * 1000 / kotlin.math.abs(velocityValue)
                                time = if (tmpTime > 500) 500 else tmpTime
                            }
                            scroller.startScroll(scrollX, 0, screenWidth - scrollX, 0, time)
                        } else {
                            touchResult = MOVE_NO_RESULT
                            scroller.startScroll(scrollX, 0, -scrollX, 0, time)
                        }
                    } else if (mode == MODE_MOVE && direction == MOVE_TO_RIGHT) {
                        if (screenWidth - scrollX > limitDistance || velocityValue > time) {
                            touchResult = MOVE_TO_RIGHT
                            if (velocityValue > time) {
                                val tmpTime = 1000 * 1000 / kotlin.math.abs(velocityValue)
                                time = if (tmpTime > 500) 500 else tmpTime
                            }
                            scroller.startScroll(scrollX, 0, -scrollX, 0, time)
                        } else {
                            touchResult = MOVE_NO_RESULT
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

    private fun invalidate() {
        slidingLayout.postInvalidate()
    }

    private fun resetVariables() {
        direction = MOVE_NO_RESULT
        mode = MODE_NONE
        startX = 0
        releaseVelocityTracker()
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

    private fun moveToNext(): Boolean {
        if (!adapter.hasNext()) {
            return false
        }

        val previousView = adapter.getPreviousView()
        if (previousView != null) {
            slidingLayout.removeView(previousView)
        }
        var newNextView = previousView

        adapter.moveToNext()
        slidingLayout.slideSelected(adapter.getCurrent())

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
            newNextView!!.scrollTo(-screenWidth, 0)
            slidingLayout.addView(newNextView)
        }

        return true
    }

    private fun moveToPrevious(): Boolean {
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

            newPreviousView!!.scrollTo(screenWidth, 0)
            slidingLayout.addView(newPreviousView)
        }

        return true
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            leftScrollerView?.scrollTo(scroller.currX, scroller.currY)
            rightScrollerView?.let {
                if (moveFirstPage) {
                    it.scrollTo(scroller.currX, scroller.currY)
                } else {
                    it.scrollTo(scroller.currX - screenWidth, scroller.currY)
                }
            }

            invalidate()
        } else if (scroller.isFinished) {
            if (touchResult != MOVE_NO_RESULT) {
                if (touchResult == MOVE_TO_LEFT) {
                    moveToNext()
                } else {
                    moveToPrevious()
                }
                touchResult = MOVE_NO_RESULT
                slidingLayout.slideScrollStateChanged(MOVE_NO_RESULT)
                invalidate()
            }
        }
    }

    override fun slideNext() {
        if (!adapter.hasNext() || !scroller.isFinished) {
            return
        }

        leftScrollerView = getCurrentShowView()
        rightScrollerView = getBottomView()

        scroller.startScroll(0, 0, screenWidth, 0, 500)
        touchResult = MOVE_TO_LEFT

        slidingLayout.slideScrollStateChanged(MOVE_TO_LEFT)
        invalidate()
    }

    override fun slidePrevious() {
        if (!adapter.hasPrevious() || !scroller.isFinished) {
            return
        }

        leftScrollerView = getTopView()
        rightScrollerView = getCurrentShowView()

        scroller.startScroll(screenWidth, 0, -screenWidth, 0, 500)
        touchResult = MOVE_TO_RIGHT

        slidingLayout.slideScrollStateChanged(MOVE_TO_RIGHT)
        invalidate()
    }

    fun getTopView(): View? = adapter.getPreviousView()

    fun getCurrentShowView(): View = adapter.getCurrentView()

    fun getBottomView(): View? = adapter.getNextView()
}
