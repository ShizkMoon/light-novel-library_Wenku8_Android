package org.mewx.wenku8.activity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mewx.wenku8.api.Wenku8Error

class NovelChapterCacheWorkflowTest {
    @Test
    fun returnsCancelledBeforeLoadingChapterWhenTaskIsInactive() {
        var chapterCacheUsed = false
        val workflow = workflow(
            cacheChapterXml = { _, _, _, _ ->
                chapterCacheUsed = true
                NovelChapterCacheResult(Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED, "<chapter />")
            },
        )

        val result = workflow.cacheChapter(
            aid = 1,
            chapterCid = 101,
            language = "zh",
            forceUpdate = false,
            shouldCacheImages = true,
            completeChapterWork = true,
            progressTracker = NovelCacheProgressTracker(),
            isActive = { false },
            publishProgress = {},
        )

        assertEquals(Wenku8Error.ErrorCode.USER_CANCELLED_TASK, result)
        assertEquals(false, chapterCacheUsed)
    }

    @Test
    fun cachesChapterImagesAndCompletesChapterWorkForFullCache() {
        val events = mutableListOf<NovelCacheProgressEvent>()
        val chapterRequests = mutableListOf<ChapterRequest>()
        val imageRequests = mutableListOf<ImageRequest>()
        val tracker = NovelCacheProgressTracker()
        tracker.startChapterTotal(emptyList())
        val workflow = workflow(
            cacheChapterXml = { aid, chapterCid, language, forceUpdate ->
                chapterRequests += ChapterRequest(aid, chapterCid, language, forceUpdate)
                NovelChapterCacheResult(Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED, "<chapter>xml</chapter>")
            },
            cacheImages = { xml, forceUpdate, _, _, _ ->
                imageRequests += ImageRequest(xml, forceUpdate)
                Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED
            },
        )

        val result = workflow.cacheChapter(
            aid = 1,
            chapterCid = 101,
            language = "zh",
            forceUpdate = true,
            shouldCacheImages = true,
            completeChapterWork = true,
            progressTracker = tracker,
            isActive = { true },
            publishProgress = events::add,
        )

        assertEquals(Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED, result)
        assertEquals(listOf(ChapterRequest(1, 101, "zh", true)), chapterRequests)
        assertEquals(listOf(ImageRequest("<chapter>xml</chapter>", true)), imageRequests)
        assertEquals(listOf(NovelCacheProgressEvent.ProgressChanged(1)), events)
    }

    @Test
    fun canLeaveChapterCompletionToSelectedVolumeCaller() {
        val events = mutableListOf<NovelCacheProgressEvent>()
        val workflow = workflow()

        val result = workflow.cacheChapter(
            aid = 1,
            chapterCid = 101,
            language = "zh",
            forceUpdate = false,
            shouldCacheImages = false,
            completeChapterWork = false,
            progressTracker = NovelCacheProgressTracker(),
            isActive = { true },
            publishProgress = events::add,
        )

        assertEquals(Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED, result)
        assertTrue(events.isEmpty())
    }

    @Test
    fun returnsChapterCacheErrorWithoutCachingImagesOrCompletingWork() {
        val events = mutableListOf<NovelCacheProgressEvent>()
        var imageCacheUsed = false
        val workflow = workflow(
            cacheChapterXml = { _, _, _, _ ->
                NovelChapterCacheResult(Wenku8Error.ErrorCode.NETWORK_ERROR)
            },
            cacheImages = { _, _, _, _, _ ->
                imageCacheUsed = true
                Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED
            },
        )

        val result = workflow.cacheChapter(
            aid = 1,
            chapterCid = 101,
            language = "zh",
            forceUpdate = false,
            shouldCacheImages = true,
            completeChapterWork = true,
            progressTracker = NovelCacheProgressTracker(),
            isActive = { true },
            publishProgress = events::add,
        )

        assertEquals(Wenku8Error.ErrorCode.NETWORK_ERROR, result)
        assertEquals(false, imageCacheUsed)
        assertTrue(events.isEmpty())
    }

    private fun workflow(
        cacheChapterXml: (Int, Int, String, Boolean) -> NovelChapterCacheResult = { _, _, _, _ ->
            NovelChapterCacheResult(Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED, "<chapter />")
        },
        cacheImages: (
            String,
            Boolean,
            NovelCacheProgressTracker,
            () -> Boolean,
            (NovelCacheProgressEvent) -> Unit,
        ) -> Wenku8Error.ErrorCode = { _, _, _, _, _ ->
            Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED
        },
    ): NovelChapterCacheWorkflow<String> =
        NovelChapterCacheWorkflow(
            cacheChapterXml = cacheChapterXml,
            cacheImages = cacheImages,
        )

    private data class ChapterRequest(
        val aid: Int,
        val chapterCid: Int,
        val language: String,
        val forceUpdate: Boolean,
    )

    private data class ImageRequest(
        val xml: String,
        val forceUpdate: Boolean,
    )
}
