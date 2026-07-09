@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package org.mewx.wenku8.activity

import android.content.ContentValues
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.analytics.FirebaseAnalytics
import com.nostra13.universalimageloader.core.ImageLoader
import java.io.File
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import org.mewx.wenku8.R
import org.mewx.wenku8.api.Wenku8API
import org.mewx.wenku8.api.Wenku8Error
import org.mewx.wenku8.global.GlobalConfig
import org.mewx.wenku8.global.api.ChapterInfo
import org.mewx.wenku8.global.api.NovelItemMeta
import org.mewx.wenku8.global.api.OldNovelContentParser
import org.mewx.wenku8.global.api.OldNovelContentParser.NovelContentType
import org.mewx.wenku8.global.api.VolumeList
import org.mewx.wenku8.global.api.Wenku8Parser
import org.mewx.wenku8.network.LightNetwork
import org.mewx.wenku8.reader.modern.launch.ReaderLaunchPlan
import org.mewx.wenku8.reader.modern.launch.ReaderLaunchPlanner
import org.mewx.wenku8.util.GoogleServicesHelper
import org.mewx.wenku8.util.LightCache
import org.mewx.wenku8.util.LightTool
import org.mewx.wenku8.util.ProgressDialogHelper

/**
 * Created by MewX on 2015/5/13.
 * Novel Info Activity.
 */
class NovelInfoActivity : BaseMaterialActivity() {
    private var firebaseAnalytics: FirebaseAnalytics? = null
    private var aid = 1
    private var from: String? = ""
    private var title: String? = ""
    private var isLoading = true

    private lateinit var maskLayout: RelativeLayout
    private lateinit var infoLinearLayout: LinearLayout
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var chapterListLayout: LinearLayout
    private lateinit var sideSheetHeader: TextView
    private var currentSelectedVolume: VolumeList? = null

    private lateinit var tvNovelTitle: TextView
    private lateinit var tvNovelAuthor: TextView
    private lateinit var tvNovelStatus: TextView
    private lateinit var tvNovelUpdate: TextView
    private lateinit var tvLatestChapter: TextView
    private lateinit var tvNovelFullIntro: TextView
    private var progressDialog: ProgressDialogHelper? = null

    private lateinit var fabFavorite: ExtendedFloatingActionButton
    private lateinit var fabDownload: ExtendedFloatingActionButton
    private lateinit var fabMenu: FloatingActionButton
    private val isMenuExpanded = AtomicBoolean(false)

    private lateinit var progressBar: LinearProgressIndicator
    private var novelItemMeta: NovelItemMeta? = null
    private var listVolume: ArrayList<VolumeList> = ArrayList()
    private var novelFullMeta: String? = null
    private var novelFullIntro: String? = null
    private var novelFullVolume: String? = null

    private lateinit var errorLayout: LinearLayout
    private lateinit var errorMessageView: TextView
    private lateinit var retryButton: MaterialButton
    private lateinit var novelInfoScrollView: ScrollView
    private lateinit var fabContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initMaterialStyle(R.layout.layout_novel_info)

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.navigationBarColor = resources.getColor(R.color.myNavigationColor)

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    when {
                        drawerLayout.isDrawerOpen(GravityCompat.END) -> drawerLayout.closeDrawer(GravityCompat.END)
                        isMenuExpanded.get() -> collapseMenu()
                        else -> finishAfterTransition()
                    }
                }
            },
        )

        firebaseAnalytics = GoogleServicesHelper.initFirebase(this)

        aid = intent.getIntExtra("aid", 1)
        from = intent.getStringExtra("from")
        title = intent.getStringExtra("title")

        val viewItemParams = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, aid.toString())
            putString(FirebaseAnalytics.Param.ITEM_NAME, title)
            putString("from", from)
        }
        GoogleServicesHelper.logEvent(firebaseAnalytics, FirebaseAnalytics.Event.VIEW_ITEM, viewItemParams)

        val imageLoader = ImageLoader.getInstance()
        if (imageLoader == null || !imageLoader.isInited) {
            GlobalConfig.initImageLoader(this)
        }

        bindViews()
        bindInitialUi()
        bindClickListeners()

        findViewById<AdView>(R.id.ad_view).loadAd(AdRequest.Builder().build())

        supportActionBar?.setTitle(R.string.action_novel_info)
        progressBar.visibility = View.INVISIBLE
        Handler().postDelayed(
            {
                isLoading = false
                refreshInfo()
            },
            500,
        )
    }

    private fun bindViews() {
        drawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        chapterListLayout = findViewById(R.id.novel_chapter_scroll)
        sideSheetHeader = findViewById(R.id.side_sheet_header)
        maskLayout = findViewById(R.id.white_mask)
        infoLinearLayout = findViewById(R.id.novel_info_scroll)

        tvNovelTitle = findViewById(R.id.novel_title)
        tvNovelAuthor = findViewById(R.id.novel_author)
        tvNovelStatus = findViewById(R.id.novel_status)
        tvNovelUpdate = findViewById(R.id.novel_update)
        tvLatestChapter = findViewById(R.id.novel_intro)
        tvNovelFullIntro = findViewById(R.id.novel_intro_full)

        fabFavorite = findViewById(R.id.fab_favorate)
        fabDownload = findViewById(R.id.fab_download)
        fabMenu = findViewById(R.id.multiple_actions)
        progressBar = findViewById(R.id.spb)
        errorLayout = findViewById(R.id.ll_error)
        errorMessageView = findViewById(R.id.tv_error_msg)
        retryButton = findViewById(R.id.btn_retry)
        novelInfoScrollView = findViewById(R.id.novel_info_scroll_view)
        fabContainer = findViewById(R.id.fab_container)
    }

    private fun bindInitialUi() {
        val cardLayout = findViewById<LinearLayout>(R.id.item_card)
        val novelCover = findViewById<ImageView>(R.id.novel_cover)
        val latestChapterNameText = findViewById<TextView>(R.id.novel_item_text_shortinfo)
        val novelOption = findViewById<ImageButton>(R.id.novel_option)

        tvNovelTitle.text = title
        loadCoverImage(novelCover)
        latestChapterNameText.text = resources.getText(R.string.novel_item_latest_chapter)
        novelOption.visibility = ImageButton.INVISIBLE
        cardLayout.setBackgroundResource(R.color.menu_transparent)
        if (GlobalConfig.testInLocalBookshelf(aid)) {
            fabFavorite.setIcon(resources.getDrawable(R.drawable.ic_favorate_pressed))
            fabFavorite.iconTint = null
        }

        tvNovelTitle.background = resources.getDrawable(R.drawable.btn_menu_item)
        tvNovelAuthor.background = resources.getDrawable(R.drawable.btn_menu_item)
        tvLatestChapter.background = resources.getDrawable(R.drawable.btn_menu_item)
    }

    private fun loadCoverImage(novelCover: ImageView) {
        val defaultCoverPath = GlobalConfig.getDefaultStoragePath() + "imgs" + File.separator + "$aid.jpg"
        val backupCoverPath = GlobalConfig.getBackupStoragePath() + "imgs" + File.separator + "$aid.jpg"
        when {
            LightCache.testFileExist(defaultCoverPath) ->
                ImageLoader.getInstance().displayImage("file://$defaultCoverPath", novelCover)
            LightCache.testFileExist(backupCoverPath) ->
                ImageLoader.getInstance().displayImage("file://$backupCoverPath", novelCover)
            else -> ImageLoader.getInstance().displayImage(Wenku8API.getCoverURL(aid), novelCover)
        }
    }

    private fun bindClickListeners() {
        retryButton.setOnClickListener { refreshInfo() }
        fabMenu.setOnClickListener { toggleMenu() }
        maskLayout.setOnClickListener {
            if (isMenuExpanded.get()) {
                collapseMenu()
            }
        }

        tvNovelTitle.setOnClickListener {
            if (runLoadingChecker()) return@setOnClickListener
            val meta = novelItemMeta ?: return@setOnClickListener
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_content_novel_title)
                .setMessage("$aid: ${meta.title}")
                .setPositiveButton(R.string.dialog_positive_known, null)
                .show()
        }

        tvNovelAuthor.setOnClickListener {
            if (runLoadingChecker()) return@setOnClickListener
            val meta = novelItemMeta ?: return@setOnClickListener
            MaterialAlertDialogBuilder(this)
                .setMessage(R.string.dialog_content_search_author)
                .setPositiveButton(R.string.dialog_positive_ok) { _, _ ->
                    val intent = Intent(this, SearchResultActivity::class.java)
                    intent.putExtra("key", meta.author)
                    startActivity(intent)
                    overridePendingTransition(R.anim.fade_in, R.anim.hold)
                }
                .setNegativeButton(R.string.dialog_negative_biao, null)
                .show()
        }

        fabFavorite.setOnClickListener {
            if (runLoadingChecker()) return@setOnClickListener
            toggleFavorite()
        }

        fabDownload.setOnClickListener {
            if (runLoadingChecker()) return@setOnClickListener
            if (!GlobalConfig.testInLocalBookshelf(aid)) {
                Toast.makeText(this, resources.getString(R.string.system_fav_it_first), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_title_choose_download_option)
                .setNegativeButton(R.string.dialog_negative_pass, null)
                .setItems(R.array.download_option) { _, which ->
                    when (which) {
                        0 -> optionCheckUpdates()
                        1 -> optionDownloadUpdates()
                        2 -> optionDownloadOverride()
                        3 -> optionDownloadSelected()
                    }
                }
                .show()
        }

        tvLatestChapter.setOnClickListener {
            if (runLoadingChecker()) return@setOnClickListener
            val meta = novelItemMeta
            if (meta != null && meta.latestSectionCid != 0) {
                showDirectJumpToReaderDialog(meta.latestSectionCid)
            } else {
                Toast.makeText(
                    this,
                    resources.getText(R.string.reader_msg_please_refresh_and_retry),
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }

        findViewById<ImageView>(R.id.novel_cover).setOnClickListener {
            if (runLoadingChecker()) return@setOnClickListener
            fetchAndShowNovelCover()
        }

        sideSheetHeader.setOnClickListener {
            val volume = currentSelectedVolume ?: return@setOnClickListener
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_content_volume_title)
                .setMessage(volume.volumeName)
                .setPositiveButton(R.string.dialog_positive_known, null)
                .show()
        }
    }

    private fun toggleFavorite() {
        if (GlobalConfig.testInLocalBookshelf(aid)) {
            MaterialAlertDialogBuilder(this)
                .setMessage(R.string.dialog_content_sure_to_unfav)
                .setPositiveButton(R.string.dialog_positive_yes) { _, _ ->
                    AsyncRemoveBookFromCloud().execute(aid)
                }
                .setNegativeButton(R.string.dialog_negative_preferno, null)
                .show()
            return
        }

        val meta = novelFullMeta
        val intro = novelFullIntro
        val volume = novelFullVolume
        if (meta == null || intro == null || volume == null) {
            val missingParts = ArrayList<String>()
            if (meta == null) missingParts.add("meta")
            if (intro == null) missingParts.add("intro")
            if (volume == null) missingParts.add("volume")
            val event = Bundle().apply {
                putStringArrayList("novel_info_save_null", missingParts)
            }
            GoogleServicesHelper.logEvent(firebaseAnalytics, FirebaseAnalytics.Event.VIEW_ITEM, event)
            Toast.makeText(this, resources.getString(R.string.system_loading_please_wait), Toast.LENGTH_SHORT).show()
            return
        }

        GlobalConfig.writeFullFileIntoSaveFolder("intro", "$aid-intro.xml", meta)
        GlobalConfig.writeFullFileIntoSaveFolder("intro", "$aid-introfull.xml", intro)
        GlobalConfig.writeFullFileIntoSaveFolder("intro", "$aid-volume.xml", volume)
        GlobalConfig.addToLocalBookshelf(aid)
        if (GlobalConfig.testInLocalBookshelf(aid)) {
            Toast.makeText(this, resources.getString(R.string.bookshelf_added), Toast.LENGTH_SHORT).show()
            fabFavorite.setIcon(resources.getDrawable(R.drawable.ic_favorate_pressed))
            fabFavorite.iconTint = null
        } else {
            Toast.makeText(this, resources.getString(R.string.bookshelf_error), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * @return true if loading; otherwise false
     */
    private fun runLoadingChecker(): Boolean {
        if (isLoading) {
            Toast.makeText(this, resources.getString(R.string.system_loading_please_wait), Toast.LENGTH_SHORT).show()
        }
        return isLoading
    }

    private fun optionCheckUpdates() {
        isLoading = true
        val task = AsyncUpdateCacheTask()
        task.execute(aid, 0)
        showCancellableProgress(task)
    }

    private fun optionDownloadUpdates() {
        isLoading = true
        val task = AsyncUpdateCacheTask()
        task.execute(aid, 1)
        showCancellableProgress(task)
    }

    private fun optionDownloadOverride() {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.dialog_content_verify_force_update)
            .setPositiveButton(R.string.dialog_positive_likethis) { _, _ ->
                isLoading = true
                val task = AsyncUpdateCacheTask()
                task.execute(aid, 2)
                showCancellableProgress(task)
            }
            .setNegativeButton(R.string.dialog_negative_preferno, null)
            .show()
    }

    private fun optionDownloadSelected() {
        val volumes = listVolume.map { it.volumeName.orEmpty() }.toTypedArray()
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_option_select_and_update)
            .setMultiChoiceItems(volumes, null) { _, _, _ -> }
            .setPositiveButton(R.string.dialog_positive_ok) { dialog, _ ->
                val alertDialog = dialog as AlertDialog
                val checkedItemPositions = alertDialog.listView.checkedItemPositions
                val selectedIndices = ArrayList<Int>()
                for (i in 0 until checkedItemPositions.size()) {
                    val index = checkedItemPositions.keyAt(i)
                    if (checkedItemPositions.valueAt(i)) {
                        selectedIndices.add(index)
                    }
                }
                if (selectedIndices.isEmpty()) return@setPositiveButton
                AsyncDownloadVolumes().execute(selectedIndices.toTypedArray())
            }
            .show()
    }

    private fun showCancellableProgress(task: AsyncTask<*, *, *>) {
        progressDialog = ProgressDialogHelper.show(
            this,
            getString(R.string.dialog_content_downloading),
            false,
            true,
        ) {
            isLoading = false
            task.cancel(true)
            progressDialog?.dismiss()
            progressDialog = null
        }
        progressDialog?.setProgress(0)
        progressDialog?.setMaxProgress(1)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        supportActionBar?.title = resources.getString(R.string.action_novel_info)
        menuInflater.inflate(R.menu.menu_novel_info, menu)
        for (i in 0 until menu.size()) {
            val drawable: Drawable? = menu.getItem(i).icon
            drawable?.mutate()
            drawable?.setColorFilter(resources.getColor(R.color.default_white), PorterDuff.Mode.SRC_ATOP)
        }
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            android.R.id.home -> finishAfterTransition()
            R.id.action_continue_read_progress -> {
                if (runLoadingChecker()) return true
                val readSaves = GlobalConfig.getReadSavesRecordV1(aid)
                if (readSaves != null) {
                    showDirectJumpToReaderDialog(readSaves.cid)
                    return true
                }
                Toast.makeText(
                    this,
                    resources.getText(R.string.reader_msg_no_saved_reading_progress),
                    Toast.LENGTH_SHORT,
                ).show()
            }
            R.id.action_go_to_forum -> {
                val intent = Intent(this, NovelReviewListActivity::class.java)
                intent.putExtra("aid", aid)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(menuItem)
    }

    private fun showDirectJumpToReaderDialog(cid: Int) {
        var savedVolumeList: VolumeList? = null
        var chapterInfo: ChapterInfo? = null
        for (volume in listVolume) {
            for (chapter in volume.chapterList.orEmpty()) {
                if (chapter.cid == cid) {
                    chapterInfo = chapter
                    savedVolumeList = volume
                    break
                }
            }
        }

        val volume = savedVolumeList
        val chapter = chapterInfo
        if (volume == null || chapter == null) {
            Toast.makeText(this, resources.getText(R.string.reader_msg_no_available_chapter), Toast.LENGTH_SHORT).show()
            return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.action_modern_reader)
            .setMessage(
                resources.getString(R.string.reader_jump_last) +
                    "\n" + title +
                    "\n" + volume.volumeName +
                    "\n" + chapter.chapterName,
            )
            .setPositiveButton(R.string.dialog_positive_sure) { _, _ ->
                val intent = createReaderIntent(
                    ReaderLaunchPlanner.defaultPlan(from, isChapterCached(cid), true),
                    volume,
                    cid,
                )
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.hold)
            }
            .setNegativeButton(R.string.dialog_negative_biao, null)
            .show()
    }

    private fun createReaderIntent(
        launchPlan: ReaderLaunchPlan,
        volumeList: VolumeList,
        cid: Int,
    ): Intent {
        return Intent(this, launchPlan.targetActivityClass).apply {
            putExtra("aid", aid)
            putExtra("volume", volumeList)
            putExtra("volumes", ArrayList(this@NovelInfoActivity.listVolume))
            putExtra("cid", cid)
            putExtra("from", launchPlan.resolvedSource)
            if (launchPlan.forceJump) {
                putExtra("forcejump", "yes")
            }
        }
    }

    private fun isChapterCached(cid: Int): Boolean =
        NovelChapterCacheAvailability.isCached(
            defaultStoragePath = GlobalConfig.getDefaultStoragePath(),
            backupStoragePath = GlobalConfig.getBackupStoragePath(),
            saveFolderName = GlobalConfig.saveFolderName,
            cid = cid,
            fileExists = LightCache::testFileExist,
        )

    private inner class FetchInfoAsyncTask : AsyncTask<Int, Int, Int>() {
        private var fromLocal = false

        override fun doInBackground(vararg params: Int?): Int {
            if (params.size == 1 && params[0] == 1) {
                fromLocal = true
            }

            val executor = Executors.newFixedThreadPool(3)
            val metaTask = Callable {
                if (fromLocal) {
                    val meta = GlobalConfig.loadFullFileFromSaveFolder("intro", "$aid-intro.xml")
                    if (meta.isEmpty()) throw Exception("Empty meta from local")
                    meta
                } else {
                    val contentValues = Wenku8API.getNovelFullMeta(aid, GlobalConfig.getCurrentLang())
                    val bytes = LightNetwork.LightHttpPostConnection(Wenku8API.BASE_URL, contentValues)
                        ?: throw Exception("Network error for meta")
                    String(bytes, Charsets.UTF_8)
                }
            }
            val introTask = Callable {
                if (fromLocal) {
                    val intro = GlobalConfig.loadFullFileFromSaveFolder("intro", "$aid-introfull.xml")
                    if (intro.isEmpty()) throw Exception("Empty intro from local")
                    intro
                } else {
                    val contentValues = Wenku8API.getNovelFullIntro(aid, GlobalConfig.getCurrentLang())
                    val bytes = LightNetwork.LightHttpPostConnection(Wenku8API.BASE_URL, contentValues)
                        ?: throw Exception("Network error for intro")
                    String(bytes, Charsets.UTF_8)
                }
            }
            val volumeTask = Callable {
                if (fromLocal) {
                    val volume = GlobalConfig.loadFullFileFromSaveFolder("intro", "$aid-volume.xml")
                    if (volume.isEmpty()) throw Exception("Empty volume from local")
                    volume
                } else {
                    val contentValues = Wenku8API.getNovelIndex(aid, GlobalConfig.getCurrentLang())
                    val bytes = LightNetwork.LightHttpPostConnection(Wenku8API.BASE_URL, contentValues)
                        ?: throw Exception("Network error for volume")
                    String(bytes, Charsets.UTF_8)
                }
            }

            try {
                novelFullMeta = executor.submit(metaTask).get()
                novelFullIntro = executor.submit(introTask).get()
                novelFullVolume = executor.submit(volumeTask).get()
            } catch (exception: InterruptedException) {
                exception.printStackTrace()
                return -1
            } catch (exception: ExecutionException) {
                exception.printStackTrace()
                val message = exception.message
                if (message != null && message.contains("local")) return -9
                return -1
            } finally {
                executor.shutdown()
            }

            val parsedMeta = Wenku8Parser.parseNovelFullMeta(novelFullMeta.orEmpty()) ?: return -1
            novelItemMeta = parsedMeta
            parsedMeta.fullIntro = novelFullIntro.orEmpty()
            if (parsedMeta.fullIntro.isEmpty()) return -1

            listVolume = Wenku8Parser.getVolumeList(novelFullVolume.orEmpty())
            if (listVolume.isEmpty()) return -1

            NovelVolumeCacheMarker.markInLocalVolumes(listVolume) { chapterCid ->
                isChapterCached(chapterCid)
            }

            return 0
        }

        override fun onPostExecute(result: Int) {
            isLoading = false
            progressBar.visibility = View.INVISIBLE
            super.onPostExecute(result)

            when {
                result == -1 -> {
                    showError(R.string.system_network_error)
                    return
                }
                result == -9 -> {
                    showError(R.string.bookshelf_intro_load_failed)
                    return
                }
                result < 0 -> {
                    showError("Unknown error occurred")
                    return
                }
            }

            errorLayout.visibility = View.GONE
            novelInfoScrollView.visibility = View.VISIBLE
            fabContainer.visibility = View.VISIBLE

            tvNovelAuthor.paintFlags = tvNovelAuthor.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            tvLatestChapter.paintFlags = tvLatestChapter.paintFlags or Paint.UNDERLINE_TEXT_FLAG

            val meta = novelItemMeta ?: return
            tvNovelTitle.text = meta.title
            tvNovelAuthor.text = meta.author
            tvNovelStatus.text = meta.bookStatus
            tvNovelUpdate.text = meta.lastUpdate
            tvLatestChapter.text = meta.latestSectionName
            supportActionBar?.title = meta.title
            tvNovelFullIntro.text = meta.fullIntro

            buildVolumeList()
        }
    }

    private fun showError(messageResId: Int) {
        errorLayout.visibility = View.VISIBLE
        novelInfoScrollView.visibility = View.GONE
        fabContainer.visibility = View.GONE
        errorMessageView.setText(messageResId)
    }

    private fun showError(message: CharSequence) {
        errorLayout.visibility = View.VISIBLE
        novelInfoScrollView.visibility = View.GONE
        fabContainer.visibility = View.GONE
        errorMessageView.text = message
    }

    private fun toggleMenu() {
        if (isMenuExpanded.get()) {
            collapseMenu()
        } else {
            expandMenu()
        }
    }

    private fun expandMenu() {
        isMenuExpanded.set(true)
        fabMenu.animate().rotation(135f).setDuration(300).setInterpolator(OvershootInterpolator()).start()

        fabFavorite.visibility = View.VISIBLE
        fabFavorite.alpha = 0f
        fabFavorite.scaleX = 0f
        fabFavorite.scaleY = 0f
        fabFavorite.translationY = 100f
        fabFavorite.animate().alpha(1f).scaleX(1f).scaleY(1f).translationY(0f)
            .setDuration(300).setInterpolator(OvershootInterpolator()).start()

        fabDownload.visibility = View.VISIBLE
        fabDownload.alpha = 0f
        fabDownload.scaleX = 0f
        fabDownload.scaleY = 0f
        fabDownload.translationY = 50f
        fabDownload.animate().alpha(1f).scaleX(1f).scaleY(1f).translationY(0f)
            .setDuration(300).setInterpolator(OvershootInterpolator()).start()

        maskLayout.visibility = View.VISIBLE
    }

    private fun collapseMenu() {
        isMenuExpanded.set(false)
        fabMenu.animate().rotation(0f).setDuration(300).setInterpolator(OvershootInterpolator()).start()
        fabFavorite.animate().alpha(0f).scaleX(0f).scaleY(0f).translationY(100f)
            .setDuration(300).withEndAction { fabFavorite.visibility = View.GONE }.start()
        fabDownload.animate().alpha(0f).scaleX(0f).scaleY(0f).translationY(50f)
            .setDuration(300).withEndAction { fabDownload.visibility = View.GONE }.start()
        maskLayout.visibility = View.INVISIBLE
    }

    private fun buildVolumeList() {
        if (infoLinearLayout.childCount >= 3) {
            infoLinearLayout.removeViews(2, infoLinearLayout.childCount - 2)
        }

        val readSaves = GlobalConfig.getReadSavesRecordV1(aid)
        for (volume in listVolume) {
            val item = LayoutInflater.from(this)
                .inflate(R.layout.view_novel_chapter_item, null) as RelativeLayout
            val titleView = item.findViewById<TextView>(R.id.chapter_title)
            titleView.text = volume.volumeName
            if (volume.inLocal) {
                item.findViewById<TextView>(R.id.chapter_status).text =
                    resources.getString(R.string.bookshelf_inlocal)
            }

            val button = item.findViewById<RelativeLayout>(R.id.chapter_btn)
            if (readSaves != null && readSaves.vid == volume.vid) {
                button.setBackgroundColor(Color.LTGRAY)
            }
            button.setOnLongClickListener {
                MaterialAlertDialogBuilder(this)
                    .setMessage(R.string.dialog_sure_to_clear_cache)
                    .setPositiveButton(R.string.dialog_positive_want) { _, _ ->
                        LightCache.cleanLocalCache(volume)
                        item.findViewById<TextView>(R.id.chapter_status).text = ""
                    }
                    .setNegativeButton(R.string.dialog_negative_biao, null)
                    .show()
                true
            }
            button.setOnClickListener {
                buildChapterList(volume)
                drawerLayout.openDrawer(GravityCompat.END)
            }

            infoLinearLayout.addView(item)
        }
    }

    private fun buildChapterList(volumeList: VolumeList) {
        currentSelectedVolume = volumeList
        sideSheetHeader.text = volumeList.volumeName
        chapterListLayout.removeAllViews()

        val readSaves = GlobalConfig.getReadSavesRecordV1(aid)
        for (chapter in volumeList.chapterList.orEmpty()) {
            val item = LayoutInflater.from(this)
                .inflate(R.layout.view_novel_chapter_item, null) as RelativeLayout
            item.findViewById<TextView>(R.id.chapter_title).text = chapter.chapterName

            val button = item.findViewById<RelativeLayout>(R.id.chapter_btn)
            if (readSaves != null && readSaves.cid == chapter.cid) {
                button.setBackgroundColor(Color.LTGRAY)
            }
            button.setOnClickListener {
                val intent = createReaderIntent(
                    ReaderLaunchPlanner.defaultPlan(from, isChapterCached(chapter.cid), false),
                    volumeList,
                    chapter.cid,
                )
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.hold)
            }

            val optionButton = button.findViewById<View>(R.id.novel_option)
            optionButton.visibility = View.VISIBLE
            optionButton.setOnClickListener {
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.system_choose_reader_engine)
                    .setItems(R.array.reader_engine_option) { _, which ->
                        val intent = createReaderIntent(
                            ReaderLaunchPlanner.dialogPlan(which, from, isChapterCached(chapter.cid), false),
                            volumeList,
                            chapter.cid,
                        )
                        startActivity(intent)
                        overridePendingTransition(R.anim.fade_in, R.anim.hold)
                    }
                    .show()
            }

            chapterListLayout.addView(item)
        }
    }

    inner class AsyncUpdateCacheTask : AsyncTask<Int, Int, Wenku8Error.ErrorCode>() {
        private var volumeXml = ""
        private var introXml = ""
        private var volumes: List<VolumeList> = ArrayList()
        private var meta: NovelItemMeta? = null
        private var size = 0
        private var current = 0

        override fun doInBackground(vararg params: Int?): Wenku8Error.ErrorCode {
            if (params.size < 2) return Wenku8Error.ErrorCode.PARAM_COUNT_NOT_MATCHED
            val taskAid = params[0] ?: return Wenku8Error.ErrorCode.PARAM_COUNT_NOT_MATCHED
            val operationType = params[1] ?: return Wenku8Error.ErrorCode.PARAM_COUNT_NOT_MATCHED

            val updateResult = fetchAndWriteIntro(taskAid)
            if (updateResult != Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED) return updateResult
            if (operationType == 0) return Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED

            for (volume in volumes) {
                size += volume.chapterList.orEmpty().size
            }
            progressDialog?.setMaxProgress(size)

            val activeMeta = meta ?: return Wenku8Error.ErrorCode.XML_PARSE_FAILED
            for (volume in volumes) {
                for (chapter in volume.chapterList.orEmpty()) {
                    val result = cacheChapter(activeMeta.aid, chapter.cid, operationType)
                    if (result != Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED) return result
                }
            }

            return Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED
        }

        private fun fetchAndWriteIntro(taskAid: Int): Wenku8Error.ErrorCode {
            if (!isLoading) return Wenku8Error.ErrorCode.USER_CANCELLED_TASK
            val volumeBytes = LightNetwork.LightHttpPostConnection(
                Wenku8API.BASE_URL,
                Wenku8API.getNovelIndex(taskAid, GlobalConfig.getCurrentLang()),
            ) ?: return Wenku8Error.ErrorCode.NETWORK_ERROR
            volumeXml = String(volumeBytes, Charsets.UTF_8)

            if (!isLoading) return Wenku8Error.ErrorCode.USER_CANCELLED_TASK
            val introBytes = LightNetwork.LightHttpPostConnection(
                Wenku8API.BASE_URL,
                Wenku8API.getNovelFullMeta(taskAid, GlobalConfig.getCurrentLang()),
            ) ?: return Wenku8Error.ErrorCode.NETWORK_ERROR
            introXml = String(introBytes, Charsets.UTF_8)

            volumes = Wenku8Parser.getVolumeList(volumeXml)
            val parsedMeta = Wenku8Parser.parseNovelFullMeta(introXml)
            if (volumes.isEmpty() || parsedMeta == null) return Wenku8Error.ErrorCode.XML_PARSE_FAILED
            meta = parsedMeta

            if (!isLoading) return Wenku8Error.ErrorCode.USER_CANCELLED_TASK
            val fullIntroBytes = LightNetwork.LightHttpPostConnection(
                Wenku8API.BASE_URL,
                Wenku8API.getNovelFullIntro(parsedMeta.aid, GlobalConfig.getCurrentLang()),
            ) ?: return Wenku8Error.ErrorCode.NETWORK_ERROR
            parsedMeta.fullIntro = String(fullIntroBytes, Charsets.UTF_8)

            GlobalConfig.writeFullFileIntoSaveFolder("intro", "$taskAid-intro.xml", introXml)
            GlobalConfig.writeFullFileIntoSaveFolder("intro", "$taskAid-introfull.xml", parsedMeta.fullIntro)
            GlobalConfig.writeFullFileIntoSaveFolder("intro", "$taskAid-volume.xml", volumeXml)

            return Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED
        }

        private fun cacheChapter(
            taskAid: Int,
            chapterCid: Int,
            operationType: Int,
        ): Wenku8Error.ErrorCode {
            if (!isLoading) return Wenku8Error.ErrorCode.USER_CANCELLED_TASK
            val contentValues = Wenku8API.getNovelContent(taskAid, chapterCid, GlobalConfig.getCurrentLang())
            var xml = GlobalConfig.loadFullFileFromSaveFolder("novel", "$chapterCid.xml")
            if (xml.isEmpty() || operationType == 2) {
                val bytes = LightNetwork.LightHttpPostConnection(Wenku8API.BASE_URL, contentValues)
                    ?: return Wenku8Error.ErrorCode.NETWORK_ERROR
                xml = String(bytes, Charsets.UTF_8)
                if (xml.trim().isEmpty()) return Wenku8Error.ErrorCode.SERVER_RETURN_NOTHING
                GlobalConfig.writeFullFileIntoSaveFolder("novel", "$chapterCid.xml", xml)
            }

            if (GlobalConfig.doCacheImage()) {
                val contents = OldNovelContentParser.NovelContentParser_onlyImage(xml)
                for (content in contents) {
                    if (content.type == NovelContentType.IMAGE) {
                        progressDialog?.setMaxProgress(++size)
                        val result = cacheImage(content.content, forceUpdate = operationType == 2)
                        if (result != Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED) return result
                        if (!isLoading) return Wenku8Error.ErrorCode.USER_CANCELLED_TASK
                        publishProgress(++current)
                    }
                }
            }

            publishProgress(++current)
            return Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED
        }

        override fun onProgressUpdate(vararg values: Int?) {
            if (values.isNotEmpty()) {
                progressDialog?.setProgress(values[0] ?: 0)
            }
        }

        override fun onPostExecute(result: Wenku8Error.ErrorCode) {
            handleCacheResult(result)
        }
    }

    inner class AsyncRemoveBookFromCloud : AsyncTask<Int, Int, Wenku8Error.ErrorCode>() {
        private lateinit var dialog: ProgressDialogHelper

        override fun onPreExecute() {
            super.onPreExecute()
            dialog = ProgressDialogHelper.show(
                this@NovelInfoActivity,
                getString(R.string.dialog_content_novel_remove_from_cloud),
                true,
                false,
                null,
            )
        }

        override fun doInBackground(vararg params: Int?): Wenku8Error.ErrorCode {
            val targetAid = params.firstOrNull() ?: return Wenku8Error.ErrorCode.PARAM_COUNT_NOT_MATCHED
            val bytes = LightNetwork.LightHttpPostConnection(
                Wenku8API.BASE_URL,
                Wenku8API.getDelFromBookshelfParams(targetAid),
            ) ?: return Wenku8Error.ErrorCode.NETWORK_ERROR

            val result = String(bytes, Charsets.UTF_8)
            Log.d("MewX", result)
            if (!LightTool.isInteger(result)) return Wenku8Error.ErrorCode.RETURNED_VALUE_EXCEPTION
            val errorCode = Wenku8Error.getSystemDefinedErrorCode(result.toInt())
            if (
                errorCode != Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED &&
                errorCode != Wenku8Error.ErrorCode.SYSTEM_4_NOT_LOGGED_IN &&
                errorCode != Wenku8Error.ErrorCode.SYSTEM_7_NOVEL_NOT_IN_BOOKSHELF
            ) {
                return errorCode
            }

            for (volume in listVolume) {
                for (chapter in volume.chapterList.orEmpty()) {
                    LightCache.deleteFile(GlobalConfig.getFirstFullSaveFilePath(), "novel" + File.separator + "${chapter.cid}.xml")
                    LightCache.deleteFile(GlobalConfig.getSecondFullSaveFilePath(), "novel" + File.separator + "${chapter.cid}.xml")
                }
            }

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
            dialog.dismiss()
            if (errorCode == Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED) {
                Toast.makeText(this@NovelInfoActivity, resources.getString(R.string.bookshelf_removed), Toast.LENGTH_SHORT).show()
                fabFavorite.setIcon(resources.getDrawable(R.drawable.ic_favorate))
                fabFavorite.iconTint = ColorStateList.valueOf(resources.getColor(R.color.default_white))
            } else {
                Toast.makeText(this@NovelInfoActivity, errorCode.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private inner class AsyncDownloadVolumes : AsyncTask<Array<Int>, Int, Wenku8Error.ErrorCode>() {
        private var dialog: ProgressDialogHelper? = null
        private var loading = false
        private var size = 0

        override fun onPreExecute() {
            super.onPreExecute()
            loading = true
            dialog = ProgressDialogHelper.show(
                this@NovelInfoActivity,
                getString(R.string.dialog_content_downloading),
                false,
                true,
            ) {
                loading = false
            }
            dialog?.setProgress(0)
            dialog?.setMaxProgress(1)
            size = 0
        }

        override fun doInBackground(vararg params: Array<Int>): Wenku8Error.ErrorCode {
            val selectedVolumes = params.firstOrNull() ?: return Wenku8Error.ErrorCode.PARAM_COUNT_NOT_MATCHED
            var current = 0
            for (volumeIndex in selectedVolumes) {
                size += listVolume[volumeIndex].chapterList.orEmpty().size
                for (chapter in listVolume[volumeIndex].chapterList.orEmpty()) {
                    if (!loading) return Wenku8Error.ErrorCode.USER_CANCELLED_TASK
                    val result = downloadSelectedChapter(chapter.cid)
                    if (result != Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED) return result
                    publishProgress(++current)
                }
            }
            return Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED
        }

        private fun downloadSelectedChapter(chapterCid: Int): Wenku8Error.ErrorCode {
            val contentValues = Wenku8API.getNovelContent(aid, chapterCid, GlobalConfig.getCurrentLang())
            var xml = GlobalConfig.loadFullFileFromSaveFolder("novel", "$chapterCid.xml")
            if (xml.isEmpty()) {
                val bytes = LightNetwork.LightHttpPostConnection(Wenku8API.BASE_URL, contentValues)
                    ?: return Wenku8Error.ErrorCode.NETWORK_ERROR
                xml = String(bytes, Charsets.UTF_8)
                if (xml.trim().isEmpty()) return Wenku8Error.ErrorCode.SERVER_RETURN_NOTHING
                GlobalConfig.writeFullFileIntoSaveFolder("novel", "$chapterCid.xml", xml)
            }

            if (GlobalConfig.doCacheImage()) {
                val contents = OldNovelContentParser.NovelContentParser_onlyImage(xml)
                for (content in contents) {
                    if (content.type == NovelContentType.IMAGE) {
                        size++
                        val result = cacheImage(content.content, forceUpdate = false)
                        if (result != Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED) return result
                        if (!loading) return Wenku8Error.ErrorCode.USER_CANCELLED_TASK
                    }
                }
            }
            return Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            dialog?.setMaxProgress(size)
            dialog?.setProgress(values.firstOrNull() ?: 0)
        }

        override fun onPostExecute(errorCode: Wenku8Error.ErrorCode) {
            super.onPostExecute(errorCode)
            handleDownloadVolumesResult(errorCode)
        }

        private fun handleDownloadVolumesResult(errorCode: Wenku8Error.ErrorCode) {
            when (errorCode) {
                Wenku8Error.ErrorCode.USER_CANCELLED_TASK -> {
                    Toast.makeText(this@NovelInfoActivity, R.string.system_manually_cancelled, Toast.LENGTH_LONG).show()
                    dialog?.dismiss()
                    refreshVolumeListUI()
                    loading = false
                    return
                }
                Wenku8Error.ErrorCode.NETWORK_ERROR -> {
                    Toast.makeText(this@NovelInfoActivity, resources.getString(R.string.system_network_error), Toast.LENGTH_LONG).show()
                    dialog?.dismiss()
                    refreshVolumeListUI()
                    loading = false
                    return
                }
                Wenku8Error.ErrorCode.XML_PARSE_FAILED,
                Wenku8Error.ErrorCode.SERVER_RETURN_NOTHING -> {
                    Toast.makeText(this@NovelInfoActivity, "Server returned strange data! (copyright reason?)", Toast.LENGTH_LONG).show()
                    dialog?.dismiss()
                    refreshVolumeListUI()
                    loading = false
                    return
                }
                else -> Unit
            }

            Toast.makeText(this@NovelInfoActivity, "OK", Toast.LENGTH_LONG).show()
            loading = false
            dialog?.dismiss()
            refreshInfoFromLocal()
        }
    }

    private fun cacheImage(url: String, forceUpdate: Boolean): Wenku8Error.ErrorCode {
        val imageFileName = GlobalConfig.generateImageFileNameByURL(url)
        val firstPath = GlobalConfig.getFirstFullSaveFilePath() +
            GlobalConfig.imgsSaveFolderName + File.separator + imageFileName
        val secondPath = GlobalConfig.getSecondFullSaveFilePath() +
            GlobalConfig.imgsSaveFolderName + File.separator + imageFileName
        if (
            (!LightCache.testFileExist(firstPath) && !LightCache.testFileExist(secondPath)) ||
            forceUpdate
        ) {
            val fileContent = LightNetwork.LightHttpDownload(url)
                ?: return Wenku8Error.ErrorCode.NETWORK_ERROR
            if (
                !LightCache.saveFile(
                    GlobalConfig.getFirstFullSaveFilePath() +
                        GlobalConfig.imgsSaveFolderName + File.separator,
                    imageFileName,
                    fileContent,
                    true,
                )
            ) {
                LightCache.saveFile(
                    GlobalConfig.getSecondFullSaveFilePath() +
                        GlobalConfig.imgsSaveFolderName + File.separator,
                    imageFileName,
                    fileContent,
                    true,
                )
            }
        }
        return Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED
    }

    private fun handleCacheResult(result: Wenku8Error.ErrorCode) {
        when (result) {
            Wenku8Error.ErrorCode.USER_CANCELLED_TASK -> {
                Toast.makeText(this, R.string.system_manually_cancelled, Toast.LENGTH_LONG).show()
                progressDialog?.dismiss()
                refreshVolumeListUI()
                isLoading = false
                return
            }
            Wenku8Error.ErrorCode.NETWORK_ERROR -> {
                Toast.makeText(this, resources.getString(R.string.system_network_error), Toast.LENGTH_LONG).show()
                progressDialog?.dismiss()
                refreshVolumeListUI()
                isLoading = false
                return
            }
            Wenku8Error.ErrorCode.XML_PARSE_FAILED,
            Wenku8Error.ErrorCode.SERVER_RETURN_NOTHING -> {
                Toast.makeText(this, "Server returned strange data! (copyright reason?)", Toast.LENGTH_LONG).show()
                progressDialog?.dismiss()
                refreshVolumeListUI()
                isLoading = false
                return
            }
            else -> Unit
        }

        Toast.makeText(this, "OK", Toast.LENGTH_LONG).show()
        isLoading = false
        progressDialog?.dismiss()
        refreshInfoFromLocal()
    }

    override fun onResume() {
        super.onResume()

        val upArrow = resources.getDrawable(R.drawable.ic_svg_back)
        supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            upArrow.setColorFilter(resources.getColor(R.color.default_white), PorterDuff.Mode.SRC_ATOP)
            setHomeAsUpIndicator(upArrow)
        }

        refreshVolumeListUI()
    }

    /**
     * Safely refreshes the volume list UI without calling onResume().
     * This avoids FragmentManager crashes when called from AsyncTask callbacks
     * after the activity may have been destroyed.
     */
    private fun refreshVolumeListUI() {
        if (isFinishing || isDestroyed) return

        buildVolumeList()
        currentSelectedVolume?.let { buildChapterList(it) }
    }

    private fun refreshInfo() {
        if (from == FROM_LOCAL) {
            refreshInfoFromLocal()
        } else {
            refreshInfoFromCloud()
        }
    }

    private fun refreshInfoFromLocal() {
        if (isLoading) return
        isLoading = true
        errorLayout.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        FetchInfoAsyncTask().execute(1)
    }

    private fun refreshInfoFromCloud() {
        if (isLoading) return
        isLoading = true
        errorLayout.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        FetchInfoAsyncTask().execute()
    }

    private fun fetchAndShowNovelCover() {
        progressDialog = ProgressDialogHelper.show(
            this,
            getString(R.string.system_loading_please_wait),
            true,
            true,
        ) {
            progressDialog?.dismiss()
            progressDialog = null
        }

        Thread {
            try {
                val data = LightNetwork.LightHttpPostConnection(
                    Wenku8API.BASE_URL,
                    Wenku8API.getNovelCover(aid),
                )
                if (data == null || data.isEmpty()) throw Exception("Fetch failed")

                val fileName = "full_cover_$aid.jpg"
                if (GlobalConfig.saveNovelCoverImage(fileName, data)) {
                    runOnUiThread {
                        progressDialog?.dismiss()
                        val intent = Intent(this, ViewImageDetailActivity::class.java)
                        intent.putExtra("path", GlobalConfig.getExistingNovelContentImagePath(fileName))
                        startActivity(intent)
                    }
                } else {
                    throw Exception("Save failed")
                }
            } catch (_: Exception) {
                runOnUiThread {
                    progressDialog?.dismiss()
                    Toast.makeText(this, "Failed to load high-res cover", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private companion object {
        private const val FROM_LOCAL = "fav"
    }
}
