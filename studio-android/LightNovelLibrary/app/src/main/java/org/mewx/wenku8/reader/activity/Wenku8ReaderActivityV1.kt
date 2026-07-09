@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package org.mewx.wenku8.reader.activity

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.graphics.Rect
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.DisplayCutout
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider as MaterialSlider
import com.google.firebase.analytics.FirebaseAnalytics
import com.nostra13.universalimageloader.core.ImageLoader
import java.io.File
import java.io.FileNotFoundException
import org.mewx.wenku8.R
import org.mewx.wenku8.activity.BaseMaterialActivity
import org.mewx.wenku8.api.Wenku8API
import org.mewx.wenku8.api.Wenku8Error
import org.mewx.wenku8.global.GlobalConfig
import org.mewx.wenku8.global.api.ChapterInfo
import org.mewx.wenku8.global.api.OldNovelContentParser
import org.mewx.wenku8.global.api.VolumeList
import org.mewx.wenku8.network.LightNetwork
import org.mewx.wenku8.reader.loader.WenkuReaderLoader
import org.mewx.wenku8.reader.loader.WenkuReaderLoaderXML
import org.mewx.wenku8.reader.setting.WenkuReaderSettingV1
import org.mewx.wenku8.reader.slider.SlidingAdapter
import org.mewx.wenku8.reader.slider.SlidingLayout
import org.mewx.wenku8.reader.slider.base.OverlappedSlider
import org.mewx.wenku8.reader.view.WenkuReaderPageView
import org.mewx.wenku8.util.GoogleServicesHelper
import org.mewx.wenku8.util.LightCache
import org.mewx.wenku8.util.LightTool
import org.mewx.wenku8.util.ProgressDialogHelper

/**
 * Created by MewX on 2015/7/10.
 * Novel Reader Engine V1.
 */
class Wenku8ReaderActivityV1 : BaseMaterialActivity() {
    private var firebaseAnalytics: FirebaseAnalytics? = null
    private var from: String? = ""
    private var aid = 0
    private var cid = 0
    private var forcejump = "no"
    private lateinit var volumeList: VolumeList
    private var novelContent: List<OldNovelContentParser.NovelContent> = ArrayList()
    private lateinit var sliderHolder: RelativeLayout
    private var slidingLayout: SlidingLayout? = null
    private var actionWatchImage: MenuItem? = null

    private lateinit var slidingPageAdapter: SlidingPageAdapter
    private lateinit var loader: WenkuReaderLoader
    private lateinit var setting: WenkuReaderSettingV1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initMaterialStyle(R.layout.layout_reader_swipe_temp, StatusBarColor.DARK)
        installFadeBackCallback()

        firebaseAnalytics = GoogleServicesHelper.initFirebase(this)

        aid = intent.getIntExtra("aid", 1)
        volumeList = intent.getSerializableExtra("volume") as? VolumeList ?: VolumeList()
        cid = intent.getIntExtra("cid", 1)
        from = intent.getStringExtra("from")
        forcejump = intent.getStringExtra("forcejump").orEmpty().ifEmpty { "no" }
        setting = WenkuReaderSettingV1()

        val readerParams = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, aid.toString())
            putString("chapter_id", cid.toString())
            putString("from", from)
            putString("jump_to_saved_page", forcejump)
        }
        GoogleServicesHelper.logEvent(firebaseAnalytics, "reader_v1", readerParams)

        setStatusBarAlpha(0.0f)
        setNavigationBarAlpha(0.0f)
        supportActionBar?.title = volumeList.volumeName

        sliderHolder = findViewById(R.id.slider_holder)

        if (!ImageLoader.getInstance().isInited) {
            GlobalConfig.initImageLoader(this)
        }

        val contentValues = Wenku8API.getNovelContent(aid, cid, GlobalConfig.getCurrentLang())
        AsyncNovelContentTask().execute(contentValues)
    }

    override fun onResume() {
        super.onResume()

        if (findViewById<View>(R.id.reader_bot).visibility != View.VISIBLE) {
            hideNavigationBar()
        } else {
            showNavigationBar()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_reader_v1, menu)
        actionWatchImage = menu.findItem(R.id.action_watch_image)

        menu.getItem(0).icon?.mutate()?.setColorFilter(
            resources.getColor(R.color.default_white),
            PorterDuff.Mode.SRC_ATOP,
        )

        return true
    }

    private fun updateActionWatchImageVisibility(pageView: WenkuReaderPageView?) {
        if (actionWatchImage != null && pageView != null) {
            actionWatchImage?.isVisible = pageView.hasImageInPage()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (Build.VERSION.SDK_INT >= 28) {
            val cutout: DisplayCutout? = window.decorView.rootWindowInsets?.displayCutout
            if (cutout != null) {
                LightTool.setDisplayCutout(
                    Rect(
                        cutout.safeInsetLeft,
                        cutout.safeInsetTop,
                        cutout.safeInsetRight,
                        cutout.safeInsetBottom,
                    ),
                )
            }
        }
    }

    private fun hideNavigationBar() {
        val flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = flags

        val decorView = window.decorView
        decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                decorView.systemUiVisibility = flags
            }
        }
    }

    private fun showNavigationBar() {
        val flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = flags

        val decorView = window.decorView
        decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                decorView.systemUiVisibility = flags
            }
        }
    }

    override fun onPause() {
        super.onPause()

        if (::slidingPageAdapter.isInitialized && ::loader.isInitialized) {
            loader.setCurrentIndex(slidingPageAdapter.getCurrentLastLineIndex())
            val chapters = chapters()
            if (
                chapters.size > 1 &&
                chapters[chapters.size - 1].cid == cid &&
                slidingPageAdapter.getCurrentLastWordIndex() == loader.getCurrentStringLength() - 1
            ) {
                GlobalConfig.removeReadSavesRecordV1(aid)
            } else {
                GlobalConfig.addReadSavesRecordV1(
                    aid,
                    volumeList.vid,
                    cid,
                    slidingPageAdapter.getCurrentFirstLineIndex(),
                    slidingPageAdapter.getCurrentFirstWordIndex(),
                )
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        when (event.keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                gotoNextPage()
                return true
            }
            KeyEvent.KEYCODE_VOLUME_UP -> {
                gotoPreviousPage()
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    inner class SlidingPageAdapter(
        begLineIndex: Int,
        begWordIndex: Int,
    ) : SlidingAdapter<WenkuReaderPageView>() {
        private var firstLineIndex = begLineIndex
        private var firstWordIndex = begWordIndex
        private var lastLineIndex = 0
        private var lastWordIndex = 0

        private var nextPage: WenkuReaderPageView? = null
        private var previousPage: WenkuReaderPageView? = null
        private var isLoadingNext = false
        private var isLoadingPrevious = false

        init {
            if (firstLineIndex + 1 >= loader.getElementCount()) {
                firstLineIndex = loader.getElementCount() - 1
            }
            loader.setCurrentIndex(firstLineIndex)
            if (firstWordIndex + 1 >= loader.getCurrentStringLength()) {
                firstLineIndex--
                firstWordIndex = 0
                if (firstLineIndex < 0) {
                    firstLineIndex = 0
                }
            }
        }

        override fun getView(contentView: View?, item: WenkuReaderPageView): View {
            Log.d("MewX", "-- slider getView")
            val view = contentView ?: layoutInflater.inflate(R.layout.layout_reader_swipe_page, null)

            val pageHolder = view.findViewById<RelativeLayout>(R.id.page_holder)
            pageHolder.removeAllViews()
            val layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            pageHolder.addView(item, layoutParams)

            return view
        }

        fun getCurrentFirstLineIndex(): Int = firstLineIndex

        fun getCurrentFirstWordIndex(): Int = firstWordIndex

        fun getCurrentLastLineIndex(): Int = lastLineIndex

        fun getCurrentLastWordIndex(): Int = lastWordIndex

        fun setCurrentIndex(lineIndex: Int, wordIndex: Int) {
            firstLineIndex = if (lineIndex + 1 >= loader.getElementCount()) {
                loader.getElementCount() - 1
            } else {
                lineIndex
            }
            loader.setCurrentIndex(firstLineIndex)
            firstWordIndex = if (wordIndex + 1 >= loader.getCurrentStringLength()) {
                loader.getCurrentStringLength() - 1
            } else {
                wordIndex
            }

            val temp = WenkuReaderPageView(
                this@Wenku8ReaderActivityV1,
                firstLineIndex,
                firstWordIndex,
                WenkuReaderPageView.LOADING_DIRECTION.CURRENT,
            )
            firstLineIndex = temp.getFirstLineIndex()
            firstWordIndex = temp.getFirstWordIndex()
            lastLineIndex = temp.getLastLineIndex()
            lastWordIndex = temp.getLastWordIndex()
        }

        override fun hasNext(): Boolean {
            Log.d("MewX", "-- slider hasNext")
            loader.setCurrentIndex(lastLineIndex)
            return !isLoadingNext && loader.hasNext(lastWordIndex)
        }

        override fun computeNext() {
            Log.d("MewX", "-- slider computeNext")
            nextPage = WenkuReaderPageView(
                this@Wenku8ReaderActivityV1,
                lastLineIndex,
                lastWordIndex,
                WenkuReaderPageView.LOADING_DIRECTION.FORWARDS,
            )
            val activeNextPage = requireNotNull(nextPage)
            firstLineIndex = activeNextPage.getFirstLineIndex()
            firstWordIndex = activeNextPage.getFirstWordIndex()
            lastLineIndex = activeNextPage.getLastLineIndex()
            lastWordIndex = activeNextPage.getLastWordIndex()
            printLog()
        }

        override fun computePrevious() {
            Log.d("MewX", "-- slider computePrevious")
            val page = WenkuReaderPageView(
                this@Wenku8ReaderActivityV1,
                firstLineIndex,
                firstWordIndex,
                WenkuReaderPageView.LOADING_DIRECTION.BACKWARDS,
            )
            previousPage = page
            firstLineIndex = page.getFirstLineIndex()
            firstWordIndex = page.getFirstWordIndex()
            lastLineIndex = page.getLastLineIndex()
            lastWordIndex = page.getLastWordIndex()

            printLog()
        }

        override fun getNext(): WenkuReaderPageView {
            Log.d("MewX", "-- slider getNext")
            nextPage = WenkuReaderPageView(
                this@Wenku8ReaderActivityV1,
                lastLineIndex,
                lastWordIndex,
                WenkuReaderPageView.LOADING_DIRECTION.FORWARDS,
            )
            return requireNotNull(nextPage)
        }

        override fun hasPrevious(): Boolean {
            Log.d("MewX", "-- slider hasPrevious")
            loader.setCurrentIndex(firstLineIndex)
            return !isLoadingPrevious && loader.hasPrevious(firstWordIndex)
        }

        override fun getPrevious(): WenkuReaderPageView {
            Log.d("MewX", "-- slider getPrevious")
            previousPage = WenkuReaderPageView(
                this@Wenku8ReaderActivityV1,
                firstLineIndex,
                firstWordIndex,
                WenkuReaderPageView.LOADING_DIRECTION.BACKWARDS,
            )
            return requireNotNull(previousPage)
        }

        override fun getCurrent(): WenkuReaderPageView {
            Log.d("MewX", "-- slider getCurrent")
            val temp = WenkuReaderPageView(
                this@Wenku8ReaderActivityV1,
                firstLineIndex,
                firstWordIndex,
                WenkuReaderPageView.LOADING_DIRECTION.CURRENT,
            )
            firstLineIndex = temp.getFirstLineIndex()
            firstWordIndex = temp.getFirstWordIndex()
            lastLineIndex = temp.getLastLineIndex()
            lastWordIndex = temp.getLastWordIndex()
            printLog()
            return temp
        }

        private fun printLog() {
            Log.d(
                "MewX",
                "saved index: $firstLineIndex($firstWordIndex) -> $lastLineIndex($lastWordIndex) | " +
                    "Total: ${loader.getCurrentIndex()} of ${loader.getElementCount() - 1}",
            )
        }
    }

    inner class AsyncNovelContentTask : AsyncTask<ContentValues, Int, Wenku8Error.ErrorCode>() {
        private var progressDialog: ProgressDialogHelper? = null

        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog = ProgressDialogHelper.show(
                this@Wenku8ReaderActivityV1,
                getString(R.string.reader_engine_v1_parsing),
                true,
                false,
                null,
            )
            progressDialog?.setTitle(getString(R.string.reader_please_wait))
        }

        override fun doInBackground(vararg params: ContentValues): Wenku8Error.ErrorCode {
            val xml = if (from == FROM_LOCAL) {
                GlobalConfig.loadFullFileFromSaveFolder("novel", "$cid.xml")
            } else {
                val tempXml = LightNetwork.LightHttpPostConnection(Wenku8API.BASE_URL, params[0])
                    ?: return Wenku8Error.ErrorCode.NETWORK_ERROR
                String(tempXml, Charsets.UTF_8)
            }

            novelContent = OldNovelContentParser.parseNovelContent(xml) { }
            if (novelContent.isEmpty()) {
                return if (xml.isEmpty()) {
                    Wenku8Error.ErrorCode.SERVER_RETURN_NOTHING
                } else {
                    Wenku8Error.ErrorCode.XML_PARSE_FAILED
                }
            }

            return Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED
        }

        override fun onPostExecute(result: Wenku8Error.ErrorCode) {
            if (isFinishing || isDestroyed) return

            if (result != Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED) {
                Toast.makeText(this@Wenku8ReaderActivityV1, result.toString(), Toast.LENGTH_LONG).show()
                dismissProgressDialog()
                finish()
                return
            }
            Log.d("MewX", "-- 小说获取完成")

            loader = WenkuReaderLoaderXML(novelContent)
            loader.setCurrentIndex(0)
            for (chapterInfo in chapters()) {
                if (chapterInfo.cid == cid) {
                    loader.setChapterName(chapterInfo.chapterName)
                    break
                }
            }

            slidingPageAdapter = SlidingPageAdapter(0, 0)
            WenkuReaderPageView.setViewComponents(loader, setting, false)
            Log.d("MewX", "-- loader, setting 初始化完成")
            val nextSlidingLayout = SlidingLayout(this@Wenku8ReaderActivityV1)
            slidingLayout = nextSlidingLayout
            val layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            nextSlidingLayout.setAdapter(slidingPageAdapter)
            nextSlidingLayout.setSlider(OverlappedSlider())
            nextSlidingLayout.setOnTapListener(object : SlidingLayout.OnTapListener {
                private var barStatus = false
                private var isSet = false

                override fun onSingleTap(event: MotionEvent) {
                    val screenWidth = resources.displayMetrics.widthPixels
                    val screenHeight = resources.displayMetrics.heightPixels
                    val x = event.x.toInt()
                    val y = event.y.toInt()

                    if (
                        x > screenWidth / 3 &&
                        x < screenWidth * 2 / 3 &&
                        y > screenHeight / 3 &&
                        y < screenHeight * 2 / 3
                    ) {
                        if (!barStatus) {
                            showNavigationBar()
                            findViewById<View>(R.id.reader_top).visibility = View.VISIBLE
                            findViewById<View>(R.id.reader_bot).visibility = View.VISIBLE

                            setStatusBarAlpha(0.90f)
                            setNavigationBarAlpha(0.80f)

                            barStatus = true

                            if (!isSet) {
                                bindReaderControlListeners()
                            }
                        } else {
                            hideNavigationBar()
                            findViewById<View>(R.id.reader_top).visibility = View.INVISIBLE
                            findViewById<View>(R.id.reader_bot).visibility = View.INVISIBLE
                            findViewById<View>(R.id.reader_bot_seeker).visibility = View.INVISIBLE
                            findViewById<View>(R.id.reader_bot_settings).visibility = View.INVISIBLE

                            setStatusBarAlpha(0.0f)
                            setNavigationBarAlpha(0.0f)

                            barStatus = false
                        }
                        return
                    }

                    if (x > screenWidth / 2) {
                        gotoNextPage()
                    } else {
                        gotoPreviousPage()
                    }
                }
            })
            nextSlidingLayout.setOnSlideChangeListener(object : SlidingLayout.OnSlideChangeListener {
                override fun onSlideScrollStateChanged(touchResult: Int) = Unit

                override fun onSlideSelected(obj: Any?) {
                    if (obj is WenkuReaderPageView) {
                        updateActionWatchImageVisibility(obj)
                    }
                }
            })
            nextSlidingLayout.slideSelected(slidingPageAdapter.getCurrent())
            sliderHolder.addView(nextSlidingLayout, 0, layoutParams)
            Log.d("MewX", "-- slider创建完毕")

            dismissProgressDialog()
            offerJumpToSavedPosition()
        }

        private fun dismissProgressDialog() {
            val dialog = progressDialog
            if (dialog != null && dialog.isShowing()) {
                try {
                    dialog.dismiss()
                } catch (_: Exception) {
                    // Do nothing.
                }
            }
        }
    }

    private fun bindReaderControlListeners() {
        findViewById<View>(R.id.btn_daylight).setOnClickListener {
            WenkuReaderPageView.switchDayMode()
            WenkuReaderPageView.resetTextColor()
            resetReaderPages(forceMode = false)
        }
        findViewById<View>(R.id.btn_daylight).setOnLongClickListener {
            Toast.makeText(this, resources.getString(R.string.reader_daynight), Toast.LENGTH_SHORT).show()
            true
        }

        findViewById<View>(R.id.btn_jump).setOnClickListener(object : View.OnClickListener {
            private var isOpen = false

            override fun onClick(v: View) {
                if (
                    findViewById<View>(R.id.reader_bot_settings).visibility == View.VISIBLE ||
                    findViewById<View>(R.id.reader_bot_seeker).visibility == View.INVISIBLE
                ) {
                    isOpen = false
                    findViewById<View>(R.id.reader_bot_settings).visibility = View.INVISIBLE
                }
                findViewById<View>(R.id.reader_bot_seeker).visibility = if (!isOpen) {
                    View.VISIBLE
                } else {
                    View.INVISIBLE
                }
                isOpen = !isOpen

                val seeker = findViewById<MaterialSlider>(R.id.reader_seekbar)
                seeker.valueFrom = 1.0f
                seeker.valueTo = kotlin.math.max(1.0f, loader.getElementCount().toFloat())
                seeker.value = kotlin.math.min(
                    seeker.valueTo,
                    slidingPageAdapter.getCurrentFirstLineIndex() + 1.0f,
                )

                seeker.addOnSliderTouchListener(object : MaterialSlider.OnSliderTouchListener {
                    override fun onStartTrackingTouch(slider: MaterialSlider) = Unit

                    override fun onStopTrackingTouch(slider: MaterialSlider) {
                        slidingPageAdapter.setCurrentIndex(slider.value.toInt() - 1, 0)
                        slidingPageAdapter.restoreState(null, null)
                        slidingPageAdapter.notifyDataSetChanged()
                    }
                })
            }
        })
        findViewById<View>(R.id.btn_jump).setOnLongClickListener {
            Toast.makeText(this, resources.getString(R.string.reader_jump), Toast.LENGTH_SHORT).show()
            true
        }

        findViewById<View>(R.id.btn_find).setOnClickListener {
            Toast.makeText(this, "查找功能尚未就绪", Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.btn_find).setOnLongClickListener {
            Toast.makeText(this, resources.getString(R.string.reader_find), Toast.LENGTH_SHORT).show()
            true
        }

        findViewById<View>(R.id.btn_config).setOnClickListener(object : View.OnClickListener {
            private var isOpen = false

            override fun onClick(v: View) {
                if (
                    findViewById<View>(R.id.reader_bot_seeker).visibility == View.VISIBLE ||
                    findViewById<View>(R.id.reader_bot_settings).visibility == View.INVISIBLE
                ) {
                    isOpen = false
                    findViewById<View>(R.id.reader_bot_seeker).visibility = View.INVISIBLE
                }
                findViewById<View>(R.id.reader_bot_settings).visibility = if (!isOpen) {
                    View.VISIBLE
                } else {
                    View.INVISIBLE
                }
                isOpen = !isOpen

                bindReaderSettingControls()
            }
        })
        findViewById<View>(R.id.btn_config).setOnLongClickListener {
            Toast.makeText(this, resources.getString(R.string.reader_config), Toast.LENGTH_SHORT).show()
            true
        }

        findViewById<View>(R.id.text_previous).setOnClickListener {
            offerPreviousChapter()
        }

        findViewById<View>(R.id.text_next).setOnClickListener {
            offerNextChapter()
        }
    }

    private fun bindReaderSettingControls() {
        val seekerFontSize = findViewById<MaterialSlider>(R.id.reader_font_size_seeker)
        val seekerLineDistance = findViewById<MaterialSlider>(R.id.reader_line_distance_seeker)
        val seekerParagraphDistance = findViewById<MaterialSlider>(R.id.reader_paragraph_distance_seeker)
        val seekerParagraphEdgeDistance = findViewById<MaterialSlider>(R.id.reader_paragraph_edge_distance_seeker)

        seekerFontSize.value = setting.getFontSize().toFloat()
        seekerFontSize.addOnSliderTouchListener(object : MaterialSlider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: MaterialSlider) = Unit

            override fun onStopTrackingTouch(slider: MaterialSlider) {
                setting.setFontSize(slider.value.toInt())
                resetReaderPages(forceMode = false)
            }
        })

        seekerLineDistance.value = setting.getLineDistance().toFloat()
        seekerLineDistance.addOnSliderTouchListener(object : MaterialSlider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: MaterialSlider) = Unit

            override fun onStopTrackingTouch(slider: MaterialSlider) {
                setting.setLineDistance(slider.value.toInt())
                resetReaderPages(forceMode = false)
            }
        })

        seekerParagraphDistance.value = setting.getParagraphDistance().toFloat()
        seekerParagraphDistance.addOnSliderTouchListener(object : MaterialSlider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: MaterialSlider) = Unit

            override fun onStopTrackingTouch(slider: MaterialSlider) {
                setting.setParagraphDistance(slider.value.toInt())
                resetReaderPages(forceMode = false)
            }
        })

        seekerParagraphEdgeDistance.value = setting.getPageEdgeDistance().toFloat()
        seekerParagraphEdgeDistance.addOnSliderTouchListener(object : MaterialSlider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: MaterialSlider) = Unit

            override fun onStopTrackingTouch(slider: MaterialSlider) {
                setting.setPageEdgeDistance(slider.value.toInt())
                resetReaderPages(forceMode = false)
            }
        })

        findViewById<View>(R.id.btn_custom_font).setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.reader_custom_font)
                .setItems(R.array.reader_font_option) { _, which ->
                    when (which) {
                        0 -> {
                            setting.setUseCustomFont(false)
                            resetReaderPages(forceMode = false)
                        }
                        1 -> {
                            val intent = Intent().apply {
                                action = Intent.ACTION_OPEN_DOCUMENT
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "font/*"
                            }
                            startActivityForResult(intent, REQUEST_FONT_PICKER)
                        }
                    }
                }
                .show()
        }

        findViewById<View>(R.id.btn_custom_background).setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.reader_custom_background)
                .setItems(R.array.reader_background_option) { _, which ->
                    when (which) {
                        0 -> {
                            setting.setPageBackgroundType(
                                WenkuReaderSettingV1.PAGE_BACKGROUND_TYPE.SYSTEM_DEFAULT,
                            )
                            resetReaderPages(forceMode = true)
                        }
                        1 -> {
                            val intent = Intent().apply {
                                action = Intent.ACTION_OPEN_DOCUMENT
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "image/*"
                            }
                            startActivityForResult(intent, REQUEST_IMAGE_PICKER)
                        }
                    }
                }
                .show()
        }
    }

    private fun offerJumpToSavedPosition() {
        val readSave = GlobalConfig.getReadSavesRecordV1(aid)
        if (readSave != null && readSave.vid == volumeList.vid && readSave.cid == cid) {
            if (forcejump == "yes") {
                slidingPageAdapter.setCurrentIndex(readSave.lineId, readSave.wordId)
                slidingPageAdapter.restoreState(null, null)
                slidingPageAdapter.notifyDataSetChanged()
            } else if (
                slidingPageAdapter.getCurrentFirstLineIndex() != readSave.lineId ||
                slidingPageAdapter.getCurrentFirstWordIndex() != readSave.wordId
            ) {
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.reader_v1_notice)
                    .setMessage(R.string.reader_jump_last)
                    .setPositiveButton(R.string.dialog_positive_sure) { _, _ ->
                        slidingPageAdapter.setCurrentIndex(readSave.lineId, readSave.wordId)
                        slidingPageAdapter.restoreState(null, null)
                        slidingPageAdapter.notifyDataSetChanged()
                    }
                    .setNegativeButton(R.string.dialog_negative_biao, null)
                    .show()
            }
        }
    }

    private fun offerPreviousChapter() {
        val chapters = chapters()
        for (i in chapters.indices) {
            if (cid == chapters[i].cid) {
                if (i == 0) {
                    Toast.makeText(this, resources.getString(R.string.reader_already_first_chapter), Toast.LENGTH_SHORT).show()
                } else {
                    showChapterJumpDialog(chapters[i - 1])
                }
                break
            }
        }
    }

    private fun offerNextChapter() {
        val chapters = chapters()
        for (i in chapters.indices) {
            if (cid == chapters[i].cid) {
                if (i + 1 >= chapters.size) {
                    Toast.makeText(this, resources.getString(R.string.reader_already_last_chapter), Toast.LENGTH_SHORT).show()
                } else {
                    showChapterJumpDialog(chapters[i + 1])
                }
                break
            }
        }
    }

    private fun showChapterJumpDialog(chapter: ChapterInfo) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_sure_to_jump_chapter)
            .setMessage(chapter.chapterName)
            .setPositiveButton(R.string.dialog_positive_yes) { _, _ ->
                val intent = Intent(this, Wenku8ReaderActivityV1::class.java).apply {
                    putExtra("aid", aid)
                    putExtra("volume", volumeList)
                    putExtra("cid", chapter.cid)
                    putExtra("from", from)
                }
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.hold)
                finish()
            }
            .setNegativeButton(R.string.dialog_negative_no, null)
            .show()
    }

    private fun gotoNextPage() {
        if (::slidingPageAdapter.isInitialized && !slidingPageAdapter.hasNext()) {
            offerNextChapter()
        } else {
            slidingLayout?.slideNext()
        }
    }

    private fun gotoPreviousPage() {
        if (::slidingPageAdapter.isInitialized && !slidingPageAdapter.hasPrevious()) {
            offerPreviousChapter()
        } else {
            slidingLayout?.slidePrevious()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_FONT_PICKER && resultCode == Activity.RESULT_OK && data != null) {
            val fontUri: Uri = data.data ?: return
            val copiedFilePath = GlobalConfig.getDefaultStoragePath() +
                GlobalConfig.customFolderName + File.separator + "reader_font"
            try {
                val inputStream = applicationContext.contentResolver.openInputStream(fontUri)
                    ?: throw FileNotFoundException(fontUri.toString())
                LightCache.copyFile(inputStream, copiedFilePath, true)
                runSaveCustomFontPath(copiedFilePath.replace("file://", ""))
            } catch (exception: FileNotFoundException) {
                exception.printStackTrace()
                Toast.makeText(this, "Exception: $exception", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == REQUEST_IMAGE_PICKER && resultCode == Activity.RESULT_OK && data != null) {
            val mediaUri: Uri = data.data ?: return
            val copiedFilePath = GlobalConfig.getDefaultStoragePath() +
                GlobalConfig.customFolderName + File.separator + "reader_background"
            try {
                val inputStream = applicationContext.contentResolver.openInputStream(mediaUri)
                    ?: throw FileNotFoundException(mediaUri.toString())
                LightCache.copyFile(inputStream, copiedFilePath, true)
                runSaveCustomBackgroundPath(copiedFilePath.replace("file://", ""))
            } catch (exception: FileNotFoundException) {
                exception.printStackTrace()
                Toast.makeText(this, "Exception: $exception", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun runSaveCustomFontPath(path: String) {
        setting.setCustomFontPath(path)
        resetReaderPages(forceMode = false)
    }

    private fun runSaveCustomBackgroundPath(path: String) {
        try {
            BitmapFactory.decodeFile(path)
        } catch (outOfMemoryError: OutOfMemoryError) {
            try {
                val options = BitmapFactory.Options().apply {
                    inSampleSize = 2
                }
                val bitmap: Bitmap? = BitmapFactory.decodeFile(path, options)
                if (bitmap == null) {
                    throw Exception("PictureDecodeFailedException")
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
                Toast.makeText(
                    this,
                    "Exception: $exception\n可能的原因有：图片不在内置SD卡；图片格式不正确；图片像素尺寸太大，请使用小一点的图，谢谢，此功能为试验性功能；",
                    Toast.LENGTH_LONG,
                ).show()
                return
            }
        }
        setting.setPageBackgroundCustomPath(path)
        resetReaderPages(forceMode = true)
    }

    private fun resetReaderPages(forceMode: Boolean) {
        WenkuReaderPageView.setViewComponents(loader, setting, forceMode)
        slidingPageAdapter.restoreState(null, null)
        slidingPageAdapter.notifyDataSetChanged()
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                return true
            }
            R.id.action_watch_image -> {
                val currentView = slidingLayout?.getAdapter()?.getCurrentView()
                val pageView = (currentView as? RelativeLayout)?.getChildAt(0) as? WenkuReaderPageView
                pageView?.watchImageDetailed(this)
            }
        }
        return super.onOptionsItemSelected(menuItem)
    }

    private fun chapters(): List<ChapterInfo> = volumeList.chapterList.orEmpty()

    private companion object {
        private const val FROM_LOCAL = "fav"
        private const val REQUEST_FONT_PICKER = 100
        private const val REQUEST_IMAGE_PICKER = 101
    }
}
