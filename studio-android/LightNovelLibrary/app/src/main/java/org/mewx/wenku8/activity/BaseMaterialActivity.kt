@file:Suppress("DEPRECATION")

package org.mewx.wenku8.activity

import android.graphics.Color
import android.graphics.PorterDuff
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import org.mewx.wenku8.R

/**
 * Base activity that handles the legacy Material-styled toolbar and system bars.
 */
open class BaseMaterialActivity : AppCompatActivity() {
    protected enum class HomeIndicatorStyle {
        NONE,
        HAMBURGER,
        ARROW
    }

    protected enum class StatusBarColor {
        PRIMARY,
        WHITE,
        DARK
    }

    private var toolbar: Toolbar? = null

    protected fun installFadeBackCallback() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finishWithFadeTransition()
                }
            },
        )
    }

    protected fun finishWithFadeTransition() {
        finish()
        overridePendingTransition(0, R.anim.fade_out)
    }

    protected fun getToolbar(): Toolbar? {
        if (toolbar == null) {
            toolbar = findViewById(R.id.toolbar_actionbar)
        }
        return toolbar
    }

    /**
     * Sets the status bar color to black with the given alpha (0.0 = transparent, 1.0 = opaque).
     */
    protected fun setStatusBarAlpha(alpha: Float) {
        window.statusBarColor = Color.argb((alpha * 255).toInt(), 0, 0, 0)
    }

    /**
     * Sets the navigation bar color to black with the given alpha (0.0 = transparent, 1.0 = opaque).
     */
    protected fun setNavigationBarAlpha(alpha: Float) {
        window.navigationBarColor = Color.argb((alpha * 255).toInt(), 0, 0, 0)
    }

    protected fun initMaterialStyle(layoutId: Int) {
        initMaterialStyle(layoutId, HomeIndicatorStyle.ARROW)
    }

    protected fun initMaterialStyle(layoutId: Int, indicatorStyle: HomeIndicatorStyle) {
        initMaterialStyle(layoutId, StatusBarColor.PRIMARY, indicatorStyle)
    }

    protected fun initMaterialStyle(layoutId: Int, statusBarColor: StatusBarColor) {
        initMaterialStyle(layoutId, statusBarColor, HomeIndicatorStyle.ARROW)
    }

    protected fun initMaterialStyle(
        layoutId: Int,
        statusBarColor: StatusBarColor,
        indicatorStyle: HomeIndicatorStyle
    ) {
        setContentView(layoutId)

        getToolbar()?.let { setSupportActionBar(it) }

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)

            if (indicatorStyle == HomeIndicatorStyle.ARROW) {
                val upArrow = ContextCompat.getDrawable(this@BaseMaterialActivity, R.drawable.ic_svg_back)
                upArrow?.setColorFilter(
                    ContextCompat.getColor(this@BaseMaterialActivity, R.color.default_white),
                    PorterDuff.Mode.SRC_ATOP
                )
                setHomeAsUpIndicator(upArrow)
            }
        }

        val statusBarAlpha = if (statusBarColor == StatusBarColor.DARK) 0.9f else 0.15f
        setStatusBarAlpha(statusBarAlpha)

        if (statusBarColor == StatusBarColor.DARK) {
            setNavigationBarAlpha(0.8f)
        } else {
            val navBarColorId = if (statusBarColor == StatusBarColor.PRIMARY) {
                R.color.myNavigationColor
            } else {
                R.color.myNavigationColorWhite
            }
            window.navigationBarColor = ContextCompat.getColor(this, navBarColorId)
        }
    }
}
