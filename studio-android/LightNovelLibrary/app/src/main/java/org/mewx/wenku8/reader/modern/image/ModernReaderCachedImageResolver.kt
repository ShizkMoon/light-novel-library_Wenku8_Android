package org.mewx.wenku8.reader.modern.image

class ModernReaderCachedImageResolver(
    private val fileNameForUrl: (String) -> String,
    private val existingPathForFileName: (String) -> String?,
) {
    fun cachedPathFor(sourceUrl: String): String? =
        existingPathForFileName(fileNameForUrl(sourceUrl))
}
