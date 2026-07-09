@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package org.mewx.wenku8.activity

import android.content.ContentValues
import android.content.Intent
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.nostra13.universalimageloader.core.ImageLoader
import org.mewx.wenku8.R
import org.mewx.wenku8.api.Wenku8API
import org.mewx.wenku8.component.ScrollViewNoFling
import org.mewx.wenku8.global.GlobalConfig
import org.mewx.wenku8.global.api.OldNovelContentParser
import org.mewx.wenku8.global.api.OldNovelContentParser.NovelContent
import org.mewx.wenku8.global.api.OldNovelContentParser.NovelContentType
import org.mewx.wenku8.global.api.VolumeList
import org.mewx.wenku8.network.LightNetwork
import org.mewx.wenku8.util.GoogleServicesHelper
import org.mewx.wenku8.util.ProgressDialogHelper

class VerticalReaderActivity : AppCompatActivity() {
    private var from: String? = ""
    private var aid = 0
    private var cid = 0
    private var volumeList: VolumeList? = null
    private var progressDialog: ProgressDialogHelper? = null
    private var scrollView: ScrollViewNoFling? = null
    private var textListLayout: LinearLayout? = null
    private var novelContents: List<NovelContent>? = null

    private val runnableScroll = Runnable {
        val layout = textListLayout ?: return@Runnable
        findViewById<View>(R.id.content_scrollview)
            .scrollTo(0, GlobalConfig.getReadSavesRecord(cid, layout.measuredHeight))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.layout_vertical_reader_temp)
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finish()
                    overridePendingTransition(0, R.anim.fade_out)
                }
            },
        )

        GoogleServicesHelper.initFirebase(this)

        aid = intent.getIntExtra("aid", 1)
        volumeList = intent.getSerializableExtra("volume") as? VolumeList
        cid = intent.getIntExtra("cid", 1)
        from = intent.getStringExtra("from")

        val imageLoader = ImageLoader.getInstance()
        if (imageLoader == null || !imageLoader.isInited) {
            GlobalConfig.initImageLoader(this)
        }

        getNovelContent()

        textListLayout = findViewById(R.id.novel_content_layout)
        scrollView = findViewById(R.id.content_scrollview)
        scrollView?.setOnTouchListener(createReaderTouchListener())

        Toast.makeText(this, getString(R.string.notice_volume_to_dark_mode), Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()

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

    private fun createReaderTouchListener(): View.OnTouchListener {
        val gestureDetector = GestureDetector(
            this,
            object : GestureDetector.OnGestureListener {
                override fun onDown(event: MotionEvent): Boolean = false

                override fun onShowPress(event: MotionEvent) = Unit

                override fun onSingleTapUp(event: MotionEvent): Boolean {
                    val screenHeight = resources.displayMetrics.heightPixels
                    val y = event.y.toInt()
                    val readerScrollView = scrollView ?: return false

                    return when {
                        y < screenHeight * 5 / 6 && y >= screenHeight / 2 -> {
                            readerScrollView.smoothScrollBy(0, screenHeight / 2)
                            true
                        }
                        y < screenHeight / 2 && y > screenHeight / 6 -> {
                            readerScrollView.smoothScrollBy(0, -screenHeight / 2)
                            true
                        }
                        else -> false
                    }
                }

                override fun onScroll(
                    firstEvent: MotionEvent?,
                    secondEvent: MotionEvent,
                    distanceX: Float,
                    distanceY: Float,
                ): Boolean = false

                override fun onLongPress(event: MotionEvent) = Unit

                override fun onFling(
                    firstEvent: MotionEvent?,
                    secondEvent: MotionEvent,
                    velocityX: Float,
                    velocityY: Float,
                ): Boolean = false
            },
        )

        return View.OnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
    }

    private fun getNovelContent() {
        val contentValues = Wenku8API.getNovelContent(aid, cid, GlobalConfig.getCurrentLang())
        val task = AsyncNovelContentTask()
        task.execute(contentValues)

        progressDialog = ProgressDialogHelper.show(
            this,
            getString(R.string.sorry_old_engine_merging),
            false,
            true,
        ) {
            task.cancel(true)
            progressDialog?.dismiss()
            progressDialog = null
        }
    }

    private inner class AsyncNovelContentTask : AsyncTask<ContentValues, Int, Int>() {
        override fun doInBackground(vararg params: ContentValues): Int {
            val xml = if (from == FROM_LOCAL) {
                GlobalConfig.loadFullFileFromSaveFolder("novel", "$cid.xml")
            } else {
                val tempXml = LightNetwork.LightHttpPostConnection(Wenku8API.BASE_URL, params[0])
                    ?: return NETWORK_ERROR
                String(tempXml, Charsets.UTF_8)
            }

            novelContents = OldNovelContentParser.parseNovelContent(xml) { size ->
                progressDialog?.setMaxProgress(size)
            }

            if (novelContents.orEmpty().isEmpty()) {
                Log.e("MewX-Main", "getNullFromParser (NovelContentParser.parseNovelContent(xml);)")
                return NETWORK_ERROR
            }

            return SUCCESS
        }

        override fun onPostExecute(result: Int) {
            if (result == NETWORK_ERROR) {
                Toast.makeText(
                    this@VerticalReaderActivity,
                    resources.getString(R.string.system_network_error),
                    Toast.LENGTH_LONG,
                ).show()
                progressDialog?.dismiss()
                return
            }

            val contents = novelContents ?: run {
                progressDialog?.dismiss()
                return
            }
            val layout = textListLayout ?: run {
                progressDialog?.dismiss()
                return
            }

            contents.forEachIndexed { index, content ->
                progressDialog?.setProgress(index)

                when (content.type) {
                    NovelContentType.TEXT -> layout.addView(createTextView(index, content))
                    NovelContentType.IMAGE -> layout.addView(createImageView(content))
                }
            }

            progressDialog?.dismiss()

            if (GlobalConfig.getReadSavesRecord(cid, layout.measuredHeight) > 100) {
                Handler(Looper.getMainLooper()).postDelayed(runnableScroll, 200)
                Log.d(
                    VerticalReaderActivity::class.java.simpleName,
                    "Scroll to = ${GlobalConfig.getReadSavesRecord(cid, layout.measuredHeight)}",
                )
            }
        }
    }

    private fun createTextView(index: Int, content: NovelContent): TextView {
        val textView = TextView(this)
        if (index == 0) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, (GlobalConfig.getShowTextSize() + 6).toFloat())
            val shader = LinearGradient(
                0f,
                0f,
                0f,
                textView.textSize,
                0xFF003399.toInt(),
                0xFF6699FF.toInt(),
                Shader.TileMode.CLAMP,
            )
            textView.paint.shader = shader
        } else {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, GlobalConfig.getShowTextSize().toFloat())
        }

        textView.text = content.content
        textView.setLineSpacing(GlobalConfig.getShowTextSize() * 1.0f, 1.0f)
        textView.setTextColor(ContextCompat.getColor(this, R.color.reader_default_text_dark))
        textView.setPadding(
            GlobalConfig.getShowTextPaddingLeft(),
            GlobalConfig.getShowTextPaddingTop(),
            GlobalConfig.getShowTextPaddingRight(),
            0,
        )
        return textView
    }

    private fun createImageView(content: NovelContent): ImageView {
        val imageView = ImageView(this)
        imageView.isClickable = true
        imageView.adjustViewBounds = true
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        imageView.setPadding(0, GlobalConfig.getShowTextPaddingTop(), 0, 0)
        imageView.setImageResource(R.drawable.ic_empty_image)

        val imgFileName = GlobalConfig.generateImageFileNameByURL(content.content)
        val path = GlobalConfig.getExistingNovelContentImagePath(imgFileName)

        if (path != null) {
            ImageLoader.getInstance().displayImage("file://$path", imageView)
            bindImageClick(imageView, path)
        } else {
            AsyncDownloadImageTask(imageView).execute(content.content)
        }

        return imageView
    }

    private inner class AsyncDownloadImageTask(
        private val imageView: ImageView,
    ) : AsyncTask<String, Int, String?>() {
        override fun doInBackground(vararg params: String): String? {
            GlobalConfig.saveNovelContentImage(params[0])
            val name = GlobalConfig.generateImageFileNameByURL(params[0])
            return GlobalConfig.getExistingNovelContentImagePath(name)
        }

        override fun onPostExecute(result: String?) {
            if (result == null) {
                return
            }

            ImageLoader.getInstance().displayImage("file://$result", imageView)
            bindImageClick(imageView, result)
        }
    }

    private fun bindImageClick(imageView: ImageView, path: String) {
        imageView.setOnClickListener {
            val intent = Intent(this, ViewImageDetailActivity::class.java)
            intent.putExtra("path", path)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.hold)
        }
    }

    override fun onPause() {
        super.onPause()
        saveRecord()
    }

    private fun saveRecord() {
        val layout = textListLayout ?: return
        GlobalConfig.addReadSavesRecord(
            cid,
            findViewById<View>(R.id.content_scrollview).scrollY,
            layout.measuredHeight,
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        progressDialog?.dismiss()
        progressDialog = null
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val layout = textListLayout ?: return super.onKeyDown(keyCode, event)
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                layout.setBackgroundColor(ContextCompat.getColor(this, R.color.reader_default_bg_black))
                for (i in 1 until layout.childCount) {
                    val child = layout.getChildAt(i)
                    if (child is TextView) {
                        child.setTextColor(ContextCompat.getColor(this, R.color.reader_default_text_light))
                    }
                }
                return true
            }

            KeyEvent.KEYCODE_VOLUME_UP -> {
                layout.setBackgroundColor(ContextCompat.getColor(this, R.color.reader_default_bg_yellow))
                for (i in 1 until layout.childCount) {
                    val child = layout.getChildAt(i)
                    if (child is TextView) {
                        child.setTextColor(ContextCompat.getColor(this, R.color.reader_default_text_dark))
                    }
                }
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private companion object {
        const val FROM_LOCAL = "fav"
        const val SUCCESS = 0
        const val NETWORK_ERROR = -100
    }
}
