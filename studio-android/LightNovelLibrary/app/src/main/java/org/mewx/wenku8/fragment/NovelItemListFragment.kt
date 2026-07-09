@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package org.mewx.wenku8.fragment

import android.content.ContentValues
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import java.util.concurrent.atomic.AtomicBoolean
import java.util.regex.Pattern
import org.mewx.wenku8.R
import org.mewx.wenku8.activity.NovelInfoActivity
import org.mewx.wenku8.adapter.NovelItemAdapterUpdate
import org.mewx.wenku8.api.Wenku8API
import org.mewx.wenku8.global.GlobalConfig
import org.mewx.wenku8.global.api.NovelItemInfoUpdate
import org.mewx.wenku8.global.api.Wenku8Parser
import org.mewx.wenku8.listener.MyItemClickListener
import org.mewx.wenku8.listener.MyItemLongClickListener
import org.mewx.wenku8.network.LightNetwork

class NovelItemListFragment : Fragment(), MyItemClickListener, MyItemLongClickListener {
    private var listType = ""
    private var searchKey = ""
    private val isLoading = AtomicBoolean(false)

    private var actionBar: ActionBar? = null
    private var layoutManager: LinearLayoutManager? = null
    private var recyclerView: RecyclerView? = null
    private var searchProgressBar: LinearProgressIndicator? = null

    private var listNovelItemAid: MutableList<Int> = ArrayList()
    private var listNovelItemInfo: MutableList<NovelItemInfoUpdate> = ArrayList()
    private var adapter: NovelItemAdapterUpdate? = null

    private var currentPage = 1
    private var totalPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = requireArguments()
        listType = args.getString("type").orEmpty()
        searchKey = if (listType == SEARCH_TYPE) args.getString("key").orEmpty() else ""

        actionBar = (activity as? AppCompatActivity)?.supportActionBar
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.fragment_novel_item_list, container, false)
        rootView.tag = listType

        rootView.findViewById<View>(R.id.relay_warning).setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(resources.getString(R.string.system_warning))
                .setMessage(resources.getString(R.string.relay_warning_full))
                .setPositiveButton(R.string.dialog_positive_ok, null)
                .show()
        }

        if (totalPage == 0) {
            currentPage = 1
        }

        val newLayoutManager = LinearLayoutManager(activity).apply {
            orientation = LinearLayoutManager.VERTICAL
        }
        layoutManager = newLayoutManager

        val newRecyclerView = rootView.findViewById<RecyclerView>(R.id.novel_item_list).apply {
            setHasFixedSize(false)
            itemAnimator = DefaultItemAnimator()
            layoutManager = newLayoutManager
        }
        recyclerView = newRecyclerView

        if (listNovelItemInfo.isNotEmpty()) {
            adapter = NovelItemAdapterUpdate(listNovelItemInfo).also {
                it.setOnItemClickListener(this)
                it.setOnItemLongClickListener(this)
                newRecyclerView.adapter = it
            }
        } else if (listType == SEARCH_TYPE) {
            searchProgressBar = requireActivity().findViewById(R.id.spb)
            searchProgressBar?.visibility = View.VISIBLE

            Toast.makeText(activity, "search", Toast.LENGTH_SHORT).show()
            AsyncGetSearchResultList().execute(searchKey)
        } else {
            newRecyclerView.addOnScrollListener(NovelListScrollListener())
            newRecyclerView.addOnScrollListener(OnHidingScrollListener())
            AsyncGetNovelItemList().execute(currentPage)
        }
        return rootView
    }

    override fun onItemClick(view: View?, position: Int) {
        if (position < 0 || position >= listNovelItemAid.size) {
            Toast.makeText(
                activity,
                "ArrayIndexOutOfBoundsException: $position in size ${listNovelItemAid.size}",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val intent = Intent(activity, NovelInfoActivity::class.java).apply {
            putExtra("aid", listNovelItemAid[position])
            putExtra("from", "list")
            putExtra("title", view?.findViewById<TextView>(R.id.novel_title)?.text)
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
        // Empty, preserving existing long-click behavior.
    }

    private inner class OnHidingScrollListener : RecyclerView.OnScrollListener() {
        private var toolbarMarginOffset = 0

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val toolbar = actionBar ?: return
            toolbarMarginOffset += dy
            if (toolbarMarginOffset > toolbar.height) {
                toolbar.hide()
            }
            if (toolbarMarginOffset == 0) {
                toolbar.show()
            }
        }
    }

    private fun refreshPartialIdList(newNovelItemAids: List<Int>?) {
        if (newNovelItemAids.isNullOrEmpty()) {
            return
        }

        listNovelItemAid.addAll(newNovelItemAids)
        val startIndex = listNovelItemInfo.size

        newNovelItemAids.forEach { aid ->
            listNovelItemInfo.add(NovelItemInfoUpdate(aid))
        }

        val currentAdapter = adapter ?: NovelItemAdapterUpdate().also {
            adapter = it
            it.setOnItemClickListener(this)
            it.setOnItemLongClickListener(this)
        }
        currentAdapter.refreshDataset(listNovelItemInfo)

        if (currentPage == 1 && recyclerView != null) {
            recyclerView?.adapter = currentAdapter
        } else {
            currentAdapter.notifyItemRangeInserted(startIndex, newNovelItemAids.size)
        }
    }

    private fun refreshEntireIdList() {
        listNovelItemInfo.clear()

        listNovelItemAid.forEach { aid ->
            listNovelItemInfo.add(NovelItemInfoUpdate(aid))
        }

        val currentAdapter = adapter ?: NovelItemAdapterUpdate().also {
            adapter = it
            it.setOnItemClickListener(this)
            it.setOnItemLongClickListener(this)
        }
        currentAdapter.refreshDataset(listNovelItemInfo)

        if (currentPage == 1 && recyclerView != null) {
            recyclerView?.adapter = currentAdapter
        } else {
            currentAdapter.notifyDataSetChanged()
        }
    }

    private inner class NovelListScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val manager = layoutManager ?: return
            val visibleItemCount = manager.childCount
            val totalItemCount = manager.itemCount
            val pastVisibleItems = manager.findFirstVisibleItemPosition()

            if (!isLoading.get()) {
                if (
                    visibleItemCount + pastVisibleItems + 2 >= totalItemCount &&
                    (totalPage == 0 || currentPage < totalPage)
                ) {
                    Snackbar.make(
                        recyclerView,
                        resources.getString(R.string.list_loading) +
                            "(" + (currentPage + 1) + "/" + totalPage + ")",
                        Snackbar.LENGTH_SHORT
                    ).show()

                    AsyncGetNovelItemList().execute(currentPage + 1)
                }
            }
        }
    }

    private inner class AsyncGetNovelItemList : AsyncTask<Int, Int, Int>() {
        private var usingWenku8Relay = false
        private val tempNovelList: MutableList<Int> = ArrayList()
        private val raceCondition = !isLoading.compareAndSet(false, true)

        override fun doInBackground(vararg params: Int?): Int {
            if (raceCondition) {
                Log.d("MewX", "doInBackground: blocking change")
                return -1
            }

            currentPage = params[0] ?: currentPage

            val contentValues: ContentValues = Wenku8API.getNovelList(
                Wenku8API.getNovelSortedBy(listType),
                currentPage
            )
            val bytes = LightNetwork.LightHttpPostConnection(Wenku8API.BASE_URL, contentValues)
                ?: return -1

            Log.d("MewX", "doInBackground: loading page $currentPage")
            tempNovelList.clear()
            tempNovelList.addAll(Wenku8Parser.parseNovelItemList(String(bytes, Charsets.UTF_8)))

            if (tempNovelList.isEmpty()) {
                Log.d("MewX", "in AsyncGetNovelItemList: doInBackground: tempNovelList == null || tempNovelList.size() == 0")
                return 0
            }

            totalPage = tempNovelList[0]
            tempNovelList.removeAt(0)
            return 0
        }

        override fun onPostExecute(result: Int) {
            isLoading.set(false)

            if (!isAdded || activity == null) {
                return
            }

            if (result == -1) {
                return
            }
            if (tempNovelList.isEmpty()) {
                Log.d("MewX", "in AsyncGetNovelItemList: onPostExecute: tempNovelList == null || tempNovelList.size() == 0")
                return
            }

            refreshPartialIdList(tempNovelList)

            val relayWarningView = activity?.findViewById<View>(R.id.relay_warning)
            relayWarningView?.visibility = if (usingWenku8Relay) View.VISIBLE else View.GONE
        }
    }

    private inner class AsyncGetSearchResultList : AsyncTask<String, Int, Int>() {
        override fun doInBackground(vararg params: String?): Int {
            val query = params[0].orEmpty()

            var contentValues = Wenku8API.searchNovelByNovelName(query, GlobalConfig.getCurrentLang())
            val titleBytes = LightNetwork.LightHttpPostConnection(Wenku8API.BASE_URL, contentValues)
                ?: return -1

            val titleResults = ArrayList<Int>()
            Log.d("MewX", String(titleBytes, Charsets.UTF_8))
            titleResults.addAll(extractAidList(String(titleBytes, Charsets.UTF_8)))

            contentValues = Wenku8API.searchNovelByAuthorName(query, GlobalConfig.getCurrentLang())
            val nameBytes = LightNetwork.LightHttpPostConnection(Wenku8API.BASE_URL, contentValues)
                ?: return -1

            val authorResults = ArrayList<Int>()
            Log.d("MewX", String(nameBytes, Charsets.UTF_8))
            authorResults.addAll(extractAidList(String(nameBytes, Charsets.UTF_8), logEach = true))

            listNovelItemAid = ArrayList<Int>().apply {
                addAll(titleResults)
                removeAll(authorResults.toSet())
                addAll(authorResults)
            }
            return 0
        }

        override fun onPostExecute(result: Int) {
            super.onPostExecute(result)

            if (!isAdded || activity == null) {
                return
            }

            searchProgressBar?.visibility = View.INVISIBLE
            if (result == -1) {
                Toast.makeText(activity, resources.getString(R.string.system_network_error), Toast.LENGTH_LONG).show()
                return
            }
            if (listNovelItemAid.isEmpty()) {
                Toast.makeText(activity, resources.getString(R.string.task_null), Toast.LENGTH_LONG).show()
                return
            }

            refreshEntireIdList()
        }

        private fun extractAidList(source: String, logEach: Boolean = false): List<Int> {
            val result = ArrayList<Int>()
            val matcher = AID_PATTERN.matcher(source)
            while (matcher.find()) {
                val aid = matcher.group(1)?.toIntOrNull()
                if (aid != null) {
                    result.add(aid)
                    if (logEach) {
                        Log.d("MewX", aid.toString())
                    }
                }
            }
            return result
        }
    }

    override fun onDetach() {
        super.onDetach()
        actionBar?.show()
    }

    companion object {
        private const val SEARCH_TYPE = "search"
        private val AID_PATTERN = Pattern.compile("aid='(.*)'")

        @JvmStatic
        fun newInstance(args: Bundle): NovelItemListFragment =
            NovelItemListFragment().apply {
                arguments = args
            }
    }
}
