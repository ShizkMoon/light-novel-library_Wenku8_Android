package org.mewx.wenku8.reader.modern.image

class ModernReaderImageCacheCoordinator(
    private val cachedPathForSource: (String) -> String?,
    private val cachePathAfterSaving: (String) -> String?,
    private val refreshCachePathAfterDeleting: (String) -> String? = cachePathAfterSaving,
    private val runInBackground: (() -> Unit) -> Unit,
    private val postToMain: (() -> Unit) -> Unit,
    private val requestGate: ModernReaderImageCacheRequestGate = ModernReaderImageCacheRequestGate(),
) {
    fun cachedPathForSource(
        source: String,
        resolvedImagePaths: Map<String, String>,
    ): String? =
        resolvedImagePaths[source] ?: cachedPathForSource(source)

    fun requestImageCache(
        source: String,
        resolvedImagePaths: Map<String, String>,
        refreshExisting: Boolean = false,
        isActive: () -> Boolean,
        onImageCached: (String, String) -> Unit,
    ) {
        if (
            (!refreshExisting && cachedPathForSource(source, resolvedImagePaths) != null) ||
            !requestGate.tryStart(source)
        ) {
            return
        }
        runInBackground {
            val path = if (refreshExisting) {
                refreshCachePathAfterDeleting(source)
            } else {
                cachePathAfterSaving(source)
            }
            postToMain {
                requestGate.finish(source)
                if (path != null && isActive()) {
                    onImageCached(source, path)
                }
            }
        }
    }
}
