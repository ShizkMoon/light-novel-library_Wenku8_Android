package org.mewx.wenku8.reader.modern.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ModernReaderImageCacheCoordinatorTest {
    @Test
    fun prefersAlreadyResolvedImagePath() {
        val queue = WorkQueue()
        val coordinator = coordinator(queue)

        val result = coordinator.cachedPathForSource(
            source = "https://img.example.test/cover.png",
            resolvedImagePaths = mapOf("https://img.example.test/cover.png" to "/resolved/cover.png"),
        )

        assertEquals("/resolved/cover.png", result)
    }

    @Test
    fun doesNotQueueWorkWhenImageIsAlreadyCached() {
        val queue = WorkQueue()
        val coordinator = coordinator(
            queue = queue,
            cachedPathForSource = { "/cache/cover.png" },
        )

        coordinator.requestImageCache(
            source = "https://img.example.test/cover.png",
            resolvedImagePaths = emptyMap(),
            isActive = { true },
            onImageCached = { _, _ -> },
        )

        assertTrue(queue.background.isEmpty())
    }

    @Test
    fun forceRefreshQueuesWorkWhenImagePathAlreadyExists() {
        val queue = WorkQueue()
        val cachedCallbacks = mutableListOf<Pair<String, String>>()
        val coordinator = coordinator(
            queue = queue,
            cachedPathForSource = { "/cache/corrupt.png" },
            refreshCachePathAfterDeleting = { "/cache/refreshed.png" },
        )

        coordinator.requestImageCache(
            source = "https://img.example.test/corrupt.png",
            resolvedImagePaths = emptyMap(),
            refreshExisting = true,
            isActive = { true },
            onImageCached = { source, path -> cachedCallbacks += source to path },
        )
        queue.runBackground()
        queue.runMain()

        assertEquals(listOf("https://img.example.test/corrupt.png" to "/cache/refreshed.png"), cachedCallbacks)
    }

    @Test
    fun cachesImageInBackgroundThenPostsResolvedPath() {
        val queue = WorkQueue()
        val cachedCallbacks = mutableListOf<Pair<String, String>>()
        val coordinator = coordinator(
            queue = queue,
            cachedPathAfterSaving = { "/cache/cover.png" },
        )

        coordinator.requestImageCache(
            source = "https://img.example.test/cover.png",
            resolvedImagePaths = emptyMap(),
            isActive = { true },
            onImageCached = { source, path -> cachedCallbacks += source to path },
        )
        queue.runBackground()
        queue.runMain()

        assertEquals(listOf("https://img.example.test/cover.png" to "/cache/cover.png"), cachedCallbacks)
    }

    @Test
    fun blocksDuplicateWorkWhileImageSourceIsPending() {
        val queue = WorkQueue()
        val coordinator = coordinator(
            queue = queue,
            cachedPathAfterSaving = { "/cache/cover.png" },
        )

        repeat(2) {
            coordinator.requestImageCache(
                source = "https://img.example.test/cover.png",
                resolvedImagePaths = emptyMap(),
                isActive = { true },
                onImageCached = { _, _ -> },
            )
        }

        assertEquals(1, queue.background.size)
    }

    @Test
    fun skipsCallbackWhenOwnerIsNoLongerActive() {
        val queue = WorkQueue()
        val cachedCallbacks = mutableListOf<Pair<String, String>>()
        val coordinator = coordinator(
            queue = queue,
            cachedPathAfterSaving = { "/cache/cover.png" },
        )

        coordinator.requestImageCache(
            source = "https://img.example.test/cover.png",
            resolvedImagePaths = emptyMap(),
            isActive = { false },
            onImageCached = { source, path -> cachedCallbacks += source to path },
        )
        queue.runBackground()
        queue.runMain()

        assertTrue(cachedCallbacks.isEmpty())
    }

    private fun coordinator(
        queue: WorkQueue,
        cachedPathForSource: (String) -> String? = { null },
        cachedPathAfterSaving: (String) -> String? = { null },
        refreshCachePathAfterDeleting: (String) -> String? = cachedPathAfterSaving,
    ): ModernReaderImageCacheCoordinator =
        ModernReaderImageCacheCoordinator(
            cachedPathForSource = cachedPathForSource,
            cachePathAfterSaving = cachedPathAfterSaving,
            refreshCachePathAfterDeleting = refreshCachePathAfterDeleting,
            runInBackground = queue::enqueueBackground,
            postToMain = queue::enqueueMain,
        )

    private class WorkQueue {
        val background = mutableListOf<() -> Unit>()
        val main = mutableListOf<() -> Unit>()

        fun enqueueBackground(work: () -> Unit) {
            background += work
        }

        fun enqueueMain(work: () -> Unit) {
            main += work
        }

        fun runBackground() {
            background.removeAt(0).invoke()
        }

        fun runMain() {
            main.removeAt(0).invoke()
        }
    }
}
