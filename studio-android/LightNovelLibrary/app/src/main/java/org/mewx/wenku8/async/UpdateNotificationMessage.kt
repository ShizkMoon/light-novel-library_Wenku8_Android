@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package org.mewx.wenku8.async

import android.os.AsyncTask
import android.util.Log
import org.mewx.wenku8.api.Wenku8API
import org.mewx.wenku8.global.GlobalConfig
import org.mewx.wenku8.network.LightNetwork

/**
 * Async task used for checking new notification texts.
 */
class UpdateNotificationMessage : AsyncTask<Void, Void, String?>() {
    override fun doInBackground(vararg voids: Void?): String? {
        val codeByte = LightNetwork.LightHttpDownload(
            if (GlobalConfig.getCurrentLang() != Wenku8API.AppLanguage.SC) {
                GlobalConfig.noticeCheckTc
            } else {
                GlobalConfig.noticeCheckSc
            }
        )

        if (codeByte == null) {
            Log.e(UpdateNotificationMessage::class.java.simpleName, "unable to get notification text")
            return null
        }

        return String(codeByte).trim()
    }

    override fun onPostExecute(notice: String?) {
        super.onPostExecute(notice)

        if (notice.isNullOrEmpty()) {
            Log.e(UpdateNotificationMessage::class.java.simpleName, "received empty notification text")
            return
        }

        Log.i("MewX", "received notification text: $notice")
        Wenku8API.NoticeString = notice
        GlobalConfig.writeTheNotice(notice)
    }
}
