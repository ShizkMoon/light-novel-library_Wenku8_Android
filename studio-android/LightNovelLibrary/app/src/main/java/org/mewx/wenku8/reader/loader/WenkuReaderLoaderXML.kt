package org.mewx.wenku8.reader.loader

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.mewx.wenku8.global.GlobalConfig
import org.mewx.wenku8.global.api.OldNovelContentParser
import org.mewx.wenku8.global.api.OldNovelContentParser.NovelContentType

/**
 * Raw XML chapter data loader. Bitmap loading may touch disk/network cache and must stay off the UI thread.
 */
class WenkuReaderLoaderXML(
    onc: List<OldNovelContentParser.NovelContent>,
) : WenkuReaderLoader() {
    private var currentIndex = 0
    private var novelContents: List<OldNovelContentParser.NovelContent>? = onc
    private var chapterNameValue: String? = null

    override fun setChapterName(name: String?) {
        chapterNameValue = name
    }

    override fun getChapterName(): String? = chapterNameValue

    override fun hasNext(wordIndex: Int): Boolean {
        val contents = novelContents ?: return false
        if (currentIndex < contents.size && currentIndex >= 0) {
            if (currentIndex + 1 < contents.size) {
                return true
            }

            val current = contents[currentIndex]
            return if (current.type == NovelContentType.TEXT && wordIndex + 1 < current.content.length) {
                true
            } else {
                current.type != NovelContentType.TEXT && wordIndex == 0
            }
        }
        return false
    }

    override fun hasPrevious(wordIndex: Int): Boolean {
        val contents = novelContents ?: return false
        if (currentIndex < contents.size && currentIndex >= 0) {
            if (currentIndex - 1 >= 0) {
                return true
            }

            val current = contents[currentIndex]
            return if (current.type == NovelContentType.TEXT && wordIndex - 1 >= 0) {
                true
            } else {
                current.type != NovelContentType.TEXT && wordIndex == current.content.length - 1
            }
        }
        return false
    }

    override fun getNextType(): ElementType? {
        val contents = novelContents ?: return null
        return if (currentIndex + 1 < contents.size && currentIndex >= 0) {
            interpretOldSign(contents[currentIndex + 1].type)
        } else {
            null
        }
    }

    override fun getNextAsString(): String? {
        val contents = novelContents ?: return null
        if (currentIndex + 1 < contents.size && currentIndex >= 0) {
            currentIndex++
            return contents[currentIndex].content
        }
        return null
    }

    override fun getNextAsBitmap(): Bitmap? {
        val contents = novelContents ?: return null
        if (currentIndex + 1 < contents.size && currentIndex >= 0) {
            currentIndex++
            return loadBitmapForContent(contents[currentIndex].content)
        }
        return null
    }

    override fun getCurrentType(): ElementType? {
        val contents = novelContents ?: return null
        return if (currentIndex < contents.size && currentIndex >= 0) {
            interpretOldSign(contents[currentIndex].type)
        } else {
            null
        }
    }

    override fun getCurrentAsString(): String? {
        val contents = novelContents ?: return null
        return if (currentIndex < contents.size && currentIndex >= 0) {
            contents[currentIndex].content
        } else {
            null
        }
    }

    override fun getCurrentStringLength(): Int = getStringLength(currentIndex)

    override fun getCurrentAsBitmap(): Bitmap? {
        val contents = novelContents ?: return null
        return if (currentIndex < contents.size && currentIndex >= 0) {
            loadBitmapForContent(contents[currentIndex].content)
        } else {
            null
        }
    }

    override fun getPreviousType(): ElementType? {
        val contents = novelContents ?: return null
        return if (currentIndex < contents.size && currentIndex - 1 >= 0) {
            interpretOldSign(contents[currentIndex - 1].type)
        } else {
            null
        }
    }

    override fun getPreviousAsString(): String? {
        val contents = novelContents ?: return null
        if (currentIndex < contents.size && currentIndex - 1 >= 0) {
            currentIndex--
            return contents[currentIndex].content
        }
        return null
    }

    override fun getPreviousAsBitmap(): Bitmap? {
        val contents = novelContents ?: return null
        if (currentIndex < contents.size && currentIndex - 1 >= 0) {
            currentIndex--
            return loadBitmapForContent(contents[currentIndex].content)
        }
        return null
    }

    override fun getStringLength(n: Int): Int {
        val contents = novelContents ?: return 0
        return if (n >= 0 && n < getElementCount()) {
            contents[n].content.length
        } else {
            0
        }
    }

    override fun getElementCount(): Int = novelContents?.size ?: 0

    override fun getCurrentIndex(): Int = currentIndex

    override fun setCurrentIndex(i: Int) {
        currentIndex = i
    }

    override fun closeLoader() {
        novelContents = null
    }

    private fun loadBitmapForContent(content: String): Bitmap? {
        var imgFileName = GlobalConfig.generateImageFileNameByURL(content)
        var path = GlobalConfig.getExistingNovelContentImagePath(imgFileName)

        if (path.isNullOrEmpty()) {
            GlobalConfig.saveNovelContentImage(content)
            imgFileName = GlobalConfig.generateImageFileNameByURL(content)
            path = GlobalConfig.getExistingNovelContentImagePath(imgFileName)
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = 2
        }
        return BitmapFactory.decodeFile(path, options)
    }

    private fun interpretOldSign(type: NovelContentType): ElementType {
        return when (type) {
            NovelContentType.TEXT -> ElementType.TEXT
            NovelContentType.IMAGE -> ElementType.IMAGE_DEPENDENT
        }
    }
}
