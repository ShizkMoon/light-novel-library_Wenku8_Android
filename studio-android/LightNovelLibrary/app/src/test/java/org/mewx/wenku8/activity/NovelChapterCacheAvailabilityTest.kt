package org.mewx.wenku8.activity

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NovelChapterCacheAvailabilityTest {
    @Test
    fun buildsPrimaryAndBackupChapterPathsFromStorageRoots() {
        val paths = NovelChapterCacheAvailability.paths(
            defaultStoragePath = "/primary/",
            backupStoragePath = "/backup/",
            saveFolderName = "saves",
            cid = 123,
        )

        assertEquals("/primary/saves${File.separator}novel${File.separator}123.xml", paths.primary)
        assertEquals("/backup/saves${File.separator}novel${File.separator}123.xml", paths.backup)
    }

    @Test
    fun reportsCachedWhenPrimaryChapterFileExists() {
        val cached = NovelChapterCacheAvailability.isCached(
            defaultStoragePath = "/primary/",
            backupStoragePath = "/backup/",
            saveFolderName = "saves",
            cid = 123,
        ) { path -> path.startsWith("/primary/") }

        assertTrue(cached)
    }

    @Test
    fun reportsCachedWhenBackupChapterFileExists() {
        val cached = NovelChapterCacheAvailability.isCached(
            defaultStoragePath = "/primary/",
            backupStoragePath = "/backup/",
            saveFolderName = "saves",
            cid = 123,
        ) { path -> path.startsWith("/backup/") }

        assertTrue(cached)
    }

    @Test
    fun reportsMissingWhenNeitherChapterFileExists() {
        val cached = NovelChapterCacheAvailability.isCached(
            defaultStoragePath = "/primary/",
            backupStoragePath = "/backup/",
            saveFolderName = "saves",
            cid = 123,
        ) { false }

        assertFalse(cached)
    }
}
