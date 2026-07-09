package org.mewx.wenku8.reader.modern.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ModernReaderCachedImageResolverTest {
    @Test
    fun resolvesCachedPathFromGeneratedImageFileName() {
        val lookedUpNames = mutableListOf<String>()
        val resolver = ModernReaderCachedImageResolver(
            fileNameForUrl = { url -> "cached-${url.substringAfterLast('/')}" },
            existingPathForFileName = { fileName ->
                lookedUpNames += fileName
                "/reader-cache/$fileName"
            },
        )

        val result = resolver.cachedPathFor("https://img.example.test/cover.png")

        assertEquals("/reader-cache/cached-cover.png", result)
        assertEquals(listOf("cached-cover.png"), lookedUpNames)
    }

    @Test
    fun returnsNullWhenImageIsNotCached() {
        val resolver = ModernReaderCachedImageResolver(
            fileNameForUrl = { "missing.png" },
            existingPathForFileName = { null },
        )

        assertNull(resolver.cachedPathFor("https://img.example.test/missing.png"))
    }

    @Test
    fun returnsExistingPathWithoutSavingImageAgain() {
        var saveCalls = 0
        val resolver = ModernReaderCachedImageResolver(
            fileNameForUrl = { "cover.png" },
            existingPathForFileName = { "/reader-cache/cover.png" },
            saveImage = {
                saveCalls += 1
                true
            },
        )

        val result = resolver.cachePathAfterSaving("https://img.example.test/cover.png")

        assertEquals("/reader-cache/cover.png", result)
        assertEquals(0, saveCalls)
    }

    @Test
    fun savesMissingImageAndResolvesCachedPath() {
        var saved = false
        val resolver = ModernReaderCachedImageResolver(
            fileNameForUrl = { "cover.png" },
            existingPathForFileName = { if (saved) "/reader-cache/cover.png" else null },
            saveImage = { source ->
                saved = source == "https://img.example.test/cover.png"
                saved
            },
        )

        val result = resolver.cachePathAfterSaving("https://img.example.test/cover.png")

        assertEquals("/reader-cache/cover.png", result)
    }

    @Test
    fun returnsNullWhenSavingImageFails() {
        val resolver = ModernReaderCachedImageResolver(
            fileNameForUrl = { "missing.png" },
            existingPathForFileName = { null },
            saveImage = { false },
        )

        assertNull(resolver.cachePathAfterSaving("https://img.example.test/missing.png"))
    }
}
