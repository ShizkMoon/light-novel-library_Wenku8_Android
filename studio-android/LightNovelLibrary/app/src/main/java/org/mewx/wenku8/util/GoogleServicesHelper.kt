package org.mewx.wenku8.util

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.analytics.FirebaseAnalytics

class GoogleServicesHelper private constructor() {
    companion object {
        private val TAG = GoogleServicesHelper::class.java.simpleName
        private var cachedGmsAvailable: Boolean? = null

        /** Check if GMS environment is actually available. */
        @JvmStatic
        fun isGmsAvailable(context: Context): Boolean {
            val cached = cachedGmsAvailable
            if (cached != null) {
                return cached
            }

            val resultCode = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(context.applicationContext)
            val available = resultCode == ConnectionResult.SUCCESS
            cachedGmsAvailable = available
            Log.d(TAG, "GMS Availability: $available (Code: $resultCode)")
            return available
        }

        /** Safely initialize AdMob. */
        @JvmStatic
        fun initAdMob(context: Context) {
            if (isGmsAvailable(context)) {
                Thread {
                    try {
                        MobileAds.initialize(context) {
                            Log.d(TAG, "AdMob Initialized")
                        }
                    } catch (exception: Exception) {
                        Log.e(TAG, "AdMob init failed even with GMS", exception)
                    }
                }.start()
            } else {
                Log.w(TAG, "Skipping AdMob init: GMS not available")
            }
        }

        /** Safely initialize Firebase Analytics for the activity entry default logging. */
        @JvmStatic
        fun initFirebase(context: Context): FirebaseAnalytics? {
            if (isGmsAvailable(context)) {
                try {
                    return FirebaseAnalytics.getInstance(context.applicationContext)
                } catch (exception: Exception) {
                    Log.e(TAG, "Firebase init failed", exception)
                }
            }
            return null
        }

        /** Safely log event to Firebase Analytics. */
        @JvmStatic
        fun logEvent(firebaseAnalytics: FirebaseAnalytics?, eventName: String, params: Bundle?) {
            if (firebaseAnalytics != null) {
                firebaseAnalytics.logEvent(eventName, params)
            } else {
                Log.v(TAG, "Firebase event dropped: $eventName")
            }
        }
    }
}
