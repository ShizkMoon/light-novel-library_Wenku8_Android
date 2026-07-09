package org.mewx.wenku8.reader.modern.image

class ModernReaderCachedImageResolver(
    private val fileNameForUrl: (String) -> String,
    private val existingPathForFileName: (String) -> String?,
    private val saveImage: (String) -> Boolean = { false },
    private val deleteCachedPath: (String) -> Boolean = { false },
) {
    fun cachedPathFor(sourceUrl: String): String? =
        existingPathForFileName(fileNameForUrl(sourceUrl))

    fun cachePathAfterSaving(sourceUrl: String): String? {
        val cachedPath = cachedPathFor(sourceUrl)
        if (cachedPath != null) {
            return cachedPath
        }
        if (!saveImage(sourceUrl)) {
            return null
        }
        return cachedPathFor(sourceUrl)
    }

    fun cachePathAfterRefreshing(sourceUrl: String): String? {
        val cachedPath = cachedPathFor(sourceUrl)
        if (cachedPath != null && !deleteCachedPath(cachedPath)) {
            return null
        }
        if (!saveImage(sourceUrl)) {
            return null
        }
        return cachedPathFor(sourceUrl)
    }
}
