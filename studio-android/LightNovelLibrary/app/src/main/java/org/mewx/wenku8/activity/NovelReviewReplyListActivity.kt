@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package org.mewx.wenku8.activity

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import java.lang.ref.WeakReference
import java.nio.charset.Charset
import java.util.concurrent.atomic.AtomicBoolean
import org.mewx.wenku8.R
import org.mewx.wenku8.adapter.ReviewReplyItemAdapter
import org.mewx.wenku8.api.Wenku8API
import org.mewx.wenku8.global.api.ReviewReplyList
import org.mewx.wenku8.global.api.Wenku8Parser
import org.mewx.wenku8.listener.MyItemLongClickListener
import org.mewx.wenku8.network.LightNetwork
import org.mewx.wenku8.network.LightUserSession
import org.mewx.wenku8.util.GoogleServicesHelper

/**
 * Novel review reply list activity.
 */
class NovelReviewReplyListActivity : BaseMaterialActivity(), MyItemLongClickListener {
    private var rid = 1
    private var reviewTitle: String? = ""

    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var loadingLayout: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingStatusTextView: TextView
    private lateinit var loadingButton: TextView
    private lateinit var replyText: EditText

    private var adapter: ReviewReplyItemAdapter? = null
    private var reviewReplyList = ReviewReplyList()

    private var pastVisibleItems = 0
    private var visibleItemCount = 0
    private var totalItemCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initMaterialStyle(R.layout.layout_novel_review_reply_list)

        GoogleServicesHelper.initFirebase(this)

        rid = intent.getIntExtra("rid", 1)
        reviewTitle = intent.getStringExtra("title")

        loadingLayout = findViewById(R.id.list_loading)
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        recyclerView = findViewById(R.id.review_item_list)
        loadingStatusTextView = findViewById(R.id.list_loading_status)
        loadingButton = findViewById(R.id.btn_loading)
        replyText = findViewById(R.id.review_reply_edit_text)
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finishIfReplyDraftIsEmpty()
                }
            },
        )
        val replyButton: LinearLayout = findViewById(R.id.review_reply_send)

        recyclerView.setHasFixedSize(false)
        val horizontalDecoration = DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL)
        val horizontalDivider = ContextCompat.getDrawable(application, R.drawable.divider_horizontal)
        if (horizontalDivider != null) {
            horizontalDecoration.setDrawable(horizontalDivider)
            recyclerView.addItemDecoration(horizontalDecoration)
        }

        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.addOnScrollListener(ReplyScrollListener())

        loadingButton.setOnClickListener {
            AsyncReviewReplyListLoader(this, swipeRefreshLayout, rid, reviewReplyList).execute()
        }

        swipeRefreshLayout.setColorSchemeColors(resources.getColor(R.color.myAccentColor))
        swipeRefreshLayout.setOnRefreshListener { refreshReviewReplyList() }

        replyButton.setOnClickListener {
            if (!LightUserSession.getLogStatus()) {
                Toast.makeText(this, R.string.system_not_logged_in, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val content = replyText.text.toString()
            val badWord = Wenku8API.searchBadWords(content)
            if (badWord != null) {
                Toast.makeText(
                    application,
                    String.format(resources.getString(R.string.system_containing_bad_word), badWord),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (content.length < Wenku8API.MIN_REPLY_TEXT) {
                Toast.makeText(
                    application,
                    resources.getString(R.string.system_review_too_short),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                hideIme()
                AsyncPublishReply(replyText, this, rid, content).execute()
            }
        }

        AsyncReviewReplyListLoader(this, swipeRefreshLayout, rid, reviewReplyList).execute()
    }

    internal fun refreshReviewReplyList() {
        reviewReplyList = ReviewReplyList()
        adapter = null
        AsyncReviewReplyListLoader(this, swipeRefreshLayout, rid, reviewReplyList).execute()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val title = reviewTitle
        if (title != null && title.isNotEmpty()) {
            supportActionBar?.title = title
        }
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(menuItem)
    }

    private fun finishIfReplyDraftIsEmpty() {
        val content = replyText.text.toString().trim()
        if (content.isEmpty()) {
            finish()
        }
    }

    internal fun getAdapter(): ReviewReplyItemAdapter? = adapter

    internal fun setAdapter(adapter: ReviewReplyItemAdapter) {
        this.adapter = adapter
    }

    internal fun getRecyclerView(): RecyclerView = recyclerView

    internal fun showRetryButton() {
        loadingStatusTextView.text = resources.getString(R.string.system_parse_failed)
        loadingButton.visibility = View.VISIBLE
    }

    internal fun hideRetryButton() {
        loadingStatusTextView.text = resources.getString(R.string.list_loading)
        loadingButton.visibility = View.GONE
    }

    internal fun hideListLoading() {
        hideRetryButton()
        loadingLayout.visibility = View.GONE
    }

    override fun onItemLongClick(view: View?, position: Int) {
        val content = reviewReplyList.list[position].content
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText(resources.getString(R.string.app_name), content)
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip)
            Toast.makeText(
                this,
                String.format(resources.getString(R.string.system_copied_to_clipboard), content),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun hideIme() {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
        val view = currentFocus ?: View(this)
        inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private inner class ReplyScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            visibleItemCount = layoutManager.childCount
            totalItemCount = layoutManager.itemCount
            pastVisibleItems = layoutManager.findFirstVisibleItemPosition()

            if (!isLoading.get() && visibleItemCount + pastVisibleItems >= totalItemCount) {
                if (reviewReplyList.currentPage < reviewReplyList.totalPage) {
                    Snackbar.make(
                        this@NovelReviewReplyListActivity.recyclerView,
                        resources.getString(R.string.list_loading) +
                            "(" + (reviewReplyList.currentPage + 1) + "/" + reviewReplyList.totalPage + ")",
                        Snackbar.LENGTH_SHORT
                    ).show()

                    AsyncReviewReplyListLoader(
                        this@NovelReviewReplyListActivity,
                        swipeRefreshLayout,
                        rid,
                        reviewReplyList
                    ).execute()
                }
            }
        }
    }

    private class AsyncReviewReplyListLoader(
        novelReviewListActivity: NovelReviewReplyListActivity,
        swipeRefreshLayout: SwipeRefreshLayout,
        private val rid: Int,
        private val reviewReplyList: ReviewReplyList
    ) : AsyncTask<Void, Void, Void?>() {
        private val activityWeakReference = WeakReference(novelReviewListActivity)
        private val swipeRefreshLayoutWeakReference = WeakReference(swipeRefreshLayout)

        private var runOrNot = true
        private var metNetworkIssue = false

        override fun onPreExecute() {
            if (isLoading.getAndSet(true)) {
                runOrNot = false
            } else {
                swipeRefreshLayoutWeakReference.get()?.isRefreshing = true
                activityWeakReference.get()?.hideRetryButton()
            }
        }

        override fun doInBackground(vararg v: Void?): Void? {
            if (!runOrNot || reviewReplyList.currentPage + 1 > reviewReplyList.totalPage) {
                return null
            }

            val xmlBytes = LightNetwork.LightHttpPostConnection(
                Wenku8API.BASE_URL,
                Wenku8API.getCommentContentParams(rid, reviewReplyList.currentPage + 1)
            )
            if (xmlBytes == null) {
                metNetworkIssue = true
                return null
            }

            val xml = String(xmlBytes, Charset.forName("UTF-8"))
            Log.d(NovelReviewReplyListActivity::class.java.simpleName, xml)
            Wenku8Parser.parseReviewReplyList(reviewReplyList, xml)
            return null
        }

        override fun onPostExecute(v: Void?) {
            if (!runOrNot) {
                return
            }

            val activity = activityWeakReference.get()
            if (metNetworkIssue) {
                activity?.showRetryButton()
            } else if (activity != null) {
                if (activity.getAdapter() == null) {
                    val reviewReplyItemAdapter = ReviewReplyItemAdapter(reviewReplyList)
                    activity.setAdapter(reviewReplyItemAdapter)
                    reviewReplyItemAdapter.setOnItemLongClickListener(activity)
                    activity.getRecyclerView().adapter = reviewReplyItemAdapter
                }
                activity.getAdapter()?.notifyDataSetChanged()
                activity.hideListLoading()
            }

            swipeRefreshLayoutWeakReference.get()?.isRefreshing = false
            isLoading.set(false)
        }
    }

    private class AsyncPublishReply(
        editText: EditText,
        activity: NovelReviewReplyListActivity,
        private val rid: Int,
        private val content: String
    ) : AsyncTask<String, Void, Int>() {
        private val editTextWeakReference = WeakReference(editText)
        private val activityWeakReference = WeakReference(activity)

        private var runOrNot = true

        override fun onPreExecute() {
            if (isPublishing.getAndSet(true)) {
                runOrNot = false
            } else {
                editTextWeakReference.get()?.isEnabled = false
            }
        }

        override fun doInBackground(vararg strings: String?): Int {
            val xmlBytes = LightNetwork.LightHttpPostConnection(
                Wenku8API.BASE_URL,
                Wenku8API.getCommentReplyParams(rid, content)
            ) ?: return 0
            val xml = String(xmlBytes, Charset.forName("UTF-8")).trim()
            Log.d(NovelReviewReplyListActivity::class.java.simpleName, xml)
            return xml.toInt()
        }

        override fun onPostExecute(result: Int) {
            if (!runOrNot) {
                return
            }

            val editText = editTextWeakReference.get()
            val activity = activityWeakReference.get()
            when (result) {
                1 -> {
                    editText?.setText("")
                    activity?.refreshReviewReplyList()
                }
                11 -> {
                    if (activity != null) {
                        Toast.makeText(
                            activity,
                            activity.resources.getString(R.string.system_post_locked),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                else -> {
                    if (activity != null) {
                        Toast.makeText(
                            activity,
                            activity.resources.getString(R.string.system_network_error),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            editText?.isEnabled = true
            isPublishing.set(false)
        }

        companion object {
            private val isPublishing = AtomicBoolean(false)
        }
    }

    companion object {
        private val isLoading = AtomicBoolean(false)
    }
}
