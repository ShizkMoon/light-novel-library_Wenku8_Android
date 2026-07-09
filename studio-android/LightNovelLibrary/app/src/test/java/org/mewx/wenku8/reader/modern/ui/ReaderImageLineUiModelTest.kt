package org.mewx.wenku8.reader.modern.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mewx.wenku8.reader.modern.model.ReaderLine
import org.mewx.wenku8.reader.modern.model.ReaderLineType

class ReaderImageLineUiModelTest {
    @Test
    fun resolvesCachedImagePathForImageLineSource() {
        val model = ReaderImageLineUiModel.from(
            line = ReaderLine(
                type = ReaderLineType.IMAGE,
                text = "https://img.example.test/cover.png",
                source = "https://img.example.test/cover.png",
            ),
            cachedPathForSource = { source -> "/cache/${source.substringAfterLast('/')}" },
        )

        assertEquals("https://img.example.test/cover.png", model.source)
        assertEquals("/cache/cover.png", model.cachedPath)
        assertEquals("插图 https://img.example.test/cover.png", model.placeholderText)
    }

    @Test
    fun keepsPlaceholderWhenImageIsNotCached() {
        val model = ReaderImageLineUiModel.from(
            line = ReaderLine(
                type = ReaderLineType.IMAGE,
                text = "https://img.example.test/missing.png",
                source = "https://img.example.test/missing.png",
            ),
            cachedPathForSource = { null },
        )

        assertEquals("https://img.example.test/missing.png", model.source)
        assertNull(model.cachedPath)
        assertEquals("插图 https://img.example.test/missing.png", model.placeholderText)
    }

    @Test
    fun uncachedImageRequestsCacheAndKeepsPlaceholderDisplay() {
        val model = ReaderImageLineUiModel.from(
            line = ReaderLine(
                type = ReaderLineType.IMAGE,
                text = "https://img.example.test/missing.png",
                source = "https://img.example.test/missing.png",
            ),
            cachedPathForSource = { null },
        )

        assertEquals(
            ReaderImageCacheRequest(
                source = "https://img.example.test/missing.png",
                refreshExisting = false,
            ),
            model.cacheRequest(bitmapAvailable = false),
        )
        assertEquals(ReaderImageLineDisplayMode.PLACEHOLDER, model.displayMode(bitmapAvailable = false))
        assertNull(model.openImagePath(bitmapAvailable = false))
    }

    @Test
    fun cachedImageDoesNotRequestCacheAndOpensOnlyWhenBitmapIsAvailable() {
        val model = ReaderImageLineUiModel.from(
            line = ReaderLine(
                type = ReaderLineType.IMAGE,
                text = "https://img.example.test/cover.png",
                source = "https://img.example.test/cover.png",
            ),
            cachedPathForSource = { "/cache/cover.png" },
        )

        assertNull(model.cacheRequest(bitmapAvailable = true))
        assertEquals(ReaderImageLineDisplayMode.IMAGE, model.displayMode(bitmapAvailable = true))
        assertEquals("/cache/cover.png", model.openImagePath(bitmapAvailable = true))
    }

    @Test
    fun brokenCachedImageRequestsRefreshAndCannotOpen() {
        val model = ReaderImageLineUiModel.from(
            line = ReaderLine(
                type = ReaderLineType.IMAGE,
                text = "https://img.example.test/corrupt.png",
                source = "https://img.example.test/corrupt.png",
            ),
            cachedPathForSource = { "/cache/corrupt.png" },
        )

        assertEquals(
            ReaderImageCacheRequest(
                source = "https://img.example.test/corrupt.png",
                refreshExisting = true,
            ),
            model.cacheRequest(bitmapAvailable = false),
        )
        assertEquals(ReaderImageLineDisplayMode.BROKEN_CACHE, model.displayMode(bitmapAvailable = false))
        assertNull(model.openImagePath(bitmapAvailable = false))
    }
}
