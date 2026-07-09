@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package org.mewx.wenku8.activity

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import java.lang.ref.WeakReference
import java.nio.charset.Charset
import java.util.concurrent.atomic.AtomicBoolean
import org.mewx.wenku8.R
import org.mewx.wenku8.adapter.ReviewItemAdapter
import org.mewx.wenku8.api.Wenku8API
import org.mewx.wenku8.global.api.ReviewList
import org.mewx.wenku8.global.api.Wenku8Parser
import org.mewx.wenku8.listener.MyItemClickListener
import org.mewx.wenku8.network.LightNetwork
import org.mewx.wenku8.network.LightUserSession
import org.mewx.wenku8.util.GoogleServicesHelper

/**
 * Novel review list activity.
 */
class NovelReviewListActivity : BaseMaterialActivity(), MyItemClickListener {
    private var aid = 1

    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var loadingLayout: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingStatusTextView: TextView
    private lateinit var loadingButton: TextView

    private val reviewList = ReviewList()
    private val adapter = ReviewItemAdapter(reviewList)

    private var pastVisibleItems = 0
    private var visibleItemCount = 0
    private var totalItemCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initMaterialStyle(R.layout.layout_novel_review_list)

        GoogleServicesHelper.initFirebase(this)

        aid = intent.getIntExtra("aid", 1)

        loadingLayout = findViewById(R.id.list_loading)
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        recyclerView = findViewById(R.id.review_item_list)
        loadingStatusTextView = findViewById(R.id.list_loading_status)
        loadingButton = findViewById(R.id.btn_loading)

        recyclerView.setHasFixedSize(false)
        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.addOnScrollListener(ReviewScrollListener())

        loadingButton.setOnClickListener {
            AsyncReviewListLoader(this, swipeRefreshLayout, aid, reviewList).execute()
        }

        swipeRefreshLayout.setColorSchemeColors(resources.getColor(R.color.myAccentColor))

        adapter.setOnItemClickListener(this)
        recyclerView.adapter = adapter
        swipeRefreshLayout.setOnRefreshListener { reloadAllReviews() }
    }

    private fun reloadAllReviews() {
        reviewList.resetList()
        AsyncReviewListLoader(this, swipeRefreshLayout, aid, reviewList).execute()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_review_list, menu)
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) {
            onBackPressed()
        } else if (menuItem.itemId == R.id.action_new) {
            if (!LightUserSession.getLogStatus()) {
                Toast.makeText(this, R.string.system_not_logged_in, Toast.LENGTH_SHORT).show()
                return true
            }
            val intent = Intent(this, NovelReviewNewPostActivity::class.java)
            intent.putExtra("aid", aid)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(menuItem)
    }

    override fun onResume() {
        super.onResume()
        reloadAllReviews()
    }

    internal fun getAdapter(): ReviewItemAdapter = adapter

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

    override fun onItemClick(view: View?, position: Int) {
        val review = reviewList.list[position]
        val intent = Intent(this, NovelReviewReplyListActivity::class.java)
        intent.putExtra("rid", review.rid)
        intent.putExtra("title", review.title)
        startActivity(intent)
    }

    private inner class ReviewScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            visibleItemCount = layoutManager.childCount
            totalItemCount = layoutManager.itemCount
            pastVisibleItems = layoutManager.findFirstVisibleItemPosition()

            if (!isLoading.get() && visibleItemCount + pastVisibleItems >= totalItemCount) {
                if (reviewList.currentPage < reviewList.totalPage) {
                    Snackbar.make(
                        this@NovelReviewListActivity.recyclerView,
                        resources.getString(R.string.list_loading) +
                            "(" + (reviewList.currentPage + 1) + "/" + reviewList.totalPage + ")",
                        Snackbar.LENGTH_SHORT
                    ).show()

                    AsyncReviewListLoader(
                        this@NovelReviewListActivity,
                        swipeRefreshLayout,
                        aid,
                        reviewList
                    ).execute()
                }
            }
        }
    }

    private class AsyncReviewListLoader(
        novelReviewListActivity: NovelReviewListActivity,
        swipeRefreshLayout: SwipeRefreshLayout,
        private val aid: Int,
        private val reviewList: ReviewList
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
            if (!runOrNot || reviewList.currentPage + 1 > reviewList.totalPage) {
                return null
            }

            val xmlBytes = LightNetwork.LightHttpPostConnection(
                Wenku8API.BASE_URL,
                Wenku8API.getCommentListParams(aid, reviewList.currentPage + 1)
            )
            if (xmlBytes == null) {
                metNetworkIssue = true
                return null
            }

            val xml = String(xmlBytes, Charset.forName("UTF-8"))
            Log.d(NovelReviewListActivity::class.java.simpleName, xml)
            Wenku8Parser.parseReviewList(reviewList, xml)
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
                activity.getAdapter().notifyItemRangeChanged(0, reviewList.list.size)
                activity.hideListLoading()
            }

            swipeRefreshLayoutWeakReference.get()?.isRefreshing = false
            isLoading.set(false)
        }
    }

    companion object {
        private val isLoading = AtomicBoolean(false)
    }
}
