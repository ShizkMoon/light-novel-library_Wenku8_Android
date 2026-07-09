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
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.regex.Pattern
import org.mewx.wenku8.MyApp
import org.mewx.wenku8.R
import org.mewx.wenku8.activity.NovelInfoActivity
import org.mewx.wenku8.adapter.NovelItemAdapterUpdate
import org.mewx.wenku8.api.Wenku8API
import org.mewx.wenku8.api.Wenku8Error
import org.mewx.wenku8.global.GlobalConfig
import org.mewx.wenku8.global.api.NovelItemInfoUpdate
import org.mewx.wenku8.global.api.VolumeList
import org.mewx.wenku8.global.api.Wenku8Parser
import org.mewx.wenku8.listener.MyItemClickListener
import org.mewx.wenku8.listener.MyItemLongClickListener
import org.mewx.wenku8.listener.MyOptionClickListener
import org.mewx.wenku8.network.LightNetwork
import org.mewx.wenku8.network.LightUserSession
import org.mewx.wenku8.util.LightCache
import org.mewx.wenku8.util.LightTool
import org.mewx.wenku8.util.ProgressDialogHelper

class FavFragment : Fragment(), MyItemClickListener, MyItemLongClickListener, MyOptionClickListener {
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var recyclerView: RecyclerView? = null
    private var timecount = 0

    private val listNovelItemAid: MutableList<Int> = ArrayList()
    private val listNovelItemInfo: MutableList<NovelItemInfoUpdate> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.fragment_fav, container, false)

        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout)
        timecount = 0

        val layoutManager = LinearLayoutManager(activity).apply {
            orientation = LinearLayoutManager.VERTICAL
        }
        recyclerView = rootView.findViewById<RecyclerView>(R.id.novel_item_list).apply {
            setHasFixedSize(false)
            itemAnimator = DefaultItemAnimator()
            this.layoutManager = layoutManager
        }

        swipeRefreshLayout.setColorSchemeColors(resources.getColor(R.color.myAccentColor))
        swipeRefreshLayout.setOnRefreshListener {
            AsyncLoadAllFromCloud().execute(1)
        }

        return rootView
    }

    override fun onItemClick(view: View?, position: Int) {
        val currentActivity = activity ?: return
        val itemView = view ?: return
        val aid = listNovelItemAid[position]

        val intent = Intent(currentActivity, NovelInfoActivity::class.java).apply {
            putExtra("aid", aid)
            putExtra("from", "fav")
            putExtra("title", itemView.findViewById<TextView>(R.id.novel_title).text)
        }
        GlobalConfig.moveBookToTheTopOfBookshelf(aid)

        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            currentActivity,
            Pair.create(itemView.findViewById(R.id.novel_cover), "novel_cover"),
            Pair.create(itemView.findViewById(R.id.novel_title), "novel_title")
        )
        ActivityCompat.startActivity(currentActivity, intent, options.toBundle())
    }

    override fun onOptionButtonClick(view: View?, position: Int) {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.dialog_title_choose_delete_option)
            .setNegativeButton(R.string.dialog_negative_pass, null)
            .setItems(R.array.cleanup_option) { _, which ->
                when (which) {
                    0 -> {
                        MaterialAlertDialogBuilder(requireActivity())
                            .setMessage(R.string.dialog_sure_to_clear_cache)
                            .setPositiveButton(R.string.dialog_positive_sure) { _, _ ->
                                val aid = listNovelItemAid[position]
                                val novelFullVolume = GlobalConfig.loadFullFileFromSaveFolder(
                                    "intro",
                                    "$aid-volume.xml"
                                )
                                if (novelFullVolume.isEmpty()) {
                                    return@setPositiveButton
                                }
                                val volumeList = Wenku8Parser.getVolumeList(novelFullVolume)
                                if (volumeList.isEmpty()) {
                                    return@setPositiveButton
                                }
                                cleanVolumesCache(volumeList)
                            }
                            .setNegativeButton(R.string.dialog_negative_preferno, null)
                            .show()
                    }
                    1 -> {
                        MaterialAlertDialogBuilder(requireActivity())
                            .setMessage(R.string.dialog_content_want_to_delete)
                            .setPositiveButton(R.string.dialog_positive_sure) { _, _ ->
                                val aid = listNovelItemAid[position]
                                listNovelItemAid.removeAt(position)
                                AsyncRemoveBookFromCloud().execute(aid)
                                refreshList(timecount++)
                            }
                            .setNegativeButton(R.string.dialog_negative_preferno, null)
                            .show()
                    }
                }
            }
            .show()
    }

    override fun onItemLongClick(view: View?, position: Int) {
        // Empty, preserving existing long-click behavior.
    }

    private fun cleanVolumesCache(listVolume: List<VolumeList>) {
        listVolume.forEach { volume ->
            LightCache.cleanLocalCache(volume)
        }
    }

    private fun refreshList(time: Int) {
        if (time == 0) {
            swipeRefreshLayout.isRefreshing = true
            AsyncLoadAllFromCloud().execute()
        } else {
            loadAllLocal()
        }
    }

    private fun loadAllLocal() {
        var returnValue = 0
        var datasetChanged = false

        listNovelItemAid.clear()
        listNovelItemAid.addAll(GlobalConfig.getLocalBookshelfList())

        aids@ for (index in listNovelItemAid.indices) {
            val aid = listNovelItemAid[index]
            for (infoIndex in index until listNovelItemInfo.size) {
                val info = listNovelItemInfo[infoIndex]
                if (info.aid == aid) {
                    if (infoIndex == index) {
                        continue@aids
                    }

                    listNovelItemInfo.removeAt(infoIndex)
                    listNovelItemInfo.add(index, info)
                    datasetChanged = true
                    continue@aids
                }
            }

            val xml = GlobalConfig.loadFullFileFromSaveFolder("intro", "$aid-intro.xml")
            val info = if (xml.isEmpty()) {
                returnValue = -1
                NovelItemInfoUpdate(aid)
            } else {
                NovelItemInfoUpdate.convertFromMeta(requireNotNull(Wenku8Parser.parseNovelFullMeta(xml)))
            }
            datasetChanged = true
            listNovelItemInfo.add(index, info)
        }

        if (listNovelItemInfo.size > listNovelItemAid.size) {
            listNovelItemInfo.subList(listNovelItemAid.size, listNovelItemInfo.size).clear()
        }

        if (returnValue != 0) {
            Toast.makeText(
                activity,
                resources.getString(R.string.bookshelf_intro_load_failed),
                Toast.LENGTH_SHORT
            ).show()
        }

        val currentRecyclerView = recyclerView ?: return
        if (currentRecyclerView.adapter == null) {
            val adapter = NovelItemAdapterUpdate().apply {
                refreshDataset(listNovelItemInfo)
                setOnItemClickListener(this@FavFragment)
                setOnDeleteClickListener(this@FavFragment)
                setOnItemLongClickListener(this@FavFragment)
            }
            currentRecyclerView.adapter = adapter
        }
        if (datasetChanged) {
            currentRecyclerView.adapter?.notifyDataSetChanged()
        }
        swipeRefreshLayout.isRefreshing = false
    }

    private inner class AsyncLoadAllFromCloud :
        AsyncTask<Int, Int, Wenku8Error.ErrorCode>() {
        private var progressDialog: ProgressDialogHelper? = null
        private var isLoading = false
        private var forceLoad = false

        override fun onPreExecute() {
            super.onPreExecute()

            loadAllLocal()
            isLoading = true
            progressDialog = ProgressDialogHelper.show(
                requireActivity(),
                getString(R.string.dialog_content_sync),
                false,
                true
            ) {
                isLoading = false
                progressDialog?.dismiss()
            }
        }

        override fun doInBackground(vararg params: Int?): Wenku8Error.ErrorCode {
            if (params.isNotEmpty()) {
                forceLoad = true
            }

            var bytes = LightNetwork.LightHttpPostConnection(
                Wenku8API.BASE_URL,
                Wenku8API.getBookshelfListAid(GlobalConfig.getCurrentLang())
            ) ?: return Wenku8Error.ErrorCode.NETWORK_ERROR

            if (LightTool.isInteger(String(bytes))) {
                if (
                    Wenku8Error.getSystemDefinedErrorCode(String(bytes).toInt()) ==
                    Wenku8Error.ErrorCode.SYSTEM_4_NOT_LOGGED_IN
                ) {
                    val loginResult = LightUserSession.doLoginFromFile(
                        Runnable { GlobalConfig.loadUserInfoSet() }
                    )
                    if (loginResult != Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED) {
                        return loginResult
                    }

                    bytes = LightNetwork.LightHttpPostConnection(
                        Wenku8API.BASE_URL,
                        Wenku8API.getBookshelfListAid(GlobalConfig.getCurrentLang())
                    ) ?: return Wenku8Error.ErrorCode.NETWORK_ERROR
                }
            }

            val cloudAidList = ArrayList<Int>()
            val xml = String(bytes, Charsets.UTF_8)
            Log.d("MewX", xml)
            val matcher = CLOUD_AID_PATTERN.matcher(xml)
            while (matcher.find()) {
                try {
                    cloudAidList.add(matcher.group(1)!!.toInt())
                } catch (e: NumberFormatException) {
                    Log.e(FavFragment::class.java.simpleName, "Found and skipped broken aid.")
                }
            }

            val allAids = ArrayList<Int>().apply {
                addAll(GlobalConfig.getLocalBookshelfList())
                addAll(cloudAidList)
            }

            val localOnly = ArrayList(allAids).apply {
                removeAll(cloudAidList.toSet())
            }

            val aidDiff = ArrayList(allAids)
            if (!forceLoad) {
                aidDiff.removeAll(GlobalConfig.getLocalBookshelfList().toSet())
            } else {
                val unique = HashSet(aidDiff)
                aidDiff.clear()
                aidDiff.addAll(unique)
            }
            if (aidDiff.isEmpty() && localOnly.isEmpty()) {
                return Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED
            }

            var count = 0
            progressDialog?.setMaxProgress(aidDiff.size)
            for (aid in aidDiff) {
                if (!isLoading) {
                    return Wenku8Error.ErrorCode.USER_CANCELLED_TASK
                }

                try {
                    val executor = Executors.newFixedThreadPool(3)

                    val volumeTask = Callable {
                        if (!isLoading) {
                            null
                        } else {
                            val contentValues: ContentValues = Wenku8API.getNovelIndex(
                                aid,
                                GlobalConfig.getCurrentLang()
                            )
                            LightNetwork.LightHttpPostConnection(Wenku8API.BASE_URL, contentValues)
                        }
                    }
                    val introTask = Callable {
                        if (!isLoading) {
                            null
                        } else {
                            LightNetwork.LightHttpPostConnection(
                                Wenku8API.BASE_URL,
                                Wenku8API.getNovelFullMeta(aid, GlobalConfig.getCurrentLang())
                            )
                        }
                    }

                    val volumeFuture = executor.submit(volumeTask)
                    val introFuture = executor.submit(introTask)
                    val volumeBytes = volumeFuture.get()
                    val introBytes = introFuture.get()
                    if (!isLoading) {
                        executor.shutdown()
                        return Wenku8Error.ErrorCode.USER_CANCELLED_TASK
                    }
                    if (volumeBytes == null || introBytes == null) {
                        executor.shutdown()
                        return Wenku8Error.ErrorCode.NETWORK_ERROR
                    }

                    val volumeXml = String(volumeBytes, Charsets.UTF_8)
                    val introXml = String(introBytes, Charsets.UTF_8)
                    val volumeList = Wenku8Parser.getVolumeList(volumeXml)
                    val novelMeta = Wenku8Parser.parseNovelFullMeta(introXml)
                    if (volumeList.isEmpty() || novelMeta == null) {
                        executor.shutdown()
                        return Wenku8Error.ErrorCode.XML_PARSE_FAILED
                    }

                    if (!isLoading) {
                        executor.shutdown()
                        return Wenku8Error.ErrorCode.USER_CANCELLED_TASK
                    }

                    val finalAid = novelMeta.aid
                    val fullIntroTask = Callable {
                        if (!isLoading) {
                            null
                        } else {
                            val contentValues: ContentValues = Wenku8API.getNovelFullIntro(
                                finalAid,
                                GlobalConfig.getCurrentLang()
                            )
                            LightNetwork.LightHttpPostConnection(Wenku8API.BASE_URL, contentValues)
                        }
                    }
                    val fullIntroBytes = executor.submit(fullIntroTask).get()
                    executor.shutdown()

                    if (fullIntroBytes == null) {
                        return Wenku8Error.ErrorCode.NETWORK_ERROR
                    }
                    novelMeta.fullIntro = String(fullIntroBytes, Charsets.UTF_8)

                    GlobalConfig.writeFullFileIntoSaveFolder("intro", "$aid-volume.xml", volumeXml)
                    GlobalConfig.writeFullFileIntoSaveFolder("intro", "$aid-introfull.xml", novelMeta.fullIntro)
                    GlobalConfig.writeFullFileIntoSaveFolder("intro", "$aid-intro.xml", introXml)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                GlobalConfig.addToLocalBookshelf(aid)
                publishProgress(++count)
            }

            val localOnlyCopy = ArrayList(localOnly)
            for (aid in localOnlyCopy) {
                bytes = LightNetwork.LightHttpPostConnection(
                    Wenku8API.BASE_URL,
                    Wenku8API.getAddToBookshelfParams(aid)
                ) ?: return Wenku8Error.ErrorCode.NETWORK_ERROR

                val resultText = String(bytes, Charsets.UTF_8)
                if (LightTool.isInteger(resultText)) {
                    val result = Wenku8Error.getSystemDefinedErrorCode(resultText.toInt())
                    if (result == Wenku8Error.ErrorCode.SYSTEM_6_BOOKSHELF_FULL) {
                        return result
                    } else if (
                        result == Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED ||
                        result == Wenku8Error.ErrorCode.SYSTEM_5_ALREADY_IN_BOOKSHELF
                    ) {
                        localOnly.remove(aid)
                    }
                }
            }

            return Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            progressDialog?.setProgress(values[0] ?: 0)
        }

        override fun onPostExecute(errorCode: Wenku8Error.ErrorCode) {
            super.onPostExecute(errorCode)

            isLoading = false
            try {
                progressDialog?.dismiss()
            } catch (_: Exception) {
                // Ignore detached or already dismissed dialogs.
            }

            if (errorCode != Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED) {
                Toast.makeText(MyApp.getContext(), errorCode.toString(), Toast.LENGTH_SHORT).show()
                refreshList(timecount++)
            } else {
                loadAllLocal()
            }
        }
    }

    inner class AsyncRemoveBookFromCloud :
        AsyncTask<Int, Int, Wenku8Error.ErrorCode>() {
        private var progressDialog: ProgressDialogHelper? = null
        private var aid = 0

        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog = ProgressDialogHelper.show(
                requireActivity(),
                getString(R.string.dialog_content_novel_remove_from_cloud),
                true,
                false,
                null
            )
        }

        override fun doInBackground(vararg params: Int?): Wenku8Error.ErrorCode {
            aid = params[0] ?: return Wenku8Error.ErrorCode.PARAM_COUNT_NOT_MATCHED
            val bytes = LightNetwork.LightHttpPostConnection(
                Wenku8API.BASE_URL,
                Wenku8API.getDelFromBookshelfParams(aid)
            ) ?: return Wenku8Error.ErrorCode.NETWORK_ERROR

            val result = String(bytes, Charsets.UTF_8)
            Log.d("MewX", result)
            if (!LightTool.isInteger(result)) {
                return Wenku8Error.ErrorCode.RETURNED_VALUE_EXCEPTION
            }

            val errorCode = Wenku8Error.getSystemDefinedErrorCode(result.toInt())
            if (
                errorCode != Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED &&
                errorCode != Wenku8Error.ErrorCode.SYSTEM_4_NOT_LOGGED_IN &&
                errorCode != Wenku8Error.ErrorCode.SYSTEM_7_NOVEL_NOT_IN_BOOKSHELF
            ) {
                return errorCode
            }

            val novelFullVolume = GlobalConfig.loadFullFileFromSaveFolder("intro", "$aid-volume.xml")
            if (novelFullVolume.isEmpty()) {
                return Wenku8Error.ErrorCode.ERROR_DEFAULT
            }
            val volumeList = Wenku8Parser.getVolumeList(novelFullVolume)
            if (volumeList.isEmpty()) {
                return Wenku8Error.ErrorCode.XML_PARSE_FAILED
            }

            cleanVolumesCache(volumeList)
            LightCache.deleteFile(GlobalConfig.getFirstFullSaveFilePath(), "intro" + File.separator + "$aid-intro.xml")
            LightCache.deleteFile(GlobalConfig.getFirstFullSaveFilePath(), "intro" + File.separator + "$aid-introfull.xml")
            LightCache.deleteFile(GlobalConfig.getFirstFullSaveFilePath(), "intro" + File.separator + "$aid-volume.xml")
            LightCache.deleteFile(GlobalConfig.getSecondFullSaveFilePath(), "intro" + File.separator + "$aid-intro.xml")
            LightCache.deleteFile(GlobalConfig.getSecondFullSaveFilePath(), "intro" + File.separator + "$aid-introfull.xml")
            LightCache.deleteFile(GlobalConfig.getSecondFullSaveFilePath(), "intro" + File.separator + "$aid-volume.xml")

            GlobalConfig.removeFromLocalBookshelf(aid)
            return if (!GlobalConfig.testInLocalBookshelf(aid)) {
                Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED
            } else {
                Wenku8Error.ErrorCode.LOCAL_BOOK_REMOVE_FAILED
            }
        }

        override fun onPostExecute(errorCode: Wenku8Error.ErrorCode) {
            super.onPostExecute(errorCode)

            try {
                progressDialog?.dismiss()
            } catch (_: Exception) {
                // Ignore detached or already dismissed dialogs.
            }

            if (errorCode == Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED) {
                Toast.makeText(activity, resources.getString(R.string.bookshelf_removed), Toast.LENGTH_SHORT).show()
                loadAllLocal()
            } else {
                Toast.makeText(activity, errorCode.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        GlobalConfig.LeaveBookshelf()
    }

    override fun onResume() {
        super.onResume()
        GlobalConfig.EnterBookshelf()
        refreshList(timecount++)
    }

    companion object {
        private val CLOUD_AID_PATTERN = Pattern.compile("aid=\"(.*)\"")

        @JvmStatic
        fun newInstance(): FavFragment = FavFragment()
    }
}
