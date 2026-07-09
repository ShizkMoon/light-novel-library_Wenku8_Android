package org.mewx.wenku8.reader.modern.ui

import org.mewx.wenku8.reader.modern.model.ReaderLine

enum class ReaderImageLineDisplayMode {
    IMAGE,
    PLACEHOLDER,
    BROKEN_CACHE,
}

data class ReaderImageCacheRequest(
    val source: String,
    val refreshExisting: Boolean = false,
)

data class ReaderImageLineUiModel(
    val source: String,
    val cachedPath: String?,
    val placeholderText: String,
) {
    fun cacheRequest(bitmapAvailable: Boolean): ReaderImageCacheRequest? =
        when {
            cachedPath == null -> ReaderImageCacheRequest(source = source)
            bitmapAvailable -> null
            else -> ReaderImageCacheRequest(source = source, refreshExisting = true)
        }

    fun displayMode(bitmapAvailable: Boolean): ReaderImageLineDisplayMode =
        when {
            cachedPath == null -> ReaderImageLineDisplayMode.PLACEHOLDER
            bitmapAvailable -> ReaderImageLineDisplayMode.IMAGE
            else -> ReaderImageLineDisplayMode.BROKEN_CACHE
        }

    fun openImagePath(bitmapAvailable: Boolean): String? =
        if (displayMode(bitmapAvailable) == ReaderImageLineDisplayMode.IMAGE) {
            cachedPath
        } else {
            null
        }

    companion object {
        fun from(
            line: ReaderLine,
            cachedPathForSource: (String) -> String?,
        ): ReaderImageLineUiModel {
            val source = line.source ?: line.text
            return ReaderImageLineUiModel(
                source = source,
                cachedPath = cachedPathForSource(source),
                placeholderText = "插图 $source",
            )
        }
    }
}
