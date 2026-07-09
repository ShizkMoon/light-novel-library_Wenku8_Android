package org.mewx.wenku8

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import org.mewx.wenku8.api.Wenku8API
import org.mewx.wenku8.util.GoogleServicesHelper

/**
 * Application entry point and legacy global context provider.
 */
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        context = getApplicationContextLocal()

        Wenku8API.AppVer = BuildConfig.VERSION_NAME
        GoogleServicesHelper.initAdMob(this)
    }

    fun getApplicationContextLocal(): Context = applicationContext

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var context: Context? = null

        @JvmStatic
        fun getContext(): Context = requireNotNull(context) {
            "Application context is not initialized"
        }
    }
}
