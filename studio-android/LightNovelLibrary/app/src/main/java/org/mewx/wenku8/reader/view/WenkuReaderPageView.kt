@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package org.mewx.wenku8.reader.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.os.AsyncTask
import android.text.TextPaint
import android.util.Log
import android.util.Pair
import android.view.View
import android.widget.Toast
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.ImageSize
import java.util.Random
import org.mewx.wenku8.MyApp
import org.mewx.wenku8.R
import org.mewx.wenku8.activity.ViewImageDetailActivity
import org.mewx.wenku8.api.Wenku8Error
import org.mewx.wenku8.global.GlobalConfig
import org.mewx.wenku8.reader.loader.WenkuReaderLoader
import org.mewx.wenku8.reader.setting.WenkuReaderSettingV1
import org.mewx.wenku8.util.LightTool

class WenkuReaderPageView(
    context: Context,
    lineIndex: Int,
    wordIndex: Int,
    directionForward: LOADING_DIRECTION,
) : View(context) {
    enum class LOADING_DIRECTION {
        FORWARDS,
        CURRENT,
        BACKWARDS,
    }

    private inner class BitmapInfo {
        var idxLineInfo: Int = 0
        var width: Int = 0
        var height: Int = 0
        var xBeg: Int = 0
        var yBeg: Int = 0
        var bitmap: Bitmap? = null
    }

    private val bitmapInfoList = mutableListOf<BitmapInfo>()
    private val textAreaSize: Point
    private val paginator: WenkuReaderPaginator

    init {
        Log.d("MewX", "-- view: construct my")

        val activeLoader = requireNotNull(loader) { "WenkuReaderPageView requires setViewComponents() before construction." }
        val activeTextPaint = requireNotNull(textPaint) { "TextPaint has not been initialized." }
        activeLoader.setCurrentIndex(lineIndex)

        screenDrawArea = getScreenLayout()
        val drawArea = requireNotNull(screenDrawArea)
        textAreaSize = Point(drawArea.second.x - drawArea.first.x, drawArea.second.y - drawArea.first.y)

        paginator = WenkuReaderPaginator(
            activeLoader,
            TextMeasurer { text -> activeTextPaint.measureText(text) },
            textAreaSize.x,
            textAreaSize.y,
            fontHeight,
            pxLineDistance,
            pxParagraphDistance,
        )

        when (directionForward) {
            LOADING_DIRECTION.FORWARDS -> {
                when {
                    wordIndex + 1 < activeLoader.getCurrentStringLength() -> {
                        paginator.setPageStart(
                            lineIndex,
                            if (lineIndex == 0 && wordIndex == 0) 0 else wordIndex + 1,
                        )
                        paginator.calcFromFirst()
                    }
                    lineIndex + 1 < activeLoader.getElementCount() -> {
                        paginator.setPageStart(lineIndex + 1, 0)
                        paginator.calcFromFirst()
                    }
                    else -> Log.d("MewX", "-- view: end construct A, just return")
                }
            }
            LOADING_DIRECTION.CURRENT -> {
                paginator.setPageStart(lineIndex, wordIndex)
                paginator.calcFromFirst()
            }
            LOADING_DIRECTION.BACKWARDS -> {
                if (wordIndex > 0) {
                    paginator.setPageEnd(lineIndex, wordIndex - 1)
                } else if (lineIndex > 0) {
                    paginator.setPageEnd(
                        lineIndex - 1,
                        activeLoader.getStringLength(paginator.getLastLineIndex()) - 1,
                    )
                }
                paginator.calcFromLast()
            }
        }

        for (lineInfo in paginator.getLineInfoList()) {
            Log.d("MewX", "get: ${lineInfo.text()}")
        }
    }

    private fun getScreenLayout(): Pair<Point, Point> {
        val activeScreenSize = requireNotNull(screenSize) { "Screen size has not been initialized." }
        val statusBarHeight = LightTool.getStatusBarHeightValue(MyApp.getContext())
        val navBarHeight = LightTool.getNavigationBarHeightValue(MyApp.getContext())

        val cutout: Rect = LightTool.getDisplayCutout()
        val top = pxPageEdgeDistance + kotlin.math.max(cutout.top, statusBarHeight)
        val left = pxPageEdgeDistance + cutout.left
        val right = pxPageEdgeDistance + cutout.right
        val bottom = pxPageEdgeDistance + pxWidgetHeight + cutout.bottom

        val topLeft = Point(left, top)
        val bottomRight = Point(activeScreenSize.x - right, activeScreenSize.y - bottom)
        return Pair(topLeft, bottomRight)
    }

    private fun drawBackground(canvas: Canvas) {
        val activeScreenSize = requireNotNull(screenSize)
        val activeSetting = requireNotNull(setting)

        if (GlobalConfig.isEinkModeEnabled()) {
            val paintBackground = Paint()
            paintBackground.color = 0xFFFFFFFF.toInt()
            canvas.drawRect(0f, 0f, activeScreenSize.x.toFloat(), activeScreenSize.y.toFloat(), paintBackground)
        } else if (getInDayMode()) {
            bmdBackground?.draw(canvas)
            var yellowBitmap = requireNotNull(bmBackgroundYellow)
            if (yellowBitmap.width != activeScreenSize.x || yellowBitmap.height != activeScreenSize.y) {
                yellowBitmap = Bitmap.createScaledBitmap(yellowBitmap, activeScreenSize.x, activeScreenSize.y, true)
                bmBackgroundYellow = yellowBitmap
            }
            canvas.drawBitmap(yellowBitmap, 0f, 0f, null)
        } else {
            val paintBackground = Paint()
            paintBackground.color = activeSetting.bgColorDark
            canvas.drawRect(0f, 0f, activeScreenSize.x.toFloat(), activeScreenSize.y.toFloat(), paintBackground)
        }
    }

    private fun drawWidgets(canvas: Canvas) {
        val activeLoader = requireNotNull(loader)
        val drawArea = requireNotNull(screenDrawArea)
        val activeWidgetTextPaint = requireNotNull(widgetTextPaint)

        canvas.drawText(
            activeLoader.getChapterName().orEmpty(),
            drawArea.first.x.toFloat(),
            (drawArea.second.y + widgetFontHeight).toFloat(),
            activeWidgetTextPaint,
        )

        val percentage = "( ${(paginator.getLastLineIndex() + 1) * 100 / activeLoader.getElementCount()}% )"
        val textWidth = activeWidgetTextPaint.measureText(percentage).toInt()
        canvas.drawText(
            percentage,
            (drawArea.second.x - textWidth).toFloat(),
            (drawArea.second.y + widgetFontHeight).toFloat(),
            activeWidgetTextPaint,
        )
    }

    private fun drawContent(canvas: Canvas) {
        val drawArea = requireNotNull(screenDrawArea)
        val activeTextPaint = requireNotNull(textPaint)
        var heightSum = drawArea.first.y + fontHeight

        for (i in paginator.getLineInfoList().indices) {
            val lineInfo = paginator.getLineInfoList()[i]
            if (i != 0) {
                heightSum += if (lineInfo.text().length > 2 && lineInfo.text().substring(0, 2) == "　　") {
                    pxParagraphDistance
                } else {
                    pxLineDistance
                }
            }

            Log.d(WenkuReaderPageView::class.java.simpleName, "draw: ${lineInfo.text()}")
            when (lineInfo.type()) {
                WenkuReaderLoader.ElementType.TEXT -> {
                    canvas.drawText(lineInfo.text(), drawArea.first.x.toFloat(), heightSum.toFloat(), activeTextPaint)
                    heightSum += fontHeight
                }
                WenkuReaderLoader.ElementType.IMAGE_DEPENDENT -> drawDependentImage(canvas, i, lineInfo, heightSum)
                else -> {
                    canvas.drawText(
                        "（！请先用旧引擎浏览）图片${lineInfo.text().drop(21)}",
                        drawArea.first.x.toFloat(),
                        heightSum.toFloat(),
                        activeTextPaint,
                    )
                }
            }
        }
    }

    private fun drawDependentImage(canvas: Canvas, lineInfoIndex: Int, lineInfo: LineInfo, heightSum: Int) {
        val drawArea = requireNotNull(screenDrawArea)
        val activeTextPaint = requireNotNull(textPaint)
        val existingBitmapInfo = bitmapInfoList.firstOrNull { it.idxLineInfo == lineInfoIndex }

        if (existingBitmapInfo == null) {
            canvas.drawText(
                "正在加载图片：${lineInfo.text().drop(21)}",
                drawArea.first.x.toFloat(),
                heightSum.toFloat(),
                activeTextPaint,
            )
            val bitmapInfo = BitmapInfo().apply {
                idxLineInfo = lineInfoIndex
                xBeg = drawArea.first.x
                yBeg = drawArea.first.y
                height = textAreaSize.y
                width = textAreaSize.x
            }
            bitmapInfoList.add(0, bitmapInfo)
            AsyncLoadImage().execute(bitmapInfoList[0])
            return
        }

        val bitmap = existingBitmapInfo.bitmap
        if (bitmap == null) {
            canvas.drawText(
                "正在加载图片：${lineInfo.text().drop(21)}",
                drawArea.first.x.toFloat(),
                heightSum.toFloat(),
                activeTextPaint,
            )
        } else {
            val newX = (drawArea.second.x - drawArea.first.x - existingBitmapInfo.width) / 2 + existingBitmapInfo.xBeg
            val newY = (drawArea.second.y - drawArea.first.y - existingBitmapInfo.height) / 2 + existingBitmapInfo.yBeg
            canvas.drawBitmap(bitmap, newX.toFloat(), newY.toFloat(), Paint())
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (setting == null || loader == null) return

        Log.d(WenkuReaderPageView::class.java.simpleName, "onDraw()")
        drawBackground(canvas)
        drawWidgets(canvas)
        drawContent(canvas)
    }

    fun getFirstLineIndex(): Int = paginator.getFirstLineIndex()

    fun getFirstWordIndex(): Int = paginator.getFirstWordIndex()

    fun getLastLineIndex(): Int = paginator.getLastLineIndex()

    fun getLastWordIndex(): Int = paginator.getLastWordIndex()

    private inner class AsyncLoadImage : AsyncTask<BitmapInfo, Int, Wenku8Error.ErrorCode>() {
        override fun doInBackground(vararg params: BitmapInfo): Wenku8Error.ErrorCode {
            val bitmapInfo = params[0]
            var imgFileName = GlobalConfig.generateImageFileNameByURL(
                paginator.getLineInfoList()[bitmapInfo.idxLineInfo].text(),
            )

            if (GlobalConfig.getExistingNovelContentImagePath(imgFileName) == null) {
                if (!GlobalConfig.saveNovelContentImage(paginator.getLineInfoList()[bitmapInfo.idxLineInfo].text())) {
                    return Wenku8Error.ErrorCode.NETWORK_ERROR
                }

                if (GlobalConfig.getExistingNovelContentImagePath(imgFileName) == null) {
                    return Wenku8Error.ErrorCode.STORAGE_ERROR
                }

                imgFileName = GlobalConfig.generateImageFileNameByURL(
                    paginator.getLineInfoList()[bitmapInfo.idxLineInfo].text(),
                )
            }

            val targetSize = ImageSize(bitmapInfo.width, bitmapInfo.height)
            bitmapInfo.bitmap = ImageLoader.getInstance().loadImageSync(
                "file://${GlobalConfig.getExistingNovelContentImagePath(imgFileName)}",
                targetSize,
            )

            val bitmap = bitmapInfo.bitmap ?: return Wenku8Error.ErrorCode.IMAGE_LOADING_ERROR
            val width = bitmap.width
            val height = bitmap.height
            if (bitmapInfo.height / bitmapInfo.width.toFloat() > height / width.toFloat()) {
                val percentage = height.toFloat() / width
                bitmapInfo.height = (bitmapInfo.width * percentage).toInt()
            } else {
                val percentage = width.toFloat() / height
                bitmapInfo.width = (bitmapInfo.height * percentage).toInt()
            }

            bitmapInfo.bitmap = Bitmap.createScaledBitmap(bitmap, bitmapInfo.width, bitmapInfo.height, true)
            return Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED
        }

        override fun onPostExecute(errorCode: Wenku8Error.ErrorCode) {
            super.onPostExecute(errorCode)

            if (errorCode == Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED) {
                this@WenkuReaderPageView.postInvalidate()
            } else {
                Log.e(TAG, "onPostExecute: image cannot be loaded $errorCode")
                Toast.makeText(context, errorCode.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun watchImageDetailed(activity: Activity) {
        val firstBitmapInfo = bitmapInfoList.firstOrNull()
        if (firstBitmapInfo?.bitmap == null) {
            Toast.makeText(context, resources.getString(R.string.reader_view_image_no_image), Toast.LENGTH_SHORT).show()
        } else {
            val intent = Intent(activity, ViewImageDetailActivity::class.java)
            intent.putExtra(
                "path",
                GlobalConfig.getExistingNovelContentImagePath(
                    GlobalConfig.generateImageFileNameByURL(
                        paginator.getLineInfoList()[firstBitmapInfo.idxLineInfo].text(),
                    ),
                ),
            )
            activity.startActivity(intent)
            activity.overridePendingTransition(R.anim.fade_in, R.anim.hold)
        }
    }

    fun hasImageInPage(): Boolean {
        return paginator.getLineInfoList().any {
            it.type() == WenkuReaderLoader.ElementType.IMAGE_DEPENDENT ||
                it.type() == WenkuReaderLoader.ElementType.IMAGE_INDEPENDENT
        }
    }

    companion object {
        private val TAG: String = WenkuReaderPageView::class.java.simpleName
        private const val SAMPLE_TEXT = "轻"
        private val random = Random()
        private val bmTextureYellowResourceIds = intArrayOf(
            R.drawable.reader_bg_yellow1,
            R.drawable.reader_bg_yellow2,
            R.drawable.reader_bg_yellow3,
        )

        private var inDayMode = true
        private var loader: WenkuReaderLoader? = null
        private var setting: WenkuReaderSettingV1? = null
        private var pxLineDistance = 0
        private var pxParagraphDistance = 0
        private var pxPageEdgeDistance = 0
        private var pxWidgetHeight = 0
        private var screenSize: Point? = null
        private var screenDrawArea: Pair<Point, Point>? = null
        private var typeface: Typeface? = null
        private var textPaint: TextPaint? = null
        private var widgetTextPaint: TextPaint? = null
        private var fontHeight = 0
        private var widgetFontHeight = 0
        private var bmBackgroundYellow: Bitmap? = null
        private var bmdBackground: BitmapDrawable? = null
        private var isBackgroundSet = false

        @JvmStatic
        fun getInDayMode(): Boolean = inDayMode

        @JvmStatic
        fun switchDayMode(): Boolean {
            inDayMode = !inDayMode
            return inDayMode
        }

        @JvmStatic
        fun setViewComponents(wrl: WenkuReaderLoader, wrs: WenkuReaderSettingV1, forceMode: Boolean) {
            loader = wrl
            setting = wrs
            pxLineDistance = LightTool.dip2px(MyApp.getContext(), wrs.getLineDistance().toFloat())
            pxParagraphDistance = LightTool.dip2px(MyApp.getContext(), wrs.getParagraphDistance().toFloat())
            pxPageEdgeDistance = LightTool.dip2px(MyApp.getContext(), wrs.getPageEdgeDistance().toFloat())

            try {
                if (wrs.getUseCustomFont()) {
                    typeface = Typeface.createFromFile(wrs.getCustomFontPath())
                }
            } catch (exception: Exception) {
                Toast.makeText(
                    MyApp.getContext(),
                    "$exception\n可能的原因有：字体文件不在内置SD卡；内存太小字体太大，请使用简体中文字体，而不是CJK或GBK，谢谢，此功能为试验性功能；",
                    Toast.LENGTH_SHORT,
                ).show()
            }

            val newTextPaint = TextPaint().apply {
                color = if (getInDayMode()) wrs.fontColorDark else wrs.fontColorLight
                textSize = LightTool.sp2px(MyApp.getContext(), wrs.getFontSize().toFloat()).toFloat()
                if (wrs.getUseCustomFont() && typeface != null) {
                    typeface = Companion.typeface
                }
                isAntiAlias = true
            }
            textPaint = newTextPaint
            fontHeight = newTextPaint.measureText(SAMPLE_TEXT).toInt()

            val newWidgetTextPaint = TextPaint().apply {
                color = if (getInDayMode()) wrs.fontColorDark else wrs.fontColorLight
                textSize = LightTool.sp2px(MyApp.getContext(), wrs.widgetTextSize.toFloat()).toFloat()
                isAntiAlias = true
            }
            widgetTextPaint = newWidgetTextPaint
            widgetFontHeight = newTextPaint.measureText(SAMPLE_TEXT).toInt()

            pxWidgetHeight = LightTool.dip2px(MyApp.getContext(), wrs.widgetHeight.toFloat())
            pxWidgetHeight = 3 * widgetFontHeight / 2

            if (forceMode || !isBackgroundSet) {
                screenSize = LightTool.getRealScreenSize(MyApp.getContext())
                val activeScreenSize = requireNotNull(screenSize)

                if (wrs.getPageBackgroundType() == WenkuReaderSettingV1.PAGE_BACKGROUND_TYPE.CUSTOM) {
                    try {
                        bmBackgroundYellow = BitmapFactory.decodeFile(wrs.getPageBackgroundCustomPath())
                    } catch (outOfMemoryError: OutOfMemoryError) {
                        try {
                            val options = BitmapFactory.Options().apply {
                                inSampleSize = 2
                            }
                            bmBackgroundYellow = BitmapFactory.decodeFile(wrs.getPageBackgroundCustomPath(), options)
                        } catch (exception: Exception) {
                            exception.printStackTrace()
                            return
                        }
                    }
                    bmdBackground = null
                }

                if (wrs.getPageBackgroundType() == WenkuReaderSettingV1.PAGE_BACKGROUND_TYPE.SYSTEM_DEFAULT ||
                    bmBackgroundYellow == null
                ) {
                    bmBackgroundYellow = BitmapFactory.decodeResource(
                        MyApp.getContext().resources,
                        R.drawable.reader_bg_yellow_edge,
                    )
                    val texturePattern = BitmapFactory.decodeResource(
                        MyApp.getContext().resources,
                        bmTextureYellowResourceIds[random.nextInt(bmTextureYellowResourceIds.size)],
                    )
                    bmdBackground = BitmapDrawable(MyApp.getContext().resources, texturePattern).apply {
                        setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
                        setBounds(0, 0, activeScreenSize.x, activeScreenSize.y)
                    }
                }
                isBackgroundSet = true
            }
        }

        @JvmStatic
        fun resetTextColor() {
            val activeSetting = setting ?: return
            val textColor = if (getInDayMode() || GlobalConfig.isEinkModeEnabled()) {
                activeSetting.fontColorDark
            } else {
                activeSetting.fontColorLight
            }
            textPaint?.color = textColor
            widgetTextPaint?.color = textColor
        }
    }
}
