@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package org.mewx.wenku8.adapter

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TableRow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nostra13.universalimageloader.core.ImageLoader
import java.io.File
import java.io.UnsupportedEncodingException
import java.util.Collections
import java.util.HashSet
import org.mewx.wenku8.R
import org.mewx.wenku8.api.Wenku8API
import org.mewx.wenku8.api.Wenku8Error
import org.mewx.wenku8.global.GlobalConfig
import org.mewx.wenku8.global.api.NovelItemInfoUpdate
import org.mewx.wenku8.listener.MyItemClickListener
import org.mewx.wenku8.listener.MyItemLongClickListener
import org.mewx.wenku8.listener.MyOptionClickListener
import org.mewx.wenku8.network.LightNetwork
import org.mewx.wenku8.util.LightCache

/**
 * Updated version of Novel Item Adapter.
 */
class NovelItemAdapterUpdate() : RecyclerView.Adapter<NovelItemAdapterUpdate.ViewHolder>() {
    private var itemClickListener: MyItemClickListener? = null
    private var optionClickListener: MyOptionClickListener? = null
    private var itemLongClickListener: MyItemLongClickListener? = null
    private var dataset: MutableList<NovelItemInfoUpdate> = ArrayList()
    private val loadingAids: MutableSet<Int> = Collections.synchronizedSet(HashSet())

    constructor(dataset: List<NovelItemInfoUpdate>) : this() {
        refreshDataset(dataset)
    }

    fun refreshDataset(dataset: List<NovelItemInfoUpdate>) {
        this.dataset = dataset as? MutableList<NovelItemInfoUpdate> ?: dataset.toMutableList()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = View.inflate(viewGroup.context, R.layout.view_novel_item, null)
        return ViewHolder(view, itemClickListener, optionClickListener, itemLongClickListener)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val currentItem = dataset[position]
        val cached = NovelItemInfoUpdate.getFromCache(currentItem.aid)
        if (cached != null) {
            if (
                GlobalConfig.testInBookshelf() &&
                NovelItemInfoUpdate.LOADING_STRING == cached.latest_chapter &&
                NovelItemInfoUpdate.LOADING_STRING != currentItem.latest_chapter
            ) {
                cached.latest_chapter = currentItem.latest_chapter
            }
            dataset[position] = cached
            refreshAllFields(viewHolder, cached)
        } else {
            refreshAllFields(viewHolder, currentItem)
            checkAndLoad(currentItem.aid, position)
        }

        for (offset in 1..10) {
            val nextPosition = position + offset
            if (nextPosition < dataset.size) {
                checkAndLoad(dataset[nextPosition].aid, nextPosition)
            }
        }
    }

    private fun checkAndLoad(aid: Int, position: Int) {
        if (
            dataset[position].isInitialized() &&
            !loadingAids.contains(aid) &&
            NovelItemInfoUpdate.getFromCache(aid) == null
        ) {
            AsyncLoadNovelIntro(aid).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }
    }

    private fun refreshAllFields(viewHolder: ViewHolder, info: NovelItemInfoUpdate) {
        viewHolder.tvNovelTitle.text = info.title
        viewHolder.tvNovelAuthor.text = info.author
        viewHolder.tvNovelStatus.text = info.status
        viewHolder.tvNovelUpdate.text = info.update
        if (!GlobalConfig.testInBookshelf()) {
            viewHolder.tvNovelIntro.text = info.intro_short
        } else if (info.latest_chapter.isEmpty()) {
            viewHolder.tvNovelIntro.visibility = View.GONE
        } else {
            viewHolder.tvLatestChapterNameText.text =
                viewHolder.tvLatestChapterNameText.resources.getText(R.string.novel_item_latest_chapter)
            viewHolder.tvNovelIntro.text = info.latest_chapter
        }

        val defaultCoverPath = GlobalConfig.getDefaultStoragePath() + "imgs" + File.separator + info.aid + ".jpg"
        val backupCoverPath = GlobalConfig.getBackupStoragePath() + "imgs" + File.separator + info.aid + ".jpg"
        when {
            LightCache.testFileExist(defaultCoverPath) ->
                ImageLoader.getInstance().displayImage("file://$defaultCoverPath", viewHolder.ivNovelCover)

            LightCache.testFileExist(backupCoverPath) ->
                ImageLoader.getInstance().displayImage("file://$backupCoverPath", viewHolder.ivNovelCover)

            else ->
                ImageLoader.getInstance().displayImage(Wenku8API.getCoverURL(info.aid), viewHolder.ivNovelCover)
        }
    }

    override fun getItemCount(): Int = dataset.size

    fun setOnItemClickListener(listener: MyItemClickListener?) {
        itemClickListener = listener
    }

    fun setOnDeleteClickListener(listener: MyOptionClickListener?) {
        optionClickListener = listener
    }

    fun setOnItemLongClickListener(listener: MyItemLongClickListener?) {
        itemLongClickListener = listener
    }

    /**
     * View holder called by RecyclerView to display the data at the specified position.
     */
    class ViewHolder(
        itemView: View,
        private val clickListener: MyItemClickListener?,
        private val optionClickListener: MyOptionClickListener?,
        private val longClickListener: MyItemLongClickListener?
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        private val ibNovelOption: ImageButton = itemView.findViewById(R.id.novel_option)
        private val trNovelIntro: TableRow = itemView.findViewById(R.id.novel_intro_row)
        val ivNovelCover: ImageView = itemView.findViewById(R.id.novel_cover)
        val tvNovelTitle: TextView = itemView.findViewById(R.id.novel_title)
        val tvNovelStatus: TextView = itemView.findViewById(R.id.novel_status)
        val tvNovelAuthor: TextView = itemView.findViewById(R.id.novel_author)
        val tvNovelUpdate: TextView = itemView.findViewById(R.id.novel_update)
        val tvNovelIntro: TextView = itemView.findViewById(R.id.novel_intro)
        val tvLatestChapterNameText: TextView = itemView.findViewById(R.id.novel_item_text_shortinfo)

        init {
            itemView.findViewById<View>(R.id.item_card).setOnClickListener(this)
            itemView.findViewById<View>(R.id.item_card).setOnLongClickListener(this)
            itemView.findViewById<View>(R.id.novel_option).setOnClickListener(this)

            if (!GlobalConfig.testInBookshelf()) {
                ibNovelOption.visibility = View.INVISIBLE
            }
        }

        override fun onClick(view: View) {
            when (view.id) {
                R.id.item_card -> clickListener?.onItemClick(view, bindingAdapterPosition)
                R.id.novel_option -> {
                    if (clickListener != null) {
                        optionClickListener?.onOptionButtonClick(view, bindingAdapterPosition)
                    }
                }
            }
        }

        override fun onLongClick(view: View): Boolean {
            longClickListener?.onItemLongClick(view, bindingAdapterPosition)
            return true
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class AsyncLoadNovelIntro(private val aid: Int) :
        AsyncTask<Void, Void, Wenku8Error.ErrorCode>() {
        private var novelIntro: String? = null

        override fun onPreExecute() {
            super.onPreExecute()
            loadingAids.add(aid)
        }

        override fun doInBackground(vararg params: Void?): Wenku8Error.ErrorCode {
            return try {
                val result = LightNetwork.LightHttpPostConnection(
                    Wenku8API.BASE_URL,
                    Wenku8API.getNovelShortInfoUpdate_CV(aid, GlobalConfig.getCurrentLang())
                ) ?: return Wenku8Error.ErrorCode.ERROR_DEFAULT

                novelIntro = String(result, Charsets.UTF_8)
                Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED
            } catch (exception: UnsupportedEncodingException) {
                exception.printStackTrace()
                Wenku8Error.ErrorCode.ERROR_DEFAULT
            }
        }

        override fun onPostExecute(errorCode: Wenku8Error.ErrorCode) {
            super.onPostExecute(errorCode)
            loadingAids.remove(aid)

            if (errorCode == Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED) {
                val currentIndex = dataset.indexOfFirst { it.aid == aid }
                if (currentIndex >= 0) {
                    val info = novelIntro?.let { NovelItemInfoUpdate.parse(it) }
                    if (info != null) {
                        NovelItemInfoUpdate.putToCache(info)
                        dataset[currentIndex] = info
                        notifyItemChanged(currentIndex)
                    }
                }
            }
        }
    }
}
