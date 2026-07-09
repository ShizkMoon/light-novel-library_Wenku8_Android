@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package org.mewx.wenku8.async

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.ref.WeakReference
import org.mewx.wenku8.BuildConfig
import org.mewx.wenku8.R
import org.mewx.wenku8.global.GlobalConfig
import org.mewx.wenku8.network.LightNetwork

class CheckAppNewVersion @JvmOverloads constructor(
    context: Context,
    private val verboseMode: Boolean = false
) : AsyncTask<Void, Void, Int>() {
    private val contextWeakReference = WeakReference(context)

    override fun doInBackground(vararg voids: Void?): Int {
        val codeByte = LightNetwork.LightHttpDownload(GlobalConfig.versionCheckUrl) ?: return -1

        val code = String(codeByte).trim()
        Log.d("MewX", "latest version code: $code")
        return if (code.isEmpty() || !TextUtils.isDigitsOnly(code)) {
            -2
        } else {
            code.toInt()
        }
    }

    override fun onPostExecute(code: Int) {
        super.onPostExecute(code)

        val ctx = contextWeakReference.get() ?: return

        if (code < 0) {
            if (code == -1) {
                Log.e("MewX", "unable to fetch latest version")
            } else if (code == -2) {
                Log.e("MewX", "unable to parse version")
            }

            if (verboseMode) {
                Toast.makeText(
                    ctx,
                    ctx.resources.getString(R.string.system_update_timeout),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val current = BuildConfig.VERSION_CODE
        if (current >= code) {
            Log.i("MewX", "no newer version")
            if (verboseMode) {
                Toast.makeText(
                    ctx,
                    ctx.resources.getString(R.string.system_update_latest_version),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else if (ctx is Activity) {
            if (!ctx.isFinishing && !ctx.isDestroyed && ctx.hasWindowFocus()) {
                MaterialAlertDialogBuilder(ctx)
                    .setTitle(R.string.system_update_found_new)
                    .setMessage(R.string.system_update_jump_to_page)
                    .setPositiveButton(R.string.dialog_positive_sure) { _, _ ->
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(GlobalConfig.blogPageUrl))
                        ctx.startActivity(browserIntent)
                    }
                    .setNegativeButton(R.string.dialog_negative_biao, null)
                    .show()
            }
        }
    }
}
