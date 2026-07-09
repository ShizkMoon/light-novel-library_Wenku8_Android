@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package org.mewx.wenku8.activity

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.analytics.FirebaseAnalytics
import com.nostra13.universalimageloader.core.ImageLoader
import org.mewx.wenku8.R
import org.mewx.wenku8.fragment.NovelItemListFragment
import org.mewx.wenku8.global.GlobalConfig
import org.mewx.wenku8.util.GoogleServicesHelper

/**
 * Search result activity.
 */
class SearchResultActivity : BaseMaterialActivity() {
    private var firebaseAnalytics: FirebaseAnalytics? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initMaterialStyle(R.layout.layout_search_result, StatusBarColor.WHITE)
        installFadeBackCallback()

        firebaseAnalytics = GoogleServicesHelper.initFirebase(this)

        val searchKey = intent.getStringExtra("key").orEmpty()
        val searchParams = Bundle().apply {
            putString(FirebaseAnalytics.Param.SEARCH_TERM, searchKey)
        }
        GoogleServicesHelper.logEvent(firebaseAnalytics, FirebaseAnalytics.Event.SEARCH, searchParams)

        findViewById<TextView?>(R.id.search_result_title)?.text =
            resources.getString(R.string.title_search) + searchKey

        val bundle = Bundle().apply {
            putString("type", "search")
            putString("key", searchKey)
        }

        if (!ImageLoader.getInstance().isInited) {
            GlobalConfig.initImageLoader(this)
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.result_fragment, NovelItemListFragment.newInstance(bundle), "fragment")
            .setTransitionStyle(FragmentTransaction.TRANSIT_NONE)
            .commit()
    }

    override fun onResume() {
        super.onResume()

        val upArrow = resources.getDrawable(R.drawable.ic_svg_back)
        if (supportActionBar != null) {
            upArrow.setColorFilter(resources.getColor(R.color.mySearchToggleColor), PorterDuff.Mode.SRC_ATOP)
            supportActionBar?.setHomeAsUpIndicator(upArrow)
        }
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(menuItem)
    }
}
