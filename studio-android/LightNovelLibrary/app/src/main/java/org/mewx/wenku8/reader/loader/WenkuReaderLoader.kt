package org.mewx.wenku8.reader.loader

import android.graphics.Bitmap

/**
 * Parent of all reader content loaders.
 */
abstract class WenkuReaderLoader {
    enum class ElementType {
        TEXT,
        IMAGE_INDEPENDENT,
        IMAGE_DEPENDENT,
    }

    abstract fun setChapterName(name: String?)

    abstract fun getChapterName(): String?

    abstract fun hasNext(wordIndex: Int): Boolean

    abstract fun hasPrevious(wordIndex: Int): Boolean

    abstract fun getNextType(): ElementType?

    abstract fun getNextAsString(): String?

    abstract fun getNextAsBitmap(): Bitmap?

    abstract fun getCurrentType(): ElementType?

    abstract fun getCurrentAsString(): String?

    abstract fun getCurrentStringLength(): Int

    abstract fun getCurrentAsBitmap(): Bitmap?

    abstract fun getPreviousType(): ElementType?

    abstract fun getPreviousAsString(): String?

    abstract fun getPreviousAsBitmap(): Bitmap?

    abstract fun getStringLength(n: Int): Int

    abstract fun getElementCount(): Int

    abstract fun getCurrentIndex(): Int

    abstract fun setCurrentIndex(i: Int)

    abstract fun closeLoader()
}
