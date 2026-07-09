package org.mewx.wenku8.activity

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NovelImageCachePlannerTest {
    @Test
    fun buildsCachePathsFromStorageRootsAndGeneratedFileName() {
        val paths = NovelImageCachePlanner.paths(
            url = "https://img.example.test/a.png",
            firstSaveRoot = "primary/",
            secondSaveRoot = "backup/",
            imageFolderName = "imgs",
            fileNameForUrl = { "cached.png" },
        )

        assertEquals("cached.png", paths.fileName)
        assertEquals("primary/imgs${File.separator}cached.png", paths.firstFilePath)
        assertEquals("backup/imgs${File.separator}cached.png", paths.secondFilePath)
        assertEquals("primary/imgs${File.separator}", paths.firstDirectoryPath)
        assertEquals("backup/imgs${File.separator}", paths.secondDirectoryPath)
    }

    @Test
    fun skipsDownloadWhenEitherStorageAlreadyHasImage() {
        assertFalse(NovelImageCachePlanner.shouldDownload(firstExists = true, secondExists = false, forceUpdate = false))
        assertFalse(NovelImageCachePlanner.shouldDownload(firstExists = false, secondExists = true, forceUpdate = false))
    }

    @Test
    fun downloadsWhenImageIsMissingOrForceUpdateIsRequested() {
        assertTrue(NovelImageCachePlanner.shouldDownload(firstExists = false, secondExists = false, forceUpdate = false))
        assertTrue(NovelImageCachePlanner.shouldDownload(firstExists = true, secondExists = true, forceUpdate = true))
    }
}
