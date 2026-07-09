package org.mewx.wenku8.reader.setting

import android.graphics.Color
import org.mewx.wenku8.global.GlobalConfig
import org.mewx.wenku8.util.LightCache
import org.mewx.wenku8.util.LightTool

/**
 * First-generation legacy reader settings.
 */
class WenkuReaderSettingV1 {
    enum class PAGE_BACKGROUND_TYPE {
        SYSTEM_DEFAULT,
        CUSTOM,
    }

    @JvmField val fontColorLight: Int = Color.parseColor("#A2B4C3")
    @JvmField val fontColorDark: Int = Color.parseColor("#444444")
    @JvmField val bgColorLight: Int = Color.parseColor("#CFBEB6")
    @JvmField val bgColorDark: Int = Color.parseColor("#090C13")
    @JvmField val widgetHeight: Int = 24
    @JvmField val widgetTextSize: Int = 12

    private var fontSize = 18
    private var useCustomFont = false
    private var customFontPath = ""
    private var lineDistance = 16
    private var paragraphDistance = 20
    private var pageEdgeDistance = 8
    private var pageBackgroundType = PAGE_BACKGROUND_TYPE.SYSTEM_DEFAULT
    private var pageBackgroundCustomPath = ""

    init {
        loadFontSize()
        loadLineDistance()
        loadParagraphDistance()
        loadPageEdgeDistance()
        loadPageBackgroundPath()
        loadFontPath()
    }

    fun setFontSize(s: Int) {
        fontSize = s
        GlobalConfig.setToAllSetting(GlobalConfig.SettingItems.reader_font_size, s.toString())
    }

    fun getFontSize(): Int = fontSize

    fun setUseCustomFont(b: Boolean) {
        if (!b) {
            setCustomFontPath("0")
        }
        useCustomFont = b
    }

    fun getUseCustomFont(): Boolean = useCustomFont

    fun setCustomFontPath(s: String) {
        customFontPath = s
        useCustomFont = s != "0"
        GlobalConfig.setToAllSetting(GlobalConfig.SettingItems.reader_font_path, s)
    }

    fun getCustomFontPath(): String = customFontPath

    fun setLineDistance(l: Int) {
        lineDistance = l
        GlobalConfig.setToAllSetting(GlobalConfig.SettingItems.reader_line_distance, l.toString())
    }

    fun getLineDistance(): Int = lineDistance

    fun setParagraphDistance(l: Int) {
        paragraphDistance = l
        GlobalConfig.setToAllSetting(GlobalConfig.SettingItems.reader_paragraph_distance, l.toString())
    }

    fun getParagraphDistance(): Int = paragraphDistance

    fun setPageEdgeDistance(l: Int) {
        pageEdgeDistance = l
        GlobalConfig.setToAllSetting(GlobalConfig.SettingItems.reader_paragraph_edge_distance, l.toString())
    }

    fun getPageEdgeDistance(): Int = pageEdgeDistance

    fun setPageBackgroundType(t: PAGE_BACKGROUND_TYPE) {
        if (t == PAGE_BACKGROUND_TYPE.SYSTEM_DEFAULT) {
            setPageBackgroundCustomPath("0")
        }
        pageBackgroundType = t
    }

    fun getPageBackgroundType(): PAGE_BACKGROUND_TYPE = pageBackgroundType

    fun setPageBackgroundCustomPath(s: String) {
        pageBackgroundCustomPath = s
        pageBackgroundType = if (s == "0") PAGE_BACKGROUND_TYPE.SYSTEM_DEFAULT else PAGE_BACKGROUND_TYPE.CUSTOM
        GlobalConfig.setToAllSetting(GlobalConfig.SettingItems.reader_background_path, s)
    }

    fun getPageBackgroundCustomPath(): String = pageBackgroundCustomPath

    private fun loadFontSize() {
        val size = GlobalConfig.getFromAllSetting(GlobalConfig.SettingItems.reader_font_size)
        if (size != null && LightTool.isInteger(size)) {
            val temp = size.toInt()
            if (temp in 8..32) {
                fontSize = temp
            }
        }
    }

    private fun loadLineDistance() {
        val size = GlobalConfig.getFromAllSetting(GlobalConfig.SettingItems.reader_line_distance)
        if (size != null && LightTool.isInteger(size)) {
            val temp = size.toInt()
            if (temp in 0..32) {
                lineDistance = temp
            }
        }
    }

    private fun loadParagraphDistance() {
        val size = GlobalConfig.getFromAllSetting(GlobalConfig.SettingItems.reader_paragraph_distance)
        if (size != null && LightTool.isInteger(size)) {
            val temp = size.toInt()
            if (temp in 0..48) {
                paragraphDistance = temp
            }
        }
    }

    private fun loadPageEdgeDistance() {
        val size = GlobalConfig.getFromAllSetting(GlobalConfig.SettingItems.reader_paragraph_edge_distance)
        if (size != null && LightTool.isInteger(size)) {
            val temp = size.toInt()
            if (temp in 0..32) {
                pageEdgeDistance = temp
            }
        }
    }

    private fun loadPageBackgroundPath() {
        val size = GlobalConfig.getFromAllSetting(GlobalConfig.SettingItems.reader_background_path)
        if (size != null) {
            if (size == "0") {
                pageBackgroundType = PAGE_BACKGROUND_TYPE.SYSTEM_DEFAULT
            } else if (LightCache.testFileExist(size)) {
                pageBackgroundType = PAGE_BACKGROUND_TYPE.CUSTOM
                pageBackgroundCustomPath = size
            }
        }
    }

    private fun loadFontPath() {
        val size = GlobalConfig.getFromAllSetting(GlobalConfig.SettingItems.reader_font_path)
        if (size != null) {
            if (size == "0") {
                useCustomFont = false
            } else if (LightCache.testFileExist(size)) {
                useCustomFont = true
                customFontPath = size
            }
        }
    }
}
