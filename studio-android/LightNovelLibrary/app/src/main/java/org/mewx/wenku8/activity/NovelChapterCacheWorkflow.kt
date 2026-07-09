package org.mewx.wenku8.activity

import org.mewx.wenku8.api.Wenku8Error

class NovelChapterCacheWorkflow<Language>(
    private val cacheChapterXml: (Int, Int, Language, Boolean) -> NovelChapterCacheResult,
    private val cacheImages: (
        String,
        Boolean,
        NovelCacheProgressTracker,
        () -> Boolean,
        (NovelCacheProgressEvent) -> Unit,
    ) -> Wenku8Error.ErrorCode,
) {
    fun cacheChapter(
        aid: Int,
        chapterCid: Int,
        language: Language,
        forceUpdate: Boolean,
        shouldCacheImages: Boolean,
        completeChapterWork: Boolean,
        progressTracker: NovelCacheProgressTracker,
        isActive: () -> Boolean,
        publishProgress: (NovelCacheProgressEvent) -> Unit,
    ): Wenku8Error.ErrorCode {
        if (!isActive()) return Wenku8Error.ErrorCode.USER_CANCELLED_TASK

        val chapterResult = cacheChapterXml(aid, chapterCid, language, forceUpdate)
        if (chapterResult.errorCode != Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED) {
            return chapterResult.errorCode
        }

        if (shouldCacheImages) {
            val imageResult = cacheImages(
                chapterResult.xml,
                forceUpdate,
                progressTracker,
                isActive,
                publishProgress,
            )
            if (imageResult != Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED) return imageResult
        }

        if (completeChapterWork) {
            publishProgress(progressTracker.completeWork())
        }
        return Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED
    }
}
