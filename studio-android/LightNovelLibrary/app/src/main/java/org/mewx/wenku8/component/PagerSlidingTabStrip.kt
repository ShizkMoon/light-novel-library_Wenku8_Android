@file:Suppress("DEPRECATION")

/*
 * Copyright (C) 2013 Andreas Stuetz <andreas.stuetz@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mewx.wenku8.component

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.database.DataSetObserver
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.viewpager.widget.ViewPager
import org.mewx.wenku8.R
import java.util.Locale
import kotlin.math.max

class PagerSlidingTabStrip @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : HorizontalScrollView(context, attrs, defStyle) {

    interface CustomTabProvider {
        fun getCustomTabView(parent: ViewGroup, position: Int): View
    }

    interface OnTabReselectedListener {
        fun onTabReselected(position: Int)
    }

    private val adapterObserver = PagerAdapterObserver()
    private val pageListener = PageListener()

    private var tabReselectedListener: OnTabReselectedListener? = null
    var delegatePageListener: ViewPager.OnPageChangeListener? = null

    private val tabsContainer = LinearLayout(context)
    private var pager: ViewPager? = null

    private var tabCount = 0
    private var currentPosition = 0
    private var currentPositionOffset = 0f

    private val rectPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    private val dividerPaint = Paint().apply {
        isAntiAlias = true
    }

    private var indicatorColor = 0
    private var indicatorHeight = 2
    private var underlineHeight = 0
    private var underlineColor = 0
    private var dividerWidth = 0
    private var dividerPadding = 0
    private var dividerColor = 0
    private var tabPadding = 12
    private var tabTextSize = 14
    private var tabTextColor: ColorStateList? = null
    private var tabTextAlpha = HALF_TRANSP
    private var tabTextSelectedAlpha = OPAQUE
    private var padding = 0
    private var shouldExpand = false
    private var textAllCaps = true
    private var isPaddingMiddle = false
    private var tabTypeface: Typeface? = null
    private var tabTypefaceStyle = Typeface.BOLD
    private var tabTypefaceSelectedStyle = Typeface.BOLD
    private var scrollOffset = 0
    private var lastScrollX = 0
    private var tabBackgroundResId = R.drawable.bg_tab
    private var locale: Locale? = null

    private val defaultTabLayoutParams = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.MATCH_PARENT,
    )
    private val expandedTabLayoutParams = LinearLayout.LayoutParams(
        0,
        ViewGroup.LayoutParams.MATCH_PARENT,
        1.0f,
    )

    private val firstTabGlobalLayoutListener = object : OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            val view = tabsContainer.getChildAt(0) ?: return
            viewTreeObserver.removeOnGlobalLayoutListener(this)

            if (isPaddingMiddle) {
                val halfWidthFirstTab = view.width / 2
                padding = width / 2 - halfWidthFirstTab
            }
            setPadding(padding, paddingTop, padding, paddingBottom)
            if (scrollOffset == 0) {
                scrollOffset = width / 2 - padding
            }
        }
    }

    init {
        isFillViewport = true
        setWillNotDraw(false)

        tabsContainer.orientation = LinearLayout.HORIZONTAL
        tabsContainer.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )
        addView(tabsContainer)

        val dm = resources.displayMetrics
        scrollOffset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, scrollOffset.toFloat(), dm).toInt()
        indicatorHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, indicatorHeight.toFloat(), dm).toInt()
        underlineHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, underlineHeight.toFloat(), dm).toInt()
        dividerPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dividerPadding.toFloat(), dm).toInt()
        tabPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, tabPadding.toFloat(), dm).toInt()
        dividerWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dividerWidth.toFloat(), dm).toInt()
        tabTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, tabTextSize.toFloat(), dm).toInt()

        var typedArray = context.obtainStyledAttributes(attrs, ATTRS)
        tabTextSize = typedArray.getDimensionPixelSize(TEXT_SIZE_INDEX, tabTextSize)
        val colorStateList = typedArray.getColorStateList(TEXT_COLOR_INDEX)
        val textPrimaryColor = typedArray.getColor(
            TEXT_COLOR_PRIMARY,
            resources.getColor(android.R.color.white),
        )
        tabTextColor = colorStateList ?: getColorStateList(textPrimaryColor)

        underlineColor = textPrimaryColor
        dividerColor = textPrimaryColor
        indicatorColor = textPrimaryColor
        val paddingLeft = typedArray.getDimensionPixelSize(PADDING_LEFT_INDEX, padding)
        val paddingRight = typedArray.getDimensionPixelSize(PADDING_RIGHT_INDEX, padding)
        typedArray.recycle()

        padding = max(paddingLeft, paddingRight)

        typedArray = context.obtainStyledAttributes(attrs, R.styleable.PagerSlidingTabStrip)
        indicatorColor = typedArray.getColor(R.styleable.PagerSlidingTabStrip_pstsIndicatorColor, indicatorColor)
        underlineColor = typedArray.getColor(R.styleable.PagerSlidingTabStrip_pstsUnderlineColor, underlineColor)
        dividerColor = typedArray.getColor(R.styleable.PagerSlidingTabStrip_pstsDividerColor, dividerColor)
        dividerWidth = typedArray.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsDividerWidth, dividerWidth)
        indicatorHeight = typedArray.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsIndicatorHeight, indicatorHeight)
        underlineHeight = typedArray.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsUnderlineHeight, underlineHeight)
        dividerPadding = typedArray.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsDividerPadding, dividerPadding)
        tabPadding = typedArray.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsTabPaddingLeftRight, tabPadding)
        tabBackgroundResId = typedArray.getResourceId(R.styleable.PagerSlidingTabStrip_pstsTabBackground, tabBackgroundResId)
        shouldExpand = typedArray.getBoolean(R.styleable.PagerSlidingTabStrip_pstsShouldExpand, shouldExpand)
        scrollOffset = typedArray.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsScrollOffset, scrollOffset)
        textAllCaps = typedArray.getBoolean(R.styleable.PagerSlidingTabStrip_pstsTextAllCaps, textAllCaps)
        isPaddingMiddle = typedArray.getBoolean(R.styleable.PagerSlidingTabStrip_pstsPaddingMiddle, isPaddingMiddle)
        tabTypefaceStyle = typedArray.getInt(R.styleable.PagerSlidingTabStrip_pstsTextStyle, Typeface.BOLD)
        tabTypefaceSelectedStyle = typedArray.getInt(R.styleable.PagerSlidingTabStrip_pstsTextSelectedStyle, Typeface.BOLD)
        tabTextAlpha = typedArray.getFloat(R.styleable.PagerSlidingTabStrip_pstsTextAlpha, HALF_TRANSP)
        tabTextSelectedAlpha = typedArray.getFloat(R.styleable.PagerSlidingTabStrip_pstsTextSelectedAlpha, OPAQUE)
        typedArray.recycle()

        setMarginBottomTabContainer()
        dividerPaint.strokeWidth = dividerWidth.toFloat()

        if (locale == null) {
            locale = resources.configuration.locale
        }
    }

    private fun setMarginBottomTabContainer() {
        val mlp = tabsContainer.layoutParams as ViewGroup.MarginLayoutParams
        val bottomMargin = if (indicatorHeight >= underlineHeight) indicatorHeight else underlineHeight
        mlp.setMargins(mlp.leftMargin, mlp.topMargin, mlp.rightMargin, bottomMargin)
        tabsContainer.layoutParams = mlp
    }

    fun setViewPager(pager: ViewPager) {
        this.pager = pager
        val adapter = pager.adapter ?: throw IllegalStateException("ViewPager does not have adapter instance.")

        pager.setOnPageChangeListener(pageListener)
        adapter.registerDataSetObserver(adapterObserver)
        adapterObserver.isAttached = true
        notifyDataSetChanged()
    }

    fun notifyDataSetChanged() {
        val viewPager = pager ?: return
        val adapter = viewPager.adapter ?: return
        tabsContainer.removeAllViews()
        tabCount = adapter.count

        for (i in 0 until tabCount) {
            val tabView = if (adapter is CustomTabProvider) {
                adapter.getCustomTabView(this, i)
            } else {
                LayoutInflater.from(context).inflate(R.layout.view_tab, this, false)
            }
            addTab(i, adapter.getPageTitle(i), tabView)
        }

        updateTabStyles()
        viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            @SuppressLint("NewApi")
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                currentPosition = viewPager.currentItem
                currentPositionOffset = 0f
                scrollToChild(currentPosition, 0)
                updateSelection(currentPosition)
            }
        })
    }

    private fun addTab(position: Int, title: CharSequence?, tabView: View) {
        val textView = tabView.findViewById<TextView>(R.id.tab_title)
        if (textView != null) {
            if (title != null) {
                textView.text = title
            }
            val alpha = if (pager?.currentItem == position) tabTextSelectedAlpha else tabTextAlpha
            ViewCompat.setAlpha(textView, alpha)
        }

        tabView.isFocusable = true
        tabView.setOnClickListener {
            val viewPager = pager ?: return@setOnClickListener
            if (viewPager.currentItem != position) {
                notSelected(tabsContainer.getChildAt(viewPager.currentItem))
                viewPager.currentItem = position
            } else {
                tabReselectedListener?.onTabReselected(position)
            }
        }

        tabsContainer.addView(
            tabView,
            position,
            if (shouldExpand) expandedTabLayoutParams else defaultTabLayoutParams,
        )
    }

    private fun updateTabStyles() {
        val viewPager = pager ?: return
        for (i in 0 until tabCount) {
            val view = tabsContainer.getChildAt(i)
            view.setBackgroundResource(tabBackgroundResId)
            view.setPadding(tabPadding, view.paddingTop, tabPadding, view.paddingBottom)

            val tabTitle = view.findViewById<TextView>(R.id.tab_title)
            if (tabTitle != null) {
                tabTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabTextSize.toFloat())
                tabTitle.setTypeface(
                    tabTypeface,
                    if (viewPager.currentItem == i) tabTypefaceSelectedStyle else tabTypefaceStyle,
                )
                tabTextColor?.let(tabTitle::setTextColor)
                if (textAllCaps) {
                    tabTitle.isAllCaps = true
                }
            }
        }
    }

    private fun scrollToChild(position: Int, offset: Int) {
        if (tabCount == 0) {
            return
        }

        var newScrollX = tabsContainer.getChildAt(position).left + offset
        if (position > 0 || offset > 0) {
            newScrollX -= scrollOffset
            val lines = getIndicatorCoordinates()
            newScrollX += ((lines.second - lines.first) / 2f).toInt()
        }

        if (newScrollX != lastScrollX) {
            lastScrollX = newScrollX
            scrollTo(newScrollX, 0)
        }
    }

    private fun getIndicatorCoordinates(): Pair<Float, Float> {
        val currentTab = tabsContainer.getChildAt(currentPosition)
        var lineLeft = currentTab.left.toFloat()
        var lineRight = currentTab.right.toFloat()

        if (currentPositionOffset > 0f && currentPosition < tabCount - 1) {
            val nextTab = tabsContainer.getChildAt(currentPosition + 1)
            val nextTabLeft = nextTab.left.toFloat()
            val nextTabRight = nextTab.right.toFloat()

            lineLeft = currentPositionOffset * nextTabLeft + (1f - currentPositionOffset) * lineLeft
            lineRight = currentPositionOffset * nextTabRight + (1f - currentPositionOffset) * lineRight
        }

        return lineLeft to lineRight
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (isPaddingMiddle || padding > 0) {
            tabsContainer.minimumWidth = width
            clipToPadding = false
        }

        if (tabsContainer.childCount > 0) {
            tabsContainer.getChildAt(0).viewTreeObserver.addOnGlobalLayoutListener(firstTabGlobalLayoutListener)
        }

        super.onLayout(changed, l, t, r, b)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isInEditMode || tabCount == 0) {
            return
        }

        val viewHeight = height.toFloat()

        rectPaint.color = indicatorColor
        val lines = getIndicatorCoordinates()
        canvas.drawRect(
            lines.first + padding,
            viewHeight - indicatorHeight,
            lines.second + padding,
            viewHeight,
            rectPaint,
        )

        rectPaint.color = underlineColor
        canvas.drawRect(
            padding.toFloat(),
            viewHeight - underlineHeight,
            tabsContainer.width + padding.toFloat(),
            viewHeight,
            rectPaint,
        )

        if (dividerWidth != 0) {
            dividerPaint.strokeWidth = dividerWidth.toFloat()
            dividerPaint.color = dividerColor
            for (i in 0 until tabCount - 1) {
                val tab = tabsContainer.getChildAt(i)
                canvas.drawLine(
                    tab.right.toFloat(),
                    dividerPadding.toFloat(),
                    tab.right.toFloat(),
                    viewHeight - dividerPadding,
                    dividerPaint,
                )
            }
        }
    }

    fun setOnTabReselectedListener(tabReselectedListener: OnTabReselectedListener?) {
        this.tabReselectedListener = tabReselectedListener
    }

    fun setOnPageChangeListener(listener: ViewPager.OnPageChangeListener?) {
        delegatePageListener = listener
    }

    private inner class PageListener : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            currentPosition = position
            currentPositionOffset = positionOffset
            val offset = if (tabCount > 0) {
                (positionOffset * tabsContainer.getChildAt(position).width).toInt()
            } else {
                0
            }
            scrollToChild(position, offset)
            invalidate()
            delegatePageListener?.onPageScrolled(position, positionOffset, positionOffsetPixels)
        }

        override fun onPageScrollStateChanged(state: Int) {
            val viewPager = pager ?: return
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                scrollToChild(viewPager.currentItem, 0)
            }

            selected(tabsContainer.getChildAt(viewPager.currentItem))
            if (viewPager.currentItem - 1 >= 0) {
                notSelected(tabsContainer.getChildAt(viewPager.currentItem - 1))
            }
            if (viewPager.currentItem + 1 <= (viewPager.adapter?.count ?: 0) - 1) {
                notSelected(tabsContainer.getChildAt(viewPager.currentItem + 1))
            }

            delegatePageListener?.onPageScrollStateChanged(state)
        }

        override fun onPageSelected(position: Int) {
            updateSelection(position)
            delegatePageListener?.onPageSelected(position)
        }
    }

    private fun updateSelection(position: Int) {
        for (i in 0 until tabCount) {
            val tab = tabsContainer.getChildAt(i)
            val selected = i == position
            tab.isSelected = selected
            if (selected) {
                selected(tab)
            } else {
                notSelected(tab)
            }
        }
    }

    private fun notSelected(tab: View?) {
        val title = tab?.findViewById<TextView>(R.id.tab_title)
        if (title != null) {
            title.setTypeface(tabTypeface, tabTypefaceStyle)
            ViewCompat.setAlpha(title, tabTextAlpha)
        }
    }

    private fun selected(tab: View?) {
        val title = tab?.findViewById<TextView>(R.id.tab_title)
        if (title != null) {
            title.setTypeface(tabTypeface, tabTypefaceSelectedStyle)
            ViewCompat.setAlpha(title, tabTextSelectedAlpha)
        }
    }

    private inner class PagerAdapterObserver : DataSetObserver() {
        var isAttached = false

        override fun onChanged() {
            notifyDataSetChanged()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val viewPager = pager ?: return
        val adapter = viewPager.adapter ?: return
        if (!adapterObserver.isAttached) {
            adapter.registerDataSetObserver(adapterObserver)
            adapterObserver.isAttached = true
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        val viewPager = pager ?: return
        val adapter = viewPager.adapter ?: return
        if (adapterObserver.isAttached) {
            try {
                adapter.unregisterDataSetObserver(adapterObserver)
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
            adapterObserver.isAttached = false
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as? SavedState
        if (savedState == null) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(savedState.superState)
        currentPosition = savedState.currentPosition
        if (currentPosition != 0 && tabsContainer.childCount > 0) {
            notSelected(tabsContainer.getChildAt(0))
            selected(tabsContainer.getChildAt(currentPosition))
        }
        requestLayout()
    }

    override fun onSaveInstanceState(): Parcelable {
        val savedState = SavedState(super.onSaveInstanceState())
        savedState.currentPosition = currentPosition
        return savedState
    }

    internal class SavedState : BaseSavedState {
        var currentPosition = 0

        constructor(superState: Parcelable?) : super(superState)

        private constructor(parcel: Parcel) : super(parcel) {
            currentPosition = parcel.readInt()
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeInt(currentPosition)
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(parcel: Parcel): SavedState = SavedState(parcel)

                override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
            }
        }
    }

    fun getIndicatorColor(): Int = indicatorColor

    fun getIndicatorHeight(): Int = indicatorHeight

    fun getUnderlineColor(): Int = underlineColor

    fun getDividerColor(): Int = dividerColor

    fun getDividerWidth(): Int = dividerWidth

    fun getUnderlineHeight(): Int = underlineHeight

    fun getDividerPadding(): Int = dividerPadding

    fun getScrollOffset(): Int = scrollOffset

    fun getShouldExpand(): Boolean = shouldExpand

    fun getTextSize(): Int = tabTextSize

    fun isTextAllCaps(): Boolean = textAllCaps

    fun getTextColor(): ColorStateList? = tabTextColor

    fun getTabBackground(): Int = tabBackgroundResId

    fun getTabPaddingLeftRight(): Int = tabPadding

    fun setIndicatorColor(indicatorColor: Int) {
        this.indicatorColor = indicatorColor
        invalidate()
    }

    fun setIndicatorColorResource(resId: Int) {
        indicatorColor = resources.getColor(resId)
        invalidate()
    }

    fun setIndicatorHeight(indicatorLineHeightPx: Int) {
        indicatorHeight = indicatorLineHeightPx
        invalidate()
    }

    fun setUnderlineColor(underlineColor: Int) {
        this.underlineColor = underlineColor
        invalidate()
    }

    fun setUnderlineColorResource(resId: Int) {
        underlineColor = resources.getColor(resId)
        invalidate()
    }

    fun setDividerColor(dividerColor: Int) {
        this.dividerColor = dividerColor
        invalidate()
    }

    fun setDividerColorResource(resId: Int) {
        dividerColor = resources.getColor(resId)
        invalidate()
    }

    fun setDividerWidth(dividerWidthPx: Int) {
        dividerWidth = dividerWidthPx
        invalidate()
    }

    fun setUnderlineHeight(underlineHeightPx: Int) {
        underlineHeight = underlineHeightPx
        invalidate()
    }

    fun setDividerPadding(dividerPaddingPx: Int) {
        dividerPadding = dividerPaddingPx
        invalidate()
    }

    fun setScrollOffset(scrollOffsetPx: Int) {
        scrollOffset = scrollOffsetPx
        invalidate()
    }

    fun setShouldExpand(shouldExpand: Boolean) {
        this.shouldExpand = shouldExpand
        if (pager != null) {
            requestLayout()
        }
    }

    fun setAllCaps(textAllCaps: Boolean) {
        this.textAllCaps = textAllCaps
    }

    fun setTextSize(textSizePx: Int) {
        tabTextSize = textSizePx
        updateTabStyles()
    }

    fun setTextColor(textColor: Int) {
        setTextColor(getColorStateList(textColor))
    }

    private fun getColorStateList(textColor: Int): ColorStateList {
        return ColorStateList(arrayOf(intArrayOf()), intArrayOf(textColor))
    }

    fun setTextColor(colorStateList: ColorStateList?) {
        tabTextColor = colorStateList
        updateTabStyles()
    }

    fun setTextColorResource(resId: Int) {
        setTextColor(resources.getColor(resId))
    }

    fun setTextColorStateListResource(resId: Int) {
        setTextColor(resources.getColorStateList(resId))
    }

    fun setTypeface(typeface: Typeface?, style: Int) {
        tabTypeface = typeface
        tabTypefaceSelectedStyle = style
        updateTabStyles()
    }

    fun setTabBackground(resId: Int) {
        tabBackgroundResId = resId
    }

    fun setTabPaddingLeftRight(paddingPx: Int) {
        tabPadding = paddingPx
        updateTabStyles()
    }

    companion object {
        private const val OPAQUE = 1.0f
        private const val HALF_TRANSP = 0.5f

        private val ATTRS = intArrayOf(
            android.R.attr.textColorPrimary,
            android.R.attr.textSize,
            android.R.attr.textColor,
            android.R.attr.paddingLeft,
            android.R.attr.paddingRight,
        )

        private const val TEXT_COLOR_PRIMARY = 0
        private const val TEXT_SIZE_INDEX = 1
        private const val TEXT_COLOR_INDEX = 2
        private const val PADDING_LEFT_INDEX = 3
        private const val PADDING_RIGHT_INDEX = 4
    }
}
