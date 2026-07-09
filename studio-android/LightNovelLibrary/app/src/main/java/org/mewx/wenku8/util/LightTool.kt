@file:Suppress("DEPRECATION")

package org.mewx.wenku8.util

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.view.WindowManager

/**
 * Utility tools collected for legacy Java callers and new Kotlin code.
 */
class LightTool private constructor() {
    companion object {
        private var displayCutout = Rect(0, 0, 0, 0)

        @JvmStatic
        fun isInteger(value: String?): Boolean {
            if (value == null) {
                return false
            }

            return try {
                value.toInt()
                true
            } catch (_: NumberFormatException) {
                false
            }
        }

        @JvmStatic
        fun isDouble(value: String?): Boolean {
            if (value == null) {
                return false
            }

            return try {
                value.toDouble()
                value.contains(".")
            } catch (_: NumberFormatException) {
                false
            }
        }

        @JvmStatic
        fun isNumber(value: String?): Boolean = isInteger(value) || isDouble(value)

        @JvmStatic
        fun getNavigationBarSize(context: Context): Point {
            val appUsableSize = getAppUsableScreenSize(context)
            val realScreenSize = getRealScreenSize(context)

            if (appUsableSize.x < realScreenSize.x) {
                return Point(realScreenSize.x - appUsableSize.x, appUsableSize.y)
            }

            if (appUsableSize.y < realScreenSize.y) {
                return Point(appUsableSize.x, realScreenSize.y - appUsableSize.y)
            }

            return Point()
        }

        @JvmStatic
        fun getAppUsableScreenSize(context: Context): Point {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val size = Point()
            windowManager.defaultDisplay.getSize(size)
            return size
        }

        @JvmStatic
        fun setDisplayCutout(rect: Rect) {
            displayCutout = rect
        }

        @JvmStatic
        fun getDisplayCutout(): Rect = Rect(displayCutout)

        @JvmStatic
        fun getRealScreenSize(context: Context): Point {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val size = Point()
            windowManager.defaultDisplay.getRealSize(size)
            return size
        }

        @JvmStatic
        fun getStatusBarHeightValue(context: Context): Int {
            val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
            return if (resourceId > 0) {
                context.resources.getDimensionPixelSize(resourceId)
            } else {
                0
            }
        }

        @JvmStatic
        fun getNavigationBarHeightValue(context: Context): Int {
            val resourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
            return if (resourceId > 0) {
                context.resources.getDimensionPixelSize(resourceId)
            } else {
                0
            }
        }

        @JvmStatic
        fun dip2px(context: Context, dpValue: Float): Int {
            val scale = context.resources.displayMetrics.density
            return (dpValue * scale + 0.5f).toInt()
        }

        @JvmStatic
        fun px2dip(context: Context, pxValue: Float): Int {
            val scale = context.resources.displayMetrics.density
            return (pxValue / scale + 0.5f).toInt()
        }

        @JvmStatic
        fun px2sp(context: Context, px: Float): Float {
            val scaledDensity = context.resources.displayMetrics.scaledDensity
            return px / scaledDensity
        }

        @JvmStatic
        fun sp2px(context: Context, sp: Float): Float {
            val scaledDensity = context.resources.displayMetrics.scaledDensity
            return sp * scaledDensity
        }
    }
}
