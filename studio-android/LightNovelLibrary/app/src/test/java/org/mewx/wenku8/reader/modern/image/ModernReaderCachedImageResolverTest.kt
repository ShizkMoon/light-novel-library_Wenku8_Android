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
}
