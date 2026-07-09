@file:Suppress("DEPRECATION")

package org.mewx.wenku8.activity

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import org.mewx.wenku8.BuildConfig
import org.mewx.wenku8.R
import org.mewx.wenku8.util.GoogleServicesHelper

/**
 * About activity.
 */
class AboutActivity : BaseMaterialActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initMaterialStyle(R.layout.layout_about)

        GoogleServicesHelper.initFirebase(this)

        val versionView = findViewById<TextView>(R.id.app_version)
        versionView.text = String.format(
            resources.getString(R.string.about_version_template),
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE
        )
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(menuItem)
    }
}
