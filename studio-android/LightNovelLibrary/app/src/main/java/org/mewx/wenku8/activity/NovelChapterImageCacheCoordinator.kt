package org.mewx.wenku8.activity

import org.mewx.wenku8.api.Wenku8Error

class NovelChapterImageCacheCoordinator(
    private val imageReferencesForXml: (String) -> List<String>,
    private val cacheImage: (String, Boolean) -> Wenku8Error.ErrorCode,
) {
    fun cacheImages(
        xml: String,
        forceUpdate: Boolean,
        progressTracker: NovelCacheProgressTracker,
        isActive: () -> Boolean,
        publishProgress: (NovelCacheProgressEvent) -> Unit,
    ): Wenku8Error.ErrorCode {
        for (imageReference in imageReferencesForXml(xml)) {
            publishProgress(progressTracker.addImageWork())
            val result = cacheImage(imageReference, forceUpdate)
            if (result != Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED) return result
            if (!isActive()) return Wenku8Error.ErrorCode.USER_CANCELLED_TASK
            publishProgress(progressTracker.completeWork())
        }
        return Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED
    }
}
