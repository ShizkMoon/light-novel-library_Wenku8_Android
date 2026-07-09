package org.mewx.wenku8.activity

import org.junit.Assert.assertEquals
import org.junit.Test
import org.mewx.wenku8.api.Wenku8Error

class NovelChapterImageCacheCoordinatorTest {
    @Test
    fun cachesEachReferencedImageAndPublishesProgressAroundSuccessfulDownloads() {
        val cacheCalls = mutableListOf<CacheCall>()
        val events = mutableListOf<NovelCacheProgressEvent>()
        val coordinator = NovelChapterImageCacheCoordinator(
            imageReferencesForXml = { listOf("https://img.example.test/1.png", "https://img.example.test/2.png") },
            cacheImage = { url, forceUpdate ->
                cacheCalls += CacheCall(url, forceUpdate)
                Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED
            },
        )

        val result = coordinator.cacheImages(
            xml = "<chapter />",
            forceUpdate = true,
            progressTracker = NovelCacheProgressTracker(),
            isActive = { true },
            publishProgress = events::add,
        )

        assertEquals(Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED, result)
        assertEquals(
            listOf(
                CacheCall("https://img.example.test/1.png", true),
                CacheCall("https://img.example.test/2.png", true),
            ),
            cacheCalls,
        )
        assertEquals(
            listOf(
                NovelCacheProgressEvent.MaxChanged(1),
                NovelCacheProgressEvent.ProgressChanged(1),
                NovelCacheProgressEvent.MaxChanged(2),
                NovelCacheProgressEvent.ProgressChanged(2),
            ),
            events,
        )
    }

    @Test
    fun returnsCacheErrorWithoutCompletingFailedImageWork() {
        val events = mutableListOf<NovelCacheProgressEvent>()
        val coordinator = NovelChapterImageCacheCoordinator(
            imageReferencesForXml = { listOf("https://img.example.test/broken.png") },
            cacheImage = { _, _ -> Wenku8Error.ErrorCode.NETWORK_ERROR },
        )

        val result = coordinator.cacheImages(
            xml = "<chapter />",
            forceUpdate = false,
            progressTracker = NovelCacheProgressTracker(),
            isActive = { true },
            publishProgress = events::add,
        )

        assertEquals(Wenku8Error.ErrorCode.NETWORK_ERROR, result)
        assertEquals(listOf(NovelCacheProgressEvent.MaxChanged(1)), events)
    }

    @Test
    fun returnsCancelledAfterSuccessfulImageCacheWhenTaskIsNoLongerActive() {
        val events = mutableListOf<NovelCacheProgressEvent>()
        val coordinator = NovelChapterImageCacheCoordinator(
            imageReferencesForXml = { listOf("https://img.example.test/cancelled.png") },
            cacheImage = { _, _ -> Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED },
        )

        val result = coordinator.cacheImages(
            xml = "<chapter />",
            forceUpdate = false,
            progressTracker = NovelCacheProgressTracker(),
            isActive = { false },
            publishProgress = events::add,
        )

        assertEquals(Wenku8Error.ErrorCode.USER_CANCELLED_TASK, result)
        assertEquals(listOf(NovelCacheProgressEvent.MaxChanged(1)), events)
    }

    @Test
    fun passesRawXmlToImageReferenceParser() {
        var parsedXml = ""
        val coordinator = NovelChapterImageCacheCoordinator(
            imageReferencesForXml = { xml ->
                parsedXml = xml
                emptyList()
            },
            cacheImage = { _, _ -> error("no images should be cached") },
        )

        val result = coordinator.cacheImages(
            xml = "<chapter>xml</chapter>",
            forceUpdate = false,
            progressTracker = NovelCacheProgressTracker(),
            isActive = { true },
            publishProgress = {},
        )

        assertEquals(Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED, result)
        assertEquals("<chapter>xml</chapter>", parsedXml)
    }

    private data class CacheCall(
        val url: String,
        val forceUpdate: Boolean,
    )
}
