@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package org.mewx.wenku8.fragment

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.util.concurrent.atomic.AtomicBoolean
import org.mewx.wenku8.R
import org.mewx.wenku8.activity.MainActivity
import org.mewx.wenku8.activity.NovelInfoActivity
import org.mewx.wenku8.adapter.NovelItemAdapterUpdate
import org.mewx.wenku8.api.Wenku8API
import org.mewx.wenku8.async.CheckAppNewVersion
import org.mewx.wenku8.global.GlobalConfig
import org.mewx.wenku8.global.api.NovelItemInfoUpdate
import org.mewx.wenku8.global.api.custom.NovelListWithInfoParser
import org.mewx.wenku8.listener.MyItemClickListener
import org.mewx.wenku8.listener.MyItemLongClickListener
import org.mewx.wenku8.network.LightNetwork

class LatestFragment : Fragment(), MyItemClickListener, MyItemLongClickListener {
    private var mainActivity: MainActivity? = null
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var novelItemListView: RecyclerView
    private lateinit var loadingStatusTextView: TextView
    private lateinit var loadingProgressBar: View
    private lateinit var reloadButton: TextView
    private lateinit var checkUpdateButton: View
    private lateinit var listLoadingView: View
    private lateinit var relayWarningView: View

    private var listNovelItemInfo: MutableList<NovelItemInfoUpdate> = ArrayList()
    private var adapter: NovelItemAdapterUpdate? = null
    private var currentPage = 0
    private var totalPage = 0

    private val isLoading = AtomicBoolean(false)
    private var pastVisibleItems = 0
    private var visibleItemCount = 0
    private var totalItemCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listNovelItemInfo = ArrayList()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.fragment_latest, container, false)

        novelItemListView = rootView.findViewById(R.id.novel_item_list)
        loadingStatusTextView = rootView.findViewById(R.id.list_loading_status)
        loadingProgressBar = rootView.findViewById(R.id.google_progress)
        reloadButton = rootView.findViewById(R.id.btn_loading)
        checkUpdateButton = rootView.findViewById(R.id.btn_check_update_home)
        listLoadingView = rootView.findViewById(R.id.list_loading)
        relayWarningView = rootView.findViewById(R.id.relay_warning)

        relayWarningView.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(resources.getString(R.string.system_warning))
                .setMessage(resources.getString(R.string.relay_warning_full))
                .setPositiveButton(R.string.dialog_positive_ok, null)
                .show()
        }

        novelItemListView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(activity)
        novelItemListView.layoutManager = layoutManager
        novelItemListView.addOnScrollListener(LatestScrollListener())

        reloadButton.setOnClickListener {
            if (!isLoading.compareAndSet(true, false)) {
                currentPage = 1
                totalPage = 1
                loadNovelList(currentPage)
            }
        }

        checkUpdateButton.setOnClickListener {
            CheckAppNewVersion(requireActivity(), true).execute()
        }

        currentPage = 1
        totalPage = 1
        isLoading.set(false)
        loadNovelList(currentPage)

        return rootView
    }

    private fun loadNovelList(page: Int) {
        if (!isLoading.compareAndSet(false, true)) {
            return
        }
        hideRetryButton()

        AsyncLoadLatestList().execute(
            Wenku8API.getMewxNovelList(
                Wenku8API.NovelSortedBy.lastUpdate,
                page,
                GlobalConfig.getCurrentLang()
            )
        )
    }

    override fun onItemClick(view: View?, position: Int) {
        if (position < 0 || position >= listNovelItemInfo.size) {
            Toast.makeText(
                activity,
                "ArrayIndexOutOfBoundsException: $position in size ${listNovelItemInfo.size}",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val item = listNovelItemInfo[position]
        val intent = Intent(activity, NovelInfoActivity::class.java).apply {
            putExtra("aid", item.aid)
            putExtra("from", "latest")
            putExtra("title", item.title)
        }

        if (view == null) {
            startActivity(intent)
            return
        }

        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            requireActivity(),
            Pair.create(view.findViewById(R.id.novel_cover), "novel_cover"),
            Pair.create(view.findViewById(R.id.novel_title), "novel_title")
        )
        ActivityCompat.startActivity(requireActivity(), intent, options.toBundle())
    }

    override fun onItemLongClick(view: View?, position: Int) {
        onItemClick(view, position)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as? MainActivity
    }

    override fun onDetach() {
        super.onDetach()
        mainActivity = null
        isLoading.set(false)
    }

    override fun onPause() {
        super.onPause()
        GlobalConfig.LeaveLatest()
    }

    override fun onResume() {
        super.onResume()
        GlobalConfig.EnterLatest()
    }

    private fun showRetryButton() {
        if (!isAdded) {
            return
        }

        reloadButton.text = resources.getString(R.string.task_retry)
        reloadButton.visibility = View.VISIBLE
        loadingProgressBar.visibility = View.GONE
        checkUpdateButton.visibility = View.VISIBLE
    }

    private fun hideRetryButton() {
        if (!isAdded) {
            return
        }

        loadingStatusTextView.text = resources.getString(R.string.list_loading)
        loadingProgressBar.visibility = View.VISIBLE
        reloadButton.visibility = View.GONE
        checkUpdateButton.visibility = View.GONE
    }

    private inner class LatestScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            visibleItemCount = layoutManager.childCount
            totalItemCount = layoutManager.itemCount
            pastVisibleItems = layoutManager.findFirstVisibleItemPosition()

            if (!isLoading.get() && visibleItemCount + pastVisibleItems + 3 >= totalItemCount) {
                if (currentPage <= totalPage) {
                    Snackbar.make(
                        novelItemListView,
                        resources.getString(R.string.list_loading) + "($currentPage/$totalPage)",
                        Snackbar.LENGTH_SHORT
                    ).show()

                    loadNovelList(currentPage)
                } else if (isLoading.compareAndSet(false, true)) {
                    Snackbar.make(
                        novelItemListView,
                        resources.getText(R.string.loading_done),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private inner class AsyncLoadLatestList :
        AsyncTask<ContentValues, Int, List<NovelItemInfoUpdate>?>() {
        private var usingWenku8Relay = false
        private var numOfItemsToRefresh = 0

        override fun doInBackground(vararg params: ContentValues?): List<NovelItemInfoUpdate>? {
            val requestParams = params.firstOrNull() ?: return null
            val newItems = ArrayList<NovelItemInfoUpdate>()

            val resultBytes = LightNetwork.LightHttpPostConnection(Wenku8API.BASE_URL, requestParams)
                ?: return null
            val json = String(resultBytes, Charsets.UTF_8)
            val result = NovelListWithInfoParser.parse(json)
            if (result == null || result.items.isEmpty()) {
                return null
            }

            totalPage = result.pageNum
            newItems.addAll(result.items)
            return newItems
        }

        override fun onPostExecute(result: List<NovelItemInfoUpdate>?) {
            if (result == null) {
                if (!isAdded) {
                    return
                }

                loadingStatusTextView.text = resources.getString(R.string.system_parse_failed)
                showRetryButton()
                isLoading.set(false)
                return
            }

            listNovelItemInfo.addAll(result)
            numOfItemsToRefresh = result.size

            if (adapter == null || novelItemListView.adapter == null) {
                val novelAdapter = NovelItemAdapterUpdate(listNovelItemInfo)
                adapter = novelAdapter
                novelAdapter.setOnItemClickListener(this@LatestFragment)
                novelAdapter.setOnItemLongClickListener(this@LatestFragment)
                novelItemListView.adapter = novelAdapter
            }

            if (numOfItemsToRefresh != 0) {
                adapter?.notifyItemRangeInserted(
                    listNovelItemInfo.size - numOfItemsToRefresh,
                    numOfItemsToRefresh
                )
            }

            currentPage++
            isLoading.set(false)

            if (!isAdded || mainActivity == null) {
                return
            }

            listLoadingView.visibility = View.GONE
            relayWarningView.visibility = if (usingWenku8Relay) View.VISIBLE else View.GONE
        }
    }
}
