@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package org.mewx.wenku8.activity

import android.app.Activity
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.ref.WeakReference
import java.nio.charset.Charset
import java.util.concurrent.atomic.AtomicBoolean
import org.mewx.wenku8.R
import org.mewx.wenku8.api.Wenku8API
import org.mewx.wenku8.network.LightNetwork
import org.mewx.wenku8.network.LightUserSession
import org.mewx.wenku8.util.GoogleServicesHelper

/**
 * Novel review new post activity.
 */
class NovelReviewNewPostActivity : BaseMaterialActivity() {
    private lateinit var titleView: EditText
    private lateinit var contentView: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initMaterialStyle(R.layout.layout_novel_review_new_post)

        GoogleServicesHelper.initFirebase(this)

        aid = intent.getIntExtra("aid", 1)

        titleView = findViewById(R.id.input_title)
        contentView = findViewById(R.id.input_content)
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    confirmDiscardDraftOrFinish()
                }
            },
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_review_new_post, menu)
        return true
    }

    private fun noBadWords(text: String): Boolean {
        val badWord = Wenku8API.searchBadWords(text)
        return if (badWord != null) {
            Toast.makeText(
                application,
                String.format(resources.getString(R.string.system_containing_bad_word), badWord),
                Toast.LENGTH_SHORT
            ).show()
            false
        } else if (text.length < Wenku8API.MIN_REPLY_TEXT) {
            Toast.makeText(
                application,
                resources.getString(R.string.system_review_too_short),
                Toast.LENGTH_SHORT
            ).show()
            false
        } else {
            true
        }
    }

    private fun hideIME() {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
        val view = currentFocus ?: View(this)
        inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        } else if (menuItem.itemId == R.id.action_submit) {
            if (!LightUserSession.getLogStatus()) {
                Toast.makeText(this, R.string.system_not_logged_in, Toast.LENGTH_SHORT).show()
                return true
            }

            val title = titleView.text.toString()
            val content = contentView.text.toString()
            if (noBadWords(title) && noBadWords(content)) {
                hideIME()
                AsyncSubmitNewPost(this, title, content).execute()
            }
        }
        return super.onOptionsItemSelected(menuItem)
    }

    private fun confirmDiscardDraftOrFinish() {
        if (titleView.text.toString().trim().isNotEmpty() ||
            contentView.text.toString().trim().isNotEmpty()
        ) {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.system_warning)
                .setMessage(R.string.system_review_draft_will_be_lost)
                .setPositiveButton(R.string.dialog_positive_ok) { _, _ ->
                    finish()
                }
                .setNegativeButton(R.string.dialog_negative_preferno, null)
                .show()
        } else {
            finish()
        }
    }

    private class AsyncSubmitNewPost(
        activity: NovelReviewNewPostActivity,
        private val title: String,
        private val content: String
    ) : AsyncTask<Void, Void, Int?>() {
        private val activityWeakReference = WeakReference(activity)
        private var ran = false

        override fun onPreExecute() {
            super.onPreExecute()

            if (!isSubmitting.getAndSet(true)) {
                ran = true
                Log.d(
                    NovelReviewNewPostActivity::class.java.simpleName,
                    String.format("start submitting: [%s] %s", title, content)
                )
            }
        }

        override fun doInBackground(vararg voids: Void?): Int? {
            if (!ran) {
                return null
            }

            val response = LightNetwork.LightHttpPostConnection(
                Wenku8API.BASE_URL,
                Wenku8API.getCommentNewThreadParams(aid, title, content)
            ) ?: return 0
            val xml = String(response, Charset.forName("UTF-8")).trim()
            Log.d(NovelReviewNewPostActivity::class.java.simpleName, xml)
            return xml.toInt()
        }

        override fun onPostExecute(errorCode: Int?) {
            super.onPostExecute(errorCode)
            if (!ran) {
                return
            }

            val activity = activityWeakReference.get()
            if (errorCode == null || errorCode != 1) {
                if (activity != null) {
                    Toast.makeText(
                        activity,
                        activity.resources.getString(R.string.system_network_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }

            isSubmitting.set(false)
            Log.d(NovelReviewNewPostActivity::class.java.simpleName, "finished submitting")
            activity?.finish()
        }
    }

    companion object {
        private var aid: Int = 1
        private val isSubmitting = AtomicBoolean(false)
    }
}
